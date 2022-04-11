package com.t8webs.enterprise.utils;

import com.t8webs.enterprise.T8WebsApplication;
import com.t8webs.enterprise.dao.IAssignedServerDAO;
import com.t8webs.enterprise.dto.Server;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;


public class ReconfigProxyTask implements IReconfigProxyTask, Runnable {

    ISShUtils sshUtils;
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

    private static String domain = properties.getProperty("domainName");
    private static String localFile = properties.getProperty("localProxyCfg");
    private static String remoteFile = properties.getProperty("remoteProxyCfg");
    private static String proxyUser = properties.getProperty("proxyUser");
    private static String proxyPass = properties.getProperty("proxyPass");
    private static String proxyIpAddress = properties.getProperty("proxyIpAddress");
    private static String reloadCommand = properties.getProperty("proxyReloadCmd");

    public ReconfigProxyTask(ISShUtils sshUtils, IAssignedServerDAO assignedServerDAO) {
        this.sshUtils = sshUtils;
        this.assignedServerDAO = assignedServerDAO;
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        this.reconfigure();
    }

    @Override
    public boolean reconfigure() {
        try {
            return (rebuildCfgFile() && reloadService());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean reloadService() throws InterruptedException {
        boolean success = sshUtils.doSecureShellCmd(proxyUser, proxyPass, proxyIpAddress, reloadCommand);
        // Random amount of time to allow the Reverse Proxy to reload its configuration
        Thread.sleep(15000);

        return success;
    }

    @Override
    public boolean rebuildCfgFile() throws SQLException, ClassNotFoundException, IOException {
        // Create String containing a configuration line for each assigned server.
        StringBuilder newLines = new StringBuilder();

        List<Server> assignedServers = assignedServerDAO.fetchAll();
        for(Server server: assignedServers){
            newLines.append("        use_backend " + server.getVmid() + " if { hdr(host) -i " + server.getName().trim() + "." + domain + " }\n");
        }

        // Copy base proxy configuration to a temporary file
        File tempCfg = File.createTempFile("haproxy_",".cfg");
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempCfg));
        BufferedReader br = null;

        try {
            InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(localFile), Charset.defaultCharset());
            br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if(br != null){
                    br.close();
                }
            } catch (IOException e) { }
            try {
                if(bw != null) {
                    bw.close();
                }
            } catch (IOException e) { }
        }
        // Append assigned server config lines to temp file
        FileWriter fw = new FileWriter(tempCfg, true);
        fw.write(newLines.toString());
        fw.close();
        // Transfer file to the reverse proxy
        return sshUtils.doSecureFileTransfer(proxyUser, proxyPass, proxyIpAddress, tempCfg.getPath(), remoteFile);
    }
}
