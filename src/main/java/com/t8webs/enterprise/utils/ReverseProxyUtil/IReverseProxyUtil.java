package com.t8webs.enterprise.utils.ReverseProxyUtil;

public interface IReverseProxyUtil {
    boolean addHostEntry(String serverName, int vmid);

    boolean deleteHostEntry(String serverName);
}
