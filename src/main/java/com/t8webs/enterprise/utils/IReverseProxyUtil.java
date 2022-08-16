package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.dto.Server;

public interface IReverseProxyUtil {
    boolean addHostEntry(String serverName, int vmid);

    boolean deleteHostEntry(String serverName);
}
