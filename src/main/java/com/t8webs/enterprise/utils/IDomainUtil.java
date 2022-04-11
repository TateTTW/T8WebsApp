package com.t8webs.enterprise.utils;

public interface IDomainUtil {

    boolean deleteDnsRecord(String domainId);

    boolean renameDnsRecord(String domainName, String domainId);

    String addDnsRecord(String domainName);
}
