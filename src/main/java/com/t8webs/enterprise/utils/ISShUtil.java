package com.t8webs.enterprise.utils;

public interface ISShUtil {

    boolean doSecureShellCmd(String user, String pass, String ipAddress, String cmd);

    boolean doSecureFileTransfer(String user, String pass, String ipAddress, String localFile, String remoteFile);
}
