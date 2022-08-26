package com.t8webs.enterprise.utils.ProxmoxUtil;

import kong.unirest.json.JSONObject;

public interface IProxmoxUtil {
    boolean cloneVM(int vmid, String vmName);

    String getVmStatus(int vmid);

    JSONObject getVmData(int vmid, ProxmoxUtil.TimeFrame timeFrame);

    JSONObject getNodeData(ProxmoxUtil.TimeFrame timeFrame);

    boolean isVmRunning(int vmid);

    boolean isVmLocked(int vmid);

    boolean reachedState(ProxmoxUtil.State expectedState, int vmid) throws ProxmoxUtil.InvalidVmStateException;

    boolean startVM(int vmid) throws ProxmoxUtil.InvalidVmStateException;

    boolean shutdownVM(int vmid) throws ProxmoxUtil.InvalidVmStateException;

    boolean rebootVM(int vmid) throws ProxmoxUtil.InvalidVmStateException;

    boolean deleteVM(int vmid) throws ProxmoxUtil.InvalidVmStateException;

    String getServerIp(int vmid) throws ProxmoxUtil.InvalidVmStateException;
}
