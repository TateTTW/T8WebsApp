package com.t8webs.enterprise.utils;

public interface IProxmoxUtil {
    boolean cloneVM(int vmid, String vmName);

    boolean deleteVm(int vmid);

    String getVmStatus(int vmid);

    boolean isVmRunning(int vmid);

    boolean isVmLocked(int vmid);

    boolean startVM(int vmid) throws ProxmoxUtil.LockedVirtualMachineException;

    boolean shutdownVM(int vmid);

    boolean rebootVM(int vmid);

    String getServerIp(int vmid) throws ProxmoxUtil.StoppedVirtualMachineException, ProxmoxUtil.IpAddressRequestException;
}
