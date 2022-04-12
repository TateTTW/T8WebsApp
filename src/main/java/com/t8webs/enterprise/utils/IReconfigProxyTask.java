package com.t8webs.enterprise.utils;

import java.io.IOException;
import java.sql.SQLException;

public interface IReconfigProxyTask {
    boolean reconfigure() throws ClassNotFoundException, SQLException, IOException;

    boolean reloadService() throws InterruptedException;

    boolean rebuildCfgFile() throws ClassNotFoundException, IOException;
}
