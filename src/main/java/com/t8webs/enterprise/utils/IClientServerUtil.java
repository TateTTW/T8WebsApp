package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.dto.Server;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;

public interface IClientServerUtil {

    Server assignUserServer(String username, String serverName) throws SQLException, IOException, ClassNotFoundException;

    boolean updateServerIp(String oldIp, String newIp);

    boolean deployBuild(String ipAddress, MultipartFile multipartFile);

    boolean updateBuildFile(String ipAddress, MultipartFile multipartFile);
}
