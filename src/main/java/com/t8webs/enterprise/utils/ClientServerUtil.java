package com.t8webs.enterprise.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.t8webs.enterprise.T8WebsApplication;
import com.t8webs.enterprise.dao.DbQuery;
import com.t8webs.enterprise.dao.IAssignedServerDAO;
import com.t8webs.enterprise.dao.IAvailableServerDAO;
import com.t8webs.enterprise.dto.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Properties;

@Component
public class ClientServerUtil implements IClientServerUtil {

    @Autowired
    ISShUtils sshUtils;
    @Autowired
    IAvailableServerDAO availableServerDAO;
    @Autowired
    IAssignedServerDAO assignedServerDAO;

    private ObjectMapper mapper = new ObjectMapper();

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String rootUser = properties.getProperty("clientRootUser");
    private static String rootPass = properties.getProperty("clientRootPass");
    private static String clientUser = properties.getProperty("clientUser");
    private static String clientPass = properties.getProperty("clientPass");
    private static String localIpCfg = properties.getProperty("localIpCfg");
    private static String remoteIpCfg = properties.getProperty("remoteIpCfg");
    private static String stopBuildCmd = properties.getProperty("stopBuildCmd");
    private static String runBuildCmd = properties.getProperty("runBuildCmd");
    private static String remoteBuildFile = properties.getProperty("remoteBuildFile");

    @Override
    @Retryable(maxAttempts=20, value= DbQuery.IntegrityConstraintViolationException.class, backoff=@Backoff(delay = 100))
    public Server assignUserServer(String username, String serverName) throws DbQuery.IntegrityConstraintViolationException {
        Server server = availableServerDAO.fetchAvailable();

        if(!server.isFound()){
            return server;
        }

        server.setUsername(username);
        server.setName(serverName);

        // throws DbQuery.IntegrityConstraintViolationException when another user is assigned server first
        if(!assignedServerDAO.save(server)){
            return new Server();
        }

        availableServerDAO.delete(server.getVmid());

        return server;
    }

    @Override
    public boolean updateServerIp(String oldIp, String newIp) throws IOException {
        final String newLine = "      addresses: [" + newIp.trim() + "/24]";

        File tempFile = File.createTempFile("temp_ip_config", ".yaml");

        try (InputStream inputStream = getClass().getResourceAsStream(localIpCfg);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("#addressesPlaceholder")){
                    line = newLine;
                }
                bw.write(line+"\n");
            }

            br.close();
            bw.close();

            return sshUtils.doSecureFileTransfer(rootUser, rootPass, oldIp, tempFile.getPath(), remoteIpCfg);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            tempFile.delete();
        }
    }

    @Override
    public boolean deployBuild(String ipAddress, MultipartFile multipartFile) throws IOException {
        return (this.sshUtils.doSecureShellCmd(clientUser, clientPass, ipAddress, stopBuildCmd)
                        && updateBuildFile(ipAddress, multipartFile)
                        && this.sshUtils.doSecureShellCmd(clientUser, clientPass, ipAddress, runBuildCmd));
    }

    @Override
    public boolean updateBuildFile(String ipAddress, MultipartFile multipartFile) throws IOException {
        if (multipartFile == null) {
            return false;
        }

        File tempFile = File.createTempFile("build_",".war");

        try {
            multipartFile.transferTo(tempFile);
            return sshUtils.doSecureFileTransfer(clientUser, clientPass, ipAddress, tempFile.getPath(), remoteBuildFile);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            tempFile.delete();
        }
    }
}
