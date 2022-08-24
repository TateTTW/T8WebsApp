package com.t8webs.enterprise.utils.DnsUtil;

public interface IDnsUtil {

    boolean deleteDnsRecord(String domainId);

    boolean renameDnsRecord(String domainName, String domainId);

    String addDnsRecord(String domainName);
}
