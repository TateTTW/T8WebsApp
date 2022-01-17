package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.T8WebsApplication;
import com.t8webs.enterprise.dao.IAssignedServerDAO;
import com.t8webs.enterprise.dto.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

@Component
public class ReverseProxyUtil implements IReverseProxyUtil {

    @Autowired
    ISShUtils sshUtils;
    @Autowired
    IAssignedServerDAO assignedServerDAO;

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String domain = properties.getProperty("domainName");
    private static String orgProxyCfg = properties.getProperty("orgProxyCfg");
    private static String localFile = properties.getProperty("localProxyCfg");
    private static String remoteFile = properties.getProperty("remoteProxyCfg");
    private static String proxyUser = properties.getProperty("proxyUser");
    private static String proxyPass = properties.getProperty("proxyPass");
    private static String proxyIpAddress = properties.getProperty("proxyIpAddress");
    private static String reloadCommand = properties.getProperty("proxyReloadCmd");

    @Override
    public boolean reconfigure() throws ClassNotFoundException, SQLException, ProxyConfigLockedException, IOException {
        boolean success = false;

        if(buildLocalCfgFile()) {
            if(syncRemoteCfg()) {
                if(reloadService()){
                    success = true;
                }
            }
        }

        deleteLocalCfg();

        return success;
    }

    @Override
    public boolean syncRemoteCfg() {
        return sshUtils.doSecureFileTransfer(proxyUser, proxyPass, proxyIpAddress, localFile, remoteFile);
    }

    @Override
    public boolean reloadService() {
        return sshUtils.doSecureShellCmd(proxyUser, proxyPass, proxyIpAddress, reloadCommand);
    }

    @Override
    public boolean deleteLocalCfg() {
        File localCfg = new File(localFile);
        return localCfg.delete();
    }

    @Override
    @Retryable(maxAttempts=60, value=ProxyConfigLockedException.class, backoff=@Backoff(delay = 1000))
    public boolean buildLocalCfgFile() throws SQLException, ClassNotFoundException, IOException, ProxyConfigLockedException {
        // Create String containing a configuration line for each assigned server.
        StringBuilder newLines = new StringBuilder();

        List<Server> assignedServers = assignedServerDAO.fetchAll();
        for(Server server: assignedServers){
            newLines.append("        use_backend " + server.getVmid() + " if { hdr(host) -i " + server.getName().trim() + "." + domain + " }\n");
        }

        File orgCfg = new File(orgProxyCfg);
        File tempCfg = File.createTempFile("haproxy_",".cfg");

        BufferedWriter bw = new BufferedWriter(new FileWriter(tempCfg));
        BufferedReader br = null;

        // Copies boilerplate proxy config file with out locking.
        try (InputStream is = Files.newInputStream(orgCfg.toPath(), StandardOpenOption.READ)) {
            InputStreamReader reader = new InputStreamReader(is, Charset.defaultCharset());
            br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if(br != null){
                    br.close();
                }
            } catch (IOException e) { }
            try {
                if(bw != null) {
                    bw.close();
                }
            } catch (IOException e) { }
        }

        // Append assigned server config lines to temp file
        FileWriter fw = new FileWriter(tempCfg, true);
        fw.write(newLines.toString());
        fw.close();

        // This rename file method locks other threads utilizing this process until completed
        if(!tempCfg.renameTo(new File(localFile))){
            // Another thread is utilizing this method. Thrown Exception initiates another attempt at this process.
            throw new ProxyConfigLockedException();
        }

        return true;
    }

    public class ProxyConfigLockedException extends Exception { }
}
