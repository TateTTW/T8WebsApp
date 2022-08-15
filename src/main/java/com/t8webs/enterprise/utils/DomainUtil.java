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

    private static final Properties PROPERTIES;
    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String AUTH_EMAIL = PROPERTIES.getProperty("authEmail");
    private static final String AUTH_KEY = PROPERTIES.getProperty("authKey");
    private static final String ZONE_ID = PROPERTIES.getProperty("zoneId");
    private static final String DOMAIN_NAME = PROPERTIES.getProperty("domainName");
    private static final String ADD_DOMAIN_URL = PROPERTIES.getProperty("addDomainUrl");
    private static final String DNS_RECORD_URL = PROPERTIES.getProperty("dnsRecordUrl");

    @Override
    public boolean deleteDnsRecord(String domainId) {
        String url = MessageFormat.format(DNS_RECORD_URL, ZONE_ID, domainId);

        HttpResponse<JsonNode> response = Unirest.delete(url)
                .header("X-Auth-Email", AUTH_EMAIL)
                .header("X-Auth-Key", AUTH_KEY)
                .header("Content-Type", "application/json")
                .asJson();

        return response.isSuccess();
    }

    @Override
    public boolean renameDnsRecord(String domainName, String domainId) {
        String url = MessageFormat.format(DNS_RECORD_URL, ZONE_ID, domainId);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("name", domainName.trim());

        HttpResponse<JsonNode> response = Unirest.patch(url)
                .header("X-Auth-Email", AUTH_EMAIL)
                .header("X-Auth-Key", AUTH_KEY)
                .header("Content-Type", "application/json")
                .body(jsonNode.toPrettyString())
                .asJson();

        return response.isSuccess();
    }

    @Override
    public String addDnsRecord(String domainName) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("content", DomainUtil.DOMAIN_NAME);
        jsonNode.put("name", domainName.trim());
        jsonNode.put("type", "CNAME");
        jsonNode.put("ttl", 1);
        jsonNode.put("proxied", true);

        String url = MessageFormat.format(ADD_DOMAIN_URL, ZONE_ID);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("X-Auth-Email", AUTH_EMAIL)
                .header("X-Auth-Key", AUTH_KEY)
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
