package com.t8webs.enterprise.utils;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class SShUtils implements ISShUtils {

    private static Properties config;
    static {
        config = new Properties();
        config.put("StrictHostKeyChecking", "no");
    }

    @Override
    public boolean doSecureShellCmd(String user, String pass, String ipAddress, String cmd) {
        Session jschSession = null;
        Channel channel = null;

        try {

            JSch jsch = new JSch();

            jschSession = jsch.getSession(user, ipAddress, 22);
            jschSession.setConfig(config);
            jschSession.setPassword(pass);

            jschSession.connect(10000);

            channel = jschSession.openChannel("exec");
            ((ChannelExec)channel).setCommand(cmd);

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

    @Override
    public boolean doSecureFileTransfer(String user, String pass, String ipAddress, String localFile, String remoteFile) {

        Session jschSession = null;
        ChannelSftp sftpChannel = null;

        try {
            JSch jsch = new JSch();

            jschSession = jsch.getSession(user, ipAddress, 22);
            jschSession.setConfig(config);
            jschSession.setPassword(pass);

            jschSession.connect(10000);

            sftpChannel = (ChannelSftp)jschSession.openChannel("sftp");
            sftpChannel.connect(5000);

            // transfer file from local to remote server
            sftpChannel.put(localFile, remoteFile);

            // download file from remote server to local
            // channelSftp.get(remoteFile, localFile);

            sftpChannel.exit();
        } catch (SftpException | JSchException e) {

            e.printStackTrace();
            return false;

        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
        }

        return true;
    }
}
