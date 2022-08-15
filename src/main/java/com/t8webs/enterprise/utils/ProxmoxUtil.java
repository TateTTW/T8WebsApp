package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.T8WebsApplication;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

@Component
@Profile("dev")
public class ProxmoxUtil implements IProxmoxUtil {

    private static final Properties PROPERTIES;
    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Unirest.config().verifySsl(false);
    }

    private static final String DOMAIN = PROPERTIES.getProperty("proxmoxIP");
    private static final String TOKEN = PROPERTIES.getProperty("promoxToken");
    private static final String CLONE_VM_URL = PROPERTIES.getProperty("cloneVmUrl");
    private static final String DELETE_VM_URL = PROPERTIES.getProperty("deleteVmUrl");
    private static final String START_VM_URL = PROPERTIES.getProperty("startVmUrl");
    private static final String SHUTDOWN_VM_URL = PROPERTIES.getProperty("shutdownVmUrl");
    private static final String REBOOT_VM_URL = PROPERTIES.getProperty("rebootVmUrl");
    private static final String GET_VM_IP_URL = PROPERTIES.getProperty("getVmIpUrl");
    private static final String GET_VM_DATA_URL = PROPERTIES.getProperty("getVmDataUrl");
    private static final String STATUS_VM_URL = PROPERTIES.getProperty("statusVmUrl");

    private static int minVmid = 120;

    public enum State {
        RUNNING,
        STOPPED
    }

    public enum TimeFrame {
        HOUR
    }

    @Override
    public boolean cloneVM(int vmid, String vmName) {
        if (vmid < ProxmoxUtil.minVmid) {
            return false;
        }

        String url = MessageFormat.format(ProxmoxUtil.CLONE_VM_URL, ProxmoxUtil.DOMAIN);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", ProxmoxUtil.TOKEN)
                .queryString("newid", vmid)
                .queryString("name", vmName)
                .queryString("storage", "local-lvm")
                .queryString("target", "pve")
                .queryString("full", "1")
                .asJson();

        return response.isSuccess();
    }

    @Override
    public String getVmStatus(int vmid) {
        if(vmid < minVmid){
            return "";
        }

        String url = MessageFormat.format(STATUS_VM_URL, DOMAIN, vmid);

        HttpResponse<JsonNode> response = Unirest.get(url)
                .header("Authorization", TOKEN)
                .asJson();

        if(response.isSuccess()){
            return response.getBody().getObject().getJSONObject("data").getString("status");
        }

        return "";
    }

    @Override
    public JSONObject getVmData(int vmid, TimeFrame timeFrame) {
        if(vmid < minVmid){
            return new JSONObject();
        }

        String url = MessageFormat.format(GET_VM_DATA_URL, DOMAIN, vmid, timeFrame.name().toLowerCase());

        HttpResponse<JsonNode> response = Unirest.get(url)
                .header("Authorization", TOKEN)
                .asJson();

        if(response.isSuccess()){
            return response.getBody().getObject();
        }

        return new JSONObject();
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

        String url = MessageFormat.format(STATUS_VM_URL, DOMAIN, vmid);

        HttpResponse<JsonNode> response = Unirest.get(url)
                .header("Authorization", TOKEN)
                .asJson();

        if(response.isSuccess()){
            return response.getBody().getObject().getJSONObject("data").has("lock");
        }

        return true;
    }

    @Override
    @Retryable(maxAttempts=50, value=InvalidVmStateException.class, backoff=@Backoff(delay = 5000))
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
    @Retryable(maxAttempts=30, value=InvalidVmStateException.class, backoff=@Backoff(delay = 5000))
    public boolean startVM(int vmid) throws InvalidVmStateException {
        if(vmid < minVmid){
            return false;
        }

        if(isVmLocked(vmid)){
            throw new InvalidVmStateException(vmid);
        }

        String url = MessageFormat.format(START_VM_URL, DOMAIN, vmid);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", TOKEN)
                .asJson();

        return response.isSuccess();
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

        String url = MessageFormat.format(SHUTDOWN_VM_URL, DOMAIN, vmid);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", TOKEN)
                .asJson();

        return response.isSuccess();
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

        String url = MessageFormat.format(REBOOT_VM_URL, DOMAIN, vmid);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", TOKEN)
                .asJson();

        return response.isSuccess();
    }

    @Override
    public boolean deleteVM(int vmid) {
        if(vmid < minVmid){
            return false;
        }

        if(isVmLocked(vmid) || isVmRunning(vmid)){
            return false;
        }

        String url = MessageFormat.format(DELETE_VM_URL, DOMAIN, vmid);

        HttpResponse<JsonNode> response = Unirest.delete(url)
                .header("Authorization", TOKEN)
                .asJson();

        return response.isSuccess();
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

        String getVMipURL = MessageFormat.format(GET_VM_IP_URL, DOMAIN, vmid);

        HttpResponse<JsonNode> response = Unirest.get(getVMipURL)
                .header("Authorization", TOKEN)
                .asJson();

        if(!response.isSuccess()){
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
                            return ipAddress.getString("ip-address").trim();
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
