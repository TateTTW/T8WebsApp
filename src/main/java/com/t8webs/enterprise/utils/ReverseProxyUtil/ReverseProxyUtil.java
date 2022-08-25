package com.t8webs.enterprise.utils.ReverseProxyUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.T8WebsApplication;
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

    private static final String PROXY_IP = PROPERTIES.getProperty("proxyIP");
    private static final String DATAPLANE_AUTH_KEY = PROPERTIES.getProperty("dataplaneAuthKey");
    private static final String DOMAIN_NAME = PROPERTIES.getProperty("domainName");
    private static final String SUBDOMAIN_FORMAT = "{0}.{1}";

    @Override
    public boolean addHostEntry(String serverName, int vmid) {
        final String subdomain = MessageFormat.format(SUBDOMAIN_FORMAT, serverName.toLowerCase().trim(), DOMAIN_NAME.trim());
        final String url = MessageFormat.format(ApiUrls.POST_HOST_ENTRY, PROXY_IP);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("key", subdomain);
        jsonNode.put("value", String.valueOf(vmid));

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", DATAPLANE_AUTH_KEY)
                .header("Content-Type", "application/json")
                .body(jsonNode.toPrettyString())
                .asJson();

        return response.isSuccess();
    }

    @Override
    public boolean deleteHostEntry(String serverName) {
        final String subdomain = MessageFormat.format(SUBDOMAIN_FORMAT, serverName.toLowerCase().trim(), DOMAIN_NAME.trim());
        final String url = MessageFormat.format(ApiUrls.DELETE_HOST_ENTRY, PROXY_IP, subdomain);

        HttpResponse<JsonNode> response = Unirest.delete(url)
                .header("Authorization", DATAPLANE_AUTH_KEY)
                .header("Content-Type", "application/json")
                .asJson();

        return response.isSuccess();
    }

    private static class ApiUrls {
        private static final String POST_HOST_ENTRY = "{0}/v2/services/haproxy/runtime/maps_entries?map=hosts.map&force_sync=true";
        private static final String DELETE_HOST_ENTRY = "{0}/v2/services/haproxy/runtime/maps_entries/{1}?map=hosts.map&force_sync=true";
    }
}
