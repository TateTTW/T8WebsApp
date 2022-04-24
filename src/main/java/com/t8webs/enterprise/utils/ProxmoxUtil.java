package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.T8WebsApplication;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

@Component
public class ProxmoxUtil implements IProxmoxUtil {

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

    private static int minVmid = 120;

    public enum State {
        RUNNING,
        STOPPED
    }

    @Override
    public boolean cloneVM(int vmid, String vmName) {
        if (vmid < ProxmoxUtil.minVmid) {
            return false;
        }

        String url = MessageFormat.format(ProxmoxUtil.cloneVmUrl, ProxmoxUtil.domain);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", ProxmoxUtil.proxmoxToken)
                .queryString("newid", vmid)
                .queryString("name", vmName)
                .queryString("storage", "local-lvm")
                .queryString("target", "pve")
                .queryString("full", "1")
                .asJson();

        return response.getStatus() == 200;
    }

    @Override
    public String getVmStatus(int vmid) {
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

    @Override
    public boolean isVmRunning(int vmid) {
        return getVmStatus(vmid).equals("running");
    }

    @Override
    public boolean isVmLocked(int vmid) {
        if(vmid < minVmid){
            return true;
        }

        String url = MessageFormat.format(statusVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.get(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        if(response.getStatus() == 200){
            return response.getBody().getObject().getJSONObject("data").has("lock");
        }

        return true;
    }

    @Override
    @Retryable(maxAttempts=24, value=InvalidVmStateException.class, backoff=@Backoff(delay = 5000))
    public boolean reachedState(State expectedState, int vmid) throws InvalidVmStateException {

        if(isVmLocked(vmid)){
            throw new InvalidVmStateException(vmid);
        }

        if( (expectedState == State.RUNNING && getServerIp(vmid).isEmpty())
                || (expectedState == State.STOPPED && isVmRunning(vmid)) ) {
            throw new InvalidVmStateException(vmid);
        }

        return true;
    }

    @Override
    @Retryable(maxAttempts=30, value=InvalidVmStateException.class, backoff=@Backoff(delay = 4000))
    public boolean startVM(int vmid) throws InvalidVmStateException {
        if(vmid < minVmid){
            return false;
        }

        if(isVmLocked(vmid)){
            throw new InvalidVmStateException(vmid);
        }

        String url = MessageFormat.format(startVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        return response.getStatus() == 200;
    }

    @Override
    @Retryable(maxAttempts=6, value=InvalidVmStateException.class, backoff=@Backoff(delay = 5000))
    public boolean shutdownVM(int vmid) throws InvalidVmStateException {
        if(vmid < minVmid){
            return false;
        }

        if(isVmLocked(vmid)){
            throw new InvalidVmStateException(vmid);
        }

        String url = MessageFormat.format(shutdownVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        return response.getStatus() == 200;
    }

    @Override
    @Retryable(maxAttempts=6, value=InvalidVmStateException.class, backoff=@Backoff(delay = 5000))
    public boolean rebootVM(int vmid) throws InvalidVmStateException {
        if(vmid < minVmid){
            return false;
        }

        if(isVmLocked(vmid)){
            throw new InvalidVmStateException(vmid);
        }

        String url = MessageFormat.format(rebootVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        return response.getStatus() == 200;
    }

    @Override
    public boolean deleteVM(int vmid) {
        if(vmid < minVmid){
            return false;
        }

        if(isVmLocked(vmid) || isVmRunning(vmid)){
            return false;
        }

        String url = MessageFormat.format(deleteVmUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.delete(url)
                .header("Authorization", proxmoxToken)
                .asJson();

        return response.getStatus() == 200;
    }

    @Override
    @Retryable(maxAttempts=18, value=InvalidVmStateException.class, backoff=@Backoff(delay = 5000))
    public String getServerIp(int vmid) throws InvalidVmStateException {
        if(vmid < minVmid){
            return "";
        }

        if(!isVmRunning(vmid)){
            throw new InvalidVmStateException(vmid);
        }

        String getVMipURL = MessageFormat.format(getVmIpUrl, domain, vmid);

        HttpResponse<JsonNode> response = Unirest.get(getVMipURL)
                .header("Authorization", proxmoxToken)
                .asJson();

        if(response.getStatus() != 200){
            throw new InvalidVmStateException(vmid);
        }

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

        return "";
    }

    public class InvalidVmStateException extends Exception {
        public InvalidVmStateException(int vmid) {
            super("VM"+vmid+" is in the wrong state for this request.");
        }
    }
}
