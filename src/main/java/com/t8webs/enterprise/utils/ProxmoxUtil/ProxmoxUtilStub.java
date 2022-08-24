package com.t8webs.enterprise.utils.ProxmoxUtil;

import kong.unirest.json.JSONObject;
import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Profile("test")
public class ProxmoxUtilStub implements IProxmoxUtil {

    private static HashMap<Integer, VirtualMachine> virtualMachines = new HashMap<>();

    @Override
    public boolean cloneVM(int vmid, String vmName) {
        VirtualMachine vm = new VirtualMachine(vmid, vmName);
        virtualMachines.put(vmid, vm);
        return true;
    }

    @Override
    public String getVmStatus(int vmid) {
        String status = "";

        VirtualMachine vm = virtualMachines.get(vmid);
        if (vm != null) {
            status = vm.getStatus().name().toLowerCase();
        }

        return status;
    }

    @Override
    public JSONObject getVmData(int vmid, ProxmoxUtil.TimeFrame timeFrame) {
        return null;
    }

    @Override
    public boolean isVmRunning(int vmid) {
        return getVmStatus(vmid).equals(ProxmoxUtil.State.RUNNING.name().toLowerCase());
    }

    @Override
    public boolean isVmLocked(int vmid) {
        return false;
    }

    @Override
    public boolean reachedState(ProxmoxUtil.State expectedState, int vmid) {
        VirtualMachine vm = virtualMachines.get(vmid);
        return (vm != null && vm.getStatus() == expectedState);
    }

    @Override
    public boolean startVM(int vmid) {
        VirtualMachine vm = virtualMachines.get(vmid);
        if (vm != null) {
            vm.setStatus(ProxmoxUtil.State.RUNNING);
        }

        return vm != null;
    }

    @Override
    public boolean shutdownVM(int vmid) {
        VirtualMachine vm = virtualMachines.get(vmid);
        if (vm != null) {
            vm.setStatus(ProxmoxUtil.State.STOPPED);
        }

        return vm != null;
    }

    @Override
    public boolean rebootVM(int vmid) {
        VirtualMachine vm = virtualMachines.get(vmid);
        if (vm != null) {
            vm.setStatus(ProxmoxUtil.State.RUNNING);
        }

        return vm != null;
    }

    @Override
    public boolean deleteVM(int vmid) {
        return virtualMachines.remove(vmid) != null;
    }

    @Override
    public String getServerIp(int vmid) {
        VirtualMachine vm = virtualMachines.get(vmid);
        return vm != null ? vm.getIpAddress() : "";
    }

    @Data
    private class VirtualMachine {
        private final int vmid;
        private final String name;
        private String ipAddress = "192.168.90.40";
        private ProxmoxUtil.State status = ProxmoxUtil.State.STOPPED;

        public VirtualMachine(int vmid, String name) {
            this.vmid = vmid;
            this.name = name;
        }
    }
}
