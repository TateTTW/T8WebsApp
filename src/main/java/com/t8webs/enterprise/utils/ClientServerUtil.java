package com.t8webs.enterprise.utils;

import com.jcraft.jsch.*;
import com.t8webs.enterprise.T8WebsApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

@Component
public class ClientServerUtil implements IClientServerUtil {

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Properties config;
    static {
        config = new Properties();
        config.put("StrictHostKeyChecking", "no");
    }

    private static String rootUser = properties.getProperty("clientRootUser");
    private static String rootPass = properties.getProperty("clientRootPass");
    private static String clientUser = properties.getProperty("clientUser");
    private static String clientPass = properties.getProperty("clientPass");
    private static String localIpCfg = properties.getProperty("localIpCfg");
    private static String localIpCfgBk = properties.getProperty("localIpCfgBk");
    private static String remoteIpCfg = properties.getProperty("remoteIpCfg");
    private static String deployCmd = properties.getProperty("deployCmd");
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
        Session jschSession = null;
        ChannelSftp channelSftp = null;

        try {

            JSch jsch = new JSch();
            jschSession = jsch.getSession(rootUser, ipAddress, 22);
            jschSession.setConfig(config);
            jschSession.setPassword(rootPass);

            jschSession.connect(10000);

            Channel sftp = jschSession.openChannel("sftp");

            sftp.connect(5000);

            channelSftp = (ChannelSftp) sftp;

            // transfer file from local to remote server
            channelSftp.put(localIpCfg, remoteIpCfg);

            channelSftp.exit();

        } catch (SftpException | JSchException e) {

            e.printStackTrace();
            return false;

        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
        }

        return true;
    }

    @Override
    public boolean updateBuildFile(String ipAddress, MultipartFile multipartFile) {

        if(multipartFile == null) {
            return false;
        }

        File tempFile = null;

        Session jschSession = null;
        ChannelSftp channelSftp = null;

        try {

            tempFile = File.createTempFile("build_",".war");

            tempFile.getName();

            multipartFile.transferTo(tempFile);

            JSch jsch = new JSch();
            jschSession = jsch.getSession(clientUser, ipAddress, 22);
            jschSession.setConfig(config);
            jschSession.setPassword(clientPass);

            jschSession.connect(10000);

            Channel sftp = jschSession.openChannel("sftp");

            sftp.connect(5000);

            channelSftp = (ChannelSftp) sftp;

            // transfer file from local to remote server
            channelSftp.put(tempFile.getPath(), remoteBuildFile);

            channelSftp.exit();

        } catch (SftpException | JSchException | IOException e) {

            e.printStackTrace();
            return false;

        } finally {

            if(tempFile != null) {
                tempFile.delete();
            }

            if (jschSession != null) {
                jschSession.disconnect();
            }
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
        }

        return true;
    }
}
