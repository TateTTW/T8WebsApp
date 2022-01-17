package com.t8webs.enterprise.utils;

import java.io.IOException;
import java.sql.SQLException;

public interface IReverseProxyUtil {
    boolean reconfigure() throws ClassNotFoundException, SQLException, ReverseProxyUtil.ProxyConfigLockedException, IOException;

    boolean syncRemoteCfg();

    boolean reloadService();

    boolean deleteLocalCfg();

    boolean buildLocalCfgFile() throws SQLException, IOException, ClassNotFoundException, ReverseProxyUtil.ProxyConfigLockedException;
}
