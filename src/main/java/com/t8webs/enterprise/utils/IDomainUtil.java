package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.dto.Server;

import java.io.IOException;
import java.sql.SQLException;

public interface IDomainUtil {

    boolean deleteDnsRecord(String domainId);

    boolean renameDnsRecord(String domainName, String domainId);

    String addDnsRecord(String domainName);
}
