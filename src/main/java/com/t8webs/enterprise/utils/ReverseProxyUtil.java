package com.t8webs.enterprise.utils;

import com.jcraft.jsch.*;
import com.t8webs.enterprise.T8WebsApplication;
import com.t8webs.enterprise.dto.Server;

import java.io.*;
import java.util.Properties;

public class ReverseProxyUtil {

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

    private static String domain = properties.getProperty("domainName");
    private static String localFile = properties.getProperty("localProxyCfg");
    private static String remoteFile = properties.getProperty("remoteProxyCfg");
    private static String proxyUser = properties.getProperty("proxyUser");
    private static String proxyPass = properties.getProperty("proxyPass");
    private static String proxyIpAddress = properties.getProperty("proxyIpAddress");
    private static String reloadCommand = properties.getProperty("proxyReloadCmd");

    public static boolean configServer(Server server) {

        if(updateCfgFile(server)) {
            if(syncRemoteCfg()) {
                if(reloadService()){
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean syncRemoteCfg() {

        Session jschSession = null;
        ChannelSftp channelSftp = null;

        try {

            JSch jsch = new JSch();
            //jsch.setKnownHosts("/home/mkyong/.ssh/known_hosts");
            jschSession = jsch.getSession(proxyUser, proxyIpAddress, 22);
            jschSession.setConfig(config);
            jschSession.setPassword(proxyPass);

            jschSession.connect(10000);

            Channel sftp = jschSession.openChannel("sftp");

            sftp.connect(5000);

            channelSftp = (ChannelSftp) sftp;

            // transfer file from local to remote server
            channelSftp.put(localFile, remoteFile);

            // download file from remote server to local
            // channelSftp.get(remoteFile, localFile);

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

    private static boolean reloadService() {
        Session jschSession = null;
        Channel channel = null;

        try {

            JSch jsch = new JSch();

            jschSession = jsch.getSession(proxyUser, proxyIpAddress, 22);
            jschSession.setConfig(config);
            jschSession.setPassword(proxyPass);

            jschSession.connect(10000);

            channel = jschSession.openChannel("exec");
            ((ChannelExec)channel).setCommand(reloadCommand);

            channel.connect();

        } catch (JSchException e) {

            e.printStackTrace();
            return false;

        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
            if (channel != null){
                channel.disconnect();
            }
        }

        return true;
    }

    private static boolean updateCfgFile(Server server) {
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
                    String subStr = line.substring(line.indexOf("{"), line.indexOf("}"));
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
