package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.dao.IAssignedServerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class SShUtilStub implements ISShUtil {
    @Autowired
    IAssignedServerDAO assignedServerDAO;

    @Override
    public boolean doSecureShellCmd(String user, String pass, String ipAddress, String cmd) {
        return true;
    }

    @Override
    public boolean doSecureFileTransfer(String user, String pass, String ipAddress, String localFile, String remoteFile) {
        return true;
    }
}
