package com.t8webs.enterprise.utils;

import org.springframework.web.multipart.MultipartFile;

public interface IClientServerUtil {
    boolean updateServerIp(String oldIp, String newIp);

    boolean deployBuild(String ipAddress, MultipartFile multipartFile);

    boolean editIpCfgFile(String newIpAddress);

    void resetLocalIpCfg();

    boolean replaceRemoteIpCfg(String ipAddress);

    String updateBuildFile(String ipAddress, MultipartFile multipartFile);
}
