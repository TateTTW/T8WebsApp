package com.t8webs.enterprise.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.T8WebsApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Properties;

@Component
public class ClientServerUtil implements IClientServerUtil {

    @Autowired
    ISShUtils sshUtils;

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
    private static String localIpCfgBk = properties.getProperty("localIpCfgBk");
    private static String remoteIpCfg = properties.getProperty("remoteIpCfg");
    private static String stopBuildCmd = properties.getProperty("stopBuildCmd");
    private static String runBuildCmd = properties.getProperty("runBuildCmd");
    private static String remoteBuildFile = properties.getProperty("remoteBuildFile");

    @Override
    public boolean updateServerIp(String oldIp, String newIp) {
        boolean success = false;

        if(editIpCfgFile(newIp)){
            success = replaceRemoteIpCfg(oldIp);
            resetLocalIpCfg();
        }

        return success;
    }

    @Override
    public boolean deployBuild(String ipAddress, MultipartFile multipartFile) {
        boolean success = false;
        String buildName = updateBuildFile(ipAddress, multipartFile);

        if(!buildName.isEmpty()) {
            if(this.sshUtils.doSecureShellCmd(clientUser, clientPass, ipAddress, stopBuildCmd)){
                success = this.sshUtils.doSecureShellCmd(clientUser, clientPass, ipAddress, MessageFormat.format(runBuildCmd, buildName));
            }
        }
        return success;
    }

    @Override
    public boolean editIpCfgFile(String newIpAddress) {
        final String newLine = "      addresses: [" + newIpAddress.trim() + "/24]";

        File orgFile  = new File(localIpCfg);
        File tempFile = null;

        BufferedReader br = null;
        BufferedWriter bw = null;

        try {
            tempFile = File.createTempFile("temp_ip_config", ".yaml");

            br = new BufferedReader(new FileReader(orgFile));
            bw = new BufferedWriter(new FileWriter(tempFile));

            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("#addressesPlaceholder")){
                    line = newLine;
                }
                bw.write(line+"\n");
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

    @Override
    public void resetLocalIpCfg() {
        Path destPath = new File(localIpCfg).toPath();
        Path srcPath = new File(localIpCfgBk).toPath();
        try {
            Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean replaceRemoteIpCfg(String ipAddress) {
        return sshUtils.doSecureFileTransfer(rootUser, rootPass, ipAddress, localIpCfg, remoteIpCfg);
    }

    @Override
    public String updateBuildFile(String ipAddress, MultipartFile multipartFile) {

        boolean success = false;

        if(multipartFile == null) {
            return "";
        }

        File tempFile = null;

        try {

            tempFile = File.createTempFile("build_",".war");

            multipartFile.transferTo(tempFile);

            success = sshUtils.doSecureFileTransfer(clientUser, clientPass, ipAddress, tempFile.getPath(), remoteBuildFile);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(tempFile != null) {
                tempFile.delete();
            }
        }

        if(success && tempFile != null) {
            return tempFile.getName();
        } else {
            return "";
        }
    }
}
