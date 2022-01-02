package com.t8webs.enterprise.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.T8WebsApplication;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.io.IOException;
import java.util.Properties;

public class DomainUtil {

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param domainName to remove
     * @return
     */
    public static boolean removeDomain(String domainName) {
        HttpResponse<JsonNode> response = Unirest.delete(properties.getProperty("domainDeleteURL") + domainName.trim())
                .header("Authorization", properties.getProperty("ssoKey"))
                .header("Content-Type", "application/json")
                .asJson();

        return response.getStatus() == 200;
    }

    /**
     * @param domainName to create
     * @return
     */
    public static boolean createDomain(String domainName) {
        ObjectMapper mapper = new ObjectMapper();

        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("data", properties.getProperty("domainIP"));
        jsonNode.put("name", domainName.trim());
        jsonNode.put("type", "A");
        jsonNode.put("ttl", 6000);
        arrayNode.add(jsonNode);

        HttpResponse<JsonNode> response = Unirest.patch(properties.getProperty("domainPatchURL"))
                .header("Authorization", properties.getProperty("ssoKey"))
                .header("Content-Type", "application/json")
                .body(arrayNode.toPrettyString())
                .asJson();

        return response.getStatus() == 200;
    }
}
