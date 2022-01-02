package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.T8WebsApplication;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

public class ProxmoxUtil {

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Unirest.config().verifySsl(false);
    }

    private static String domain = properties.getProperty("proxmoxIP");
    private static String proxmoxToken = properties.getProperty("promoxToken");
    private static String cloneVmUrl = properties.getProperty("cloneVmUrl");
    private static String deleteVmUrl = properties.getProperty("deleteVmUrl");
    private static String startVmUrl = properties.getProperty("startVmUrl");
    private static String shutdownVmUrl = properties.getProperty("shutdownVmUrl");
    private static String rebootVmUrl = properties.getProperty("rebootVmUrl");
    private static String getVmIpUrl = properties.getProperty("getVmIpUrl");
    private static String statusVmUrl = properties.getProperty("statusVmUrl");

    private static int minVmid = 110;


    static public String createServer(int vmid, String vmName) {
        if(cloneVM(vmid, vmName)){
            if(startVM(vmid)) {
                int attempts = 0;
                String ipAddress = "";
                do {
                    attempts++;
                    try {
                        Thread.sleep(10000);
                        ipAddress = getServerIp(vmid);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (ipAddress.trim().isEmpty() && attempts < 10);

                if(!ipAddress.trim().isEmpty()){
                    return ipAddress;
                }
            }
        }

        return "";
    }

    static private boolean cloneVM(int vmid, String vmName) {
        if(vmid < minVmid){
            return false;
        }

        String url = MessageFormat.format(cloneVmUrl, domain);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", proxmoxToken)
                .queryString("newid", vmid)
                .queryString("name", vmName)
                .queryString("storage", "local-lvm")
                .queryString("target", "pve")
                .queryString("full", "1")
                .asJson();

        return response.getStatus() == 200;
    }

    static public boolean deleteVm(int vmid) {
        if(vmid < minVmid){
            return false;
        }

        String url = MessageFormat.format(deleteVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.delete(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        return response.getStatus() == 200;
    }

    static public String getVmStatus(int vmid) {
        if(vmid < minVmid){
            return "";
        }

        String url = MessageFormat.format(statusVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.get(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        if(response.getStatus() == 200){
            return response.getBody().getObject().getJSONObject("data").getString("status");
        }

        return "";
    }

    static public boolean startVM(int vmid) {
        if(vmid < minVmid){
            return false;
        }

        String url = MessageFormat.format(startVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        return response.getStatus() == 200;
    }

    static public boolean shutdownVM(int vmid) {
        if(vmid < minVmid){
            return false;
        }

        String url = MessageFormat.format(shutdownVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        return response.getStatus() == 200;
    }

    static public boolean rebootVM(int vmid) {
        if(vmid < minVmid){
            return false;
        }

        String url = MessageFormat.format(rebootVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        return response.getStatus() == 200;
    }

    static public String getServerIp(int vmid) {
        if(vmid < minVmid){
            return "";
        }

        String getVMipURL = MessageFormat.format(getVmIpUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.get(getVMipURL)
                .header("Authorization", proxmoxToken)
                .asJson();

        if(response.getStatus() == 200){
            try {
                JSONArray results = response.getBody().getObject().getJSONObject("data").getJSONArray("result");
                for(int i=0; i<results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    if(result.getString("name").equals("ens18")){
                        JSONArray ipAddresses = result.getJSONArray("ip-addresses");
                        for(int ii=0; ii<ipAddresses.length(); ii++){
                            JSONObject ipAddress = ipAddresses.getJSONObject(ii);
                            if(ipAddress.getString("ip-address-type").equals("ipv4") && ipAddress.getString("ip-address") != null){
                                return ipAddress.getString("ip-address");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }

}
