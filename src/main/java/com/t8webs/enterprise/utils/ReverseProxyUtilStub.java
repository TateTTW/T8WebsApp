package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.dto.Server;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Profile("test")
public class ReverseProxyUtilStub implements IReverseProxyUtil {

    private static HashMap<String, String> hostEntries = new HashMap<>();

    @Override
    public boolean addHostEntry(Server server) {
        hostEntries.put(server.getName(), server.getIpAddress());
        return true;
    }

    @Override
    public boolean deleteHostEntry(Server server) {
        return hostEntries.remove(server.getName()) != null;
    }
}
