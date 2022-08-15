package com.t8webs.enterprise.utils;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Profile("test")
public class DomainUtilStub implements IDomainUtil {

    private static int dnsId = 1;
    private static HashMap<String, String> dnsRecords = new HashMap<>();

    @Override
    public boolean deleteDnsRecord(String domainId) {
        return dnsRecords.remove(domainId) != null;
    }

    @Override
    public boolean renameDnsRecord(String domainName, String domainId) {
        if (dnsRecords.get(domainId) != null) {
           dnsRecords.put(domainId, domainName);
           return true;
        }
        return false;
    }

    @Override
    public String addDnsRecord(String domainName) {
        String dnsId = String.valueOf(this.dnsId++);
        dnsRecords.put(dnsId, domainName);
        return dnsId;
    }
}
