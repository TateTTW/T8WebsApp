package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.T8WebsApplication;
import com.t8webs.enterprise.dto.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

@Component
public class ReverseProxyUtil implements IReverseProxyUtil {

    @Autowired
    ISShUtils sshUtils;

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
    private static String localFile = properties.getProperty("localProxyCfg");
    private static String remoteFile = properties.getProperty("remoteProxyCfg");
    private static String proxyUser = properties.getProperty("proxyUser");
    private static String proxyPass = properties.getProperty("proxyPass");
    private static String proxyIpAddress = properties.getProperty("proxyIpAddress");
    private static String reloadCommand = properties.getProperty("proxyReloadCmd");

    @Override
    public boolean configServer(Server server) {

        if(addUpdateServerName(server)) {
            if(syncRemoteCfg()) {
                if(reloadService()){
                    return true;
                }
            }
        }

        return false;
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
    public boolean addUpdateServerName(Server server) {
        String newLine = "        use_backend " + server.getVmid() + " if { hdr(host) -i " + server.getName().trim() + "." + domain + " }";

        File orgFile  = new File(localFile);
        File tempFile = null;

        BufferedReader br = null;
        BufferedWriter bw = null;

        try {
            tempFile = File.createTempFile("temp_haproxy", ".cfg");

            br = new BufferedReader(new FileReader(orgFile));
            bw = new BufferedWriter(new FileWriter(tempFile));

            String line;
            boolean found = false;
            while ((line = br.readLine()) != null) {
                if (line.contains("use_backend " + server.getVmid() + " if ")){
                    line = newLine;
                    found = true;
                }
                bw.write(line+"\n");
            }

            if(!found){
                bw.write(newLine+"\n");
            }

        } catch (Exception e) {
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

        if(orgFile != null){
            orgFile.delete();
        }

        if(tempFile != null){
            tempFile.renameTo(orgFile);
        }

        return true;
    }

}
