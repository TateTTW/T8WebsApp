package com.t8webs.enterprise.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.T8WebsApplication;
import com.t8webs.enterprise.dto.Server;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

@Component
@Profile("dev")
public class ReverseProxyUtil implements IReverseProxyUtil {

    private static final Properties PROPERTIES;
    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String POST_HOST_ENTRY_URL = PROPERTIES.getProperty("postHostEntryURL");
    private static final String DELETE_HOST_ENTRY_URL = PROPERTIES.getProperty("deleteHostEntryURL");
    private static final String DATAPLANE_AUTH_KEY = PROPERTIES.getProperty("dataplaneAuthKey");
    private static final String DOMAIN_NAME = PROPERTIES.getProperty("domainName");
    private static final String SUBDOMAIN_FORMAT = "{0}.{1}";

    @Override
    public boolean addHostEntry(Server server) {
        String subdomain = MessageFormat.format(SUBDOMAIN_FORMAT, server.getName().toLowerCase().trim(), DOMAIN_NAME.trim());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("key", subdomain);
        jsonNode.put("value", String.valueOf(server.getVmid()));

        HttpResponse<JsonNode> response = Unirest.post(POST_HOST_ENTRY_URL)
                .header("Authorization", DATAPLANE_AUTH_KEY)
                .header("Content-Type", "application/json")
                .body(jsonNode.toPrettyString())
                .asJson();

        return response.isSuccess();
    }

    @Override
    public boolean deleteHostEntry(Server server) {
        String subdomain = MessageFormat.format(SUBDOMAIN_FORMAT, server.getName().toLowerCase().trim(), DOMAIN_NAME.trim());
        String url = MessageFormat.format(DELETE_HOST_ENTRY_URL, subdomain);

        HttpResponse<JsonNode> response = Unirest.delete(url)
                .header("Authorization", DATAPLANE_AUTH_KEY)
                .header("Content-Type", "application/json")
                .asJson();

        return response.isSuccess();
    }
}
