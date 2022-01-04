package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.dto.Server;

public interface IReverseProxyUtil {
    boolean configServer(Server server);

    boolean syncRemoteCfg();

    boolean reloadService();

    boolean addUpdateServerName(Server server);
}
