package com.t8webs.enterprise.utils.UserServerUtil;

import com.t8webs.enterprise.T8WebsApplication;
import com.t8webs.enterprise.dao.DbQuery;
import com.t8webs.enterprise.dao.AssignedServer.IAssignedServerDAO;
import com.t8webs.enterprise.dao.AvailableServer.IAvailableServerDAO;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.utils.SShUtil.ISShUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Properties;

@Component
public class UserServerUtil implements IUserServerUtil {

    @Autowired
    ISShUtil sshUtil;
    @Autowired
    IAvailableServerDAO availableServerDAO;
    @Autowired
    IAssignedServerDAO assignedServerDAO;

    private static final Properties PROPERTIES;
    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String CLIENT_ROOT_USER = PROPERTIES.getProperty("clientRootUser");
    private static final String CLIENT_ROOT_PASS = PROPERTIES.getProperty("clientRootPass");
    private static final String CLIENT_USER = PROPERTIES.getProperty("clientUser");
    private static final String CLIENT_PASS = PROPERTIES.getProperty("clientPass");
    private static final String LOCAL_IP_CFG = PROPERTIES.getProperty("localIpCfg");
    private static final String REMOTE_IP_CFG = PROPERTIES.getProperty("remoteIpCfg");
    private static final String STOP_BUILD_CMD = PROPERTIES.getProperty("stopBuildCmd");
    private static final String RUN_BUILD_CMD = PROPERTIES.getProperty("runBuildCmd");
    private static final String REMOTE_BUILD_FILE = PROPERTIES.getProperty("remoteBuildFile");

    @Override
    @Retryable(maxAttempts=20, value= DbQuery.IntegrityConstraintViolationException.class, backoff=@Backoff(delay = 100))
    public Server assignUserServer(String userId, String serverName) throws DbQuery.IntegrityConstraintViolationException {
        Server server = availableServerDAO.fetchAvailable();

        if(!server.isFound()){
            return server;
        }

        server.setUserId(userId);
        server.setName(serverName);

        // throws DbQuery.IntegrityConstraintViolationException when another user is assigned server first
        if(!(assignedServerDAO.save(server) && availableServerDAO.delete(server.getVmid()))){
            server.setFound(false);
        }

        return server;
    }

    public boolean unassignUserServer(Server server) {
        return assignedServerDAO.delete(server.getVmid()) && availableServerDAO.save(server);
    }

    @Override
    public boolean updateServerIp(String oldIp, String newIp) throws IOException {
        final String newLine = "      addresses: [" + newIp.trim() + "/24]";

        File tempFile = File.createTempFile("temp_ip_config", ".yaml");

        try (InputStream inputStream = getClass().getResourceAsStream(LOCAL_IP_CFG);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile)))
        {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("#addressesPlaceholder")){
                    line = newLine;
                }
                bw.write(line+"\n");
            }

            br.close();
            bw.close();

            return sshUtil.doSecureFileTransfer(CLIENT_ROOT_USER, CLIENT_ROOT_PASS, oldIp, tempFile.getPath(), REMOTE_IP_CFG);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            tempFile.delete();
        }
    }

    @Override
    public boolean deployBuild(String ipAddress, MultipartFile multipartFile) throws IOException {
        return (this.sshUtil.doSecureShellCmd(CLIENT_USER, CLIENT_PASS, ipAddress, STOP_BUILD_CMD)
                        && updateBuildFile(ipAddress, multipartFile)
                        && this.sshUtil.doSecureShellCmd(CLIENT_USER, CLIENT_PASS, ipAddress, RUN_BUILD_CMD));
    }

    @Override
    public boolean updateBuildFile(String ipAddress, MultipartFile multipartFile) throws IOException {
        if (multipartFile == null) {
            return false;
        }

        File tempFile = File.createTempFile("build_",".war");

        try {
            multipartFile.transferTo(tempFile);
            return sshUtil.doSecureFileTransfer(CLIENT_USER, CLIENT_PASS, ipAddress, tempFile.getPath(), REMOTE_BUILD_FILE);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            tempFile.delete();
        }
    }
}
