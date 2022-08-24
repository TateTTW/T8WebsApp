package com.t8webs.enterprise.utils.ReverseProxyUtil;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Profile("test")
public class ReverseProxyUtilStub implements IReverseProxyUtil {

    private static HashMap<String, Integer> hostEntries = new HashMap<>();

    @Override
    public boolean addHostEntry(String serverName, int vmid) {
        hostEntries.put(serverName, vmid);
        return true;
    }

    @Override
    public boolean deleteHostEntry(String serverName) {
        return hostEntries.remove(serverName) != null;
    }
}
