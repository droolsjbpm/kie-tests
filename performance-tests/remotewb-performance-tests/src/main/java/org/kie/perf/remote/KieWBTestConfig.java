package org.kie.perf.remote;

import java.util.Properties;

import org.kie.perf.TestConfig;

public class KieWBTestConfig extends TestConfig {

    public static final String DEPLOYMENT_ID = "org.kie.perf:workbench-assets:1.0.0-SNAPSHOT";

    protected String username;

    protected String password;

    protected String host;

    protected int port;

    protected int remotingPort;

    protected String name;

    protected String remoteAPI;

    protected boolean sslEnabled;

    protected String kieSessionQueue;

    protected String kieTaskQueue;

    protected String kieResponseQueue;

    protected String connectionFactory;

    protected String keystoreLocation;

    protected String keystorePassword;

    protected KieWBTestConfig() {

    }

    public static KieWBTestConfig getInstance() {
        if (tc == null || !(tc instanceof KieWBTestConfig)) {
            tc = new KieWBTestConfig();
            try {
                tc.loadProperties();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return (KieWBTestConfig) tc;
    }

    @Override
    public Properties loadProperties() throws Exception {
        super.loadProperties();

        username = System.getProperty("workbench.username");
        properties.put("workbench.username", username);

        password = System.getProperty("workbench.password");
        properties.put("workbench.password", password);

        host = System.getProperty("workbench.host");
        properties.put("workbench.host", host);

        port = Integer.valueOf(System.getProperty("workbench.port"));
        properties.put("workbench.port", port);

        remotingPort = Integer.valueOf(System.getProperty("workbench.remotingPort"));
        properties.put("workbench.remotingPort", remotingPort);

        name = System.getProperty("workbench.name");
        properties.put("workbench.name", name);

        remoteAPI = System.getProperty("remoteAPI");
        properties.put("remoteAPI", remoteAPI);
        addTag(remoteAPI);

        kieSessionQueue = System.getProperty("workbench.jms.queue.kieSession");
        properties.put("workbench.jms.queue.kieSession", kieSessionQueue);

        kieTaskQueue = System.getProperty("workbench.jms.queue.kieTask");
        properties.put("workbench.jms.queue.kieTask", kieTaskQueue);

        kieResponseQueue = System.getProperty("workbench.jms.queue.kieResponse");
        properties.put("workbench.jms.queue.kieResponse", kieResponseQueue);

        sslEnabled = Boolean.valueOf(System.getProperty("workbench.jms.sslEnabled"));
        properties.put("workbench.jms.sslEnabled", sslEnabled);

        connectionFactory = System.getProperty("workbench.jms.connectionFactory");
        properties.put("workbench.jms.connectionFactory", connectionFactory);

        keystoreLocation = System.getProperty("workbench.jms.ssl.keystoreLocation");
        properties.put("workbench.jms.ssl.keystoreLocation", keystoreLocation);

        keystorePassword = System.getProperty("workbench.jms.ssl.keystorePassword");
        properties.put("workbench.jms.ssl.keystorePassword", keystorePassword);

        return properties;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getRemotingPort() {
        return remotingPort;
    }

    public void setRemotingPort(int remotingPort) {
        this.remotingPort = remotingPort;
    }

    public String getRemoteAPI() {
        return remoteAPI;
    }

    public void setRemoteAPI(String remoteAPI) {
        this.remoteAPI = remoteAPI;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String getKieSessionQueue() {
        return kieSessionQueue;
    }

    public void setKieSessionQueue(String kieSessionQueue) {
        this.kieSessionQueue = kieSessionQueue;
    }

    public String getKieTaskQueue() {
        return kieTaskQueue;
    }

    public void setKieTaskQueue(String kieTaskQueue) {
        this.kieTaskQueue = kieTaskQueue;
    }

    public String getKieResponseQueue() {
        return kieResponseQueue;
    }

    public void setKieResponseQueue(String kieResponseQueue) {
        this.kieResponseQueue = kieResponseQueue;
    }

    public String getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public int getMessagingPort() {
        return isSslEnabled() ? 5446 : 5445;
    }

    public String getKeystoreLocation() {
        return keystoreLocation;
    }

    public void setKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationUrl() {
        return "http://" + host + ":" + port + "/" + name;
    }

}
