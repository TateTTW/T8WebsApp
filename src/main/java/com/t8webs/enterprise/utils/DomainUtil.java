package com.t8webs.enterprise.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.t8webs.enterprise.T8WebsApplication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

@Service
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
     * @throws UnirestException
     */
    public static boolean removeDomain(String domainName) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.delete(properties.getProperty("domainDeleteURL") + domainName.trim())
                .header("Authorization", properties.getProperty("ssoKey"))
                .header("Content-Type", "application/json")
                .asJson();

        return response.getStatus() == 200;
    }

    /**
     * @param domainName to create
     * @return
     * @throws UnirestException
     */
    public static boolean createDomain(String domainName) throws UnirestException {
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