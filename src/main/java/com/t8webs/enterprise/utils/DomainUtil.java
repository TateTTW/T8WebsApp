package com.t8webs.enterprise.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.T8WebsApplication;
import com.t8webs.enterprise.dao.IAssignedServerDAO;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

@Component
@Profile("dev")
public class DomainUtil implements IDomainUtil {

    @Autowired
    IAssignedServerDAO assignedServerDAO;

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String authEmail = properties.getProperty("authEmail");
    private static String authKey = properties.getProperty("authKey");
    private static String zoneId = properties.getProperty("zoneId");
    private static String domainName = properties.getProperty("domainName");
    private static String addDomainUrl = properties.getProperty("addDomainUrl");
    private static String dnsRecordUrl = properties.getProperty("dnsRecordUrl");

    @Override
    public boolean deleteDnsRecord(String domainId) {
        String url = MessageFormat.format(dnsRecordUrl, zoneId, domainId);

        HttpResponse<JsonNode> response = Unirest.delete(url)
                .header("X-Auth-Email", authEmail)
                .header("X-Auth-Key", authKey)
                .header("Content-Type", "application/json")
                .asJson();

        return response.getStatus() == 200;
    }

    @Override
    public boolean renameDnsRecord(String domainName, String domainId) {

        String url = MessageFormat.format(dnsRecordUrl, zoneId, domainId);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("name", domainName.trim());

        HttpResponse<JsonNode> response = Unirest.patch(url)
                .header("X-Auth-Email", authEmail)
                .header("X-Auth-Key", authKey)
                .header("Content-Type", "application/json")
                .body(jsonNode.toPrettyString())
                .asJson();

        return response.getStatus() == 200;
    }

    @Override
    public String addDnsRecord(String domainName) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("content", DomainUtil.domainName);
        jsonNode.put("name", domainName.trim());
        jsonNode.put("type", "CNAME");
        jsonNode.put("ttl", 1);
        jsonNode.put("proxied", true);

        String url = MessageFormat.format(addDomainUrl, zoneId);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("X-Auth-Email", authEmail)
                .header("X-Auth-Key", authKey)
                .header("Content-Type", "application/json")
                .body(jsonNode.toPrettyString())
                .asJson();

        if(response.getStatus() == 200
                && response.getBody() != null
                && response.getBody().getObject() != null
                && response.getBody().getObject().has("result")
                && response.getBody().getObject().getJSONObject("result") != null
                && response.getBody().getObject().getJSONObject("result").has("id")
                && response.getBody().getObject().getJSONObject("result").getString("id") != null)
        {
            return response.getBody().getObject().getJSONObject("result").getString("id");
        }

        return "";
    }
}
