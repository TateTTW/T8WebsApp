package com.t8webs.enterprise.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface IClientServerUtil {
    boolean updateServerIp(String oldIp, String newIp);

    boolean editIpCfgFile(String newIpAddress);

    void resetLocalIpCfg();

    boolean replaceRemoteIpCfg(String ipAddress);

    boolean updateBuildFile(String ipAddress, MultipartFile multipartFile);
}
