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

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String postHostEntryURL = properties.getProperty("postHostEntryURL");
    private static final String deleteHostEntryURL = properties.getProperty("deleteHostEntryURL");
    private static final String dataplaneAuthKey = properties.getProperty("dataplaneAuthKey");
    private static final String domainName = properties.getProperty("domainName");
    private static final String subdomainFormat = "{0}.{1}";

    @Override
    public boolean addHostEntry(Server server) {
        String subdomain = MessageFormat.format(subdomainFormat, server.getName().toLowerCase().trim(), domainName.trim());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("key", subdomain);
        jsonNode.put("value", String.valueOf(server.getVmid()));

        HttpResponse<JsonNode> response = Unirest.post(postHostEntryURL)
                .header("Authorization", dataplaneAuthKey)
                .header("Content-Type", "application/json")
                .body(jsonNode.toPrettyString())
                .asJson();

        return response.isSuccess();
    }

    @Override
    public boolean deleteHostEntry(Server server) {
        String subdomain = MessageFormat.format(subdomainFormat, server.getName().toLowerCase().trim(), domainName.trim());
        String url = MessageFormat.format(deleteHostEntryURL, subdomain);

        HttpResponse<JsonNode> response = Unirest.delete(url)
                .header("Authorization", dataplaneAuthKey)
                .header("Content-Type", "application/json")
                .asJson();

        return response.isSuccess();
    }
}
