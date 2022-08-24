package com.t8webs.enterprise.utils.UserServerUtil;

import com.t8webs.enterprise.dao.DbQuery;
import com.t8webs.enterprise.dto.Server;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IUserServerUtil {

    Server assignUserServer(String userId, String serverName) throws DbQuery.IntegrityConstraintViolationException;

    boolean unassignUserServer(Server server);

    boolean updateServerIp(String oldIp, String newIp) throws IOException;

    boolean deployBuild(String ipAddress, MultipartFile multipartFile) throws IOException;

    boolean updateBuildFile(String ipAddress, MultipartFile multipartFile) throws IOException;
}
