package com.t8webs.enterprise.utils.SShUtil;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class SShUtilStub implements ISShUtil {

    @Override
    public boolean doSecureShellCmd(String user, String pass, String ipAddress, String cmd) {
        return true;
    }

    @Override
    public boolean doSecureFileTransfer(String user, String pass, String ipAddress, String localFile, String remoteFile) {
        return true;
    }
}
