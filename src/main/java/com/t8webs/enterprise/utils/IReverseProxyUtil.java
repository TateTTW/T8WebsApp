package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.dto.Server;

public interface IReverseProxyUtil {
    boolean addHostEntry(Server server);

    boolean deleteHostEntry(Server server);
}
