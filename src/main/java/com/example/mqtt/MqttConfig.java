package com.example.mqtt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MQTT配置类
 * 从配置文件加载MQTT连接参数
 */
public class MqttConfig {
    private static final String CONFIG_FILE = "mqtt.properties";
    
    private String broker;
    private int port;
    private String topic;
    private String clientIdPrefix;
    private String username;
    private String password;
    private int keepAlive;
    private int qos;
    private boolean cleanSession;
    
    // 默认配置
    private static final String DEFAULT_BROKER = "broker.emqx.io";
    private static final int DEFAULT_PORT = 1883;
    private static final String DEFAULT_TOPIC = "test/topic";
    private static final String DEFAULT_CLIENT_ID_PREFIX = "mqtt_client";
    private static final int DEFAULT_KEEP_ALIVE = 60;
    private static final int DEFAULT_QOS = 1;
    private static final boolean DEFAULT_CLEAN_SESSION = true;
    
    public MqttConfig() {
        // 设置默认值
        this.broker = DEFAULT_BROKER;
        this.port = DEFAULT_PORT;
        this.topic = DEFAULT_TOPIC;
        this.clientIdPrefix = DEFAULT_CLIENT_ID_PREFIX;
        this.keepAlive = DEFAULT_KEEP_ALIVE;
        this.qos = DEFAULT_QOS;
        this.cleanSession = DEFAULT_CLEAN_SESSION;
        this.username = "";
        this.password = "";
        
        // 尝试从配置文件加载
        loadConfig();
    }
    
    /**
     * 从配置文件加载配置
     */
    private void loadConfig() {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
                
                // 读取配置
                this.broker = props.getProperty("mqtt.broker", DEFAULT_BROKER);
                this.port = Integer.parseInt(props.getProperty("mqtt.port", String.valueOf(DEFAULT_PORT)));
                this.topic = props.getProperty("mqtt.topic", DEFAULT_TOPIC);
                this.clientIdPrefix = props.getProperty("mqtt.client.id.prefix", DEFAULT_CLIENT_ID_PREFIX);
                this.username = props.getProperty("mqtt.username", "");
                this.password = props.getProperty("mqtt.password", "");
                this.keepAlive = Integer.parseInt(props.getProperty("mqtt.keepalive", String.valueOf(DEFAULT_KEEP_ALIVE)));
                this.qos = Integer.parseInt(props.getProperty("mqtt.qos", String.valueOf(DEFAULT_QOS)));
                this.cleanSession = Boolean.parseBoolean(props.getProperty("mqtt.clean.session", String.valueOf(DEFAULT_CLEAN_SESSION)));
                
                System.out.println("✓ 配置文件加载成功");
            } else {
                System.out.println("⚠ 未找到配置文件，使用默认配置");
            }
        } catch (IOException e) {
            System.out.println("⚠ 读取配置文件失败，使用默认配置: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("⚠ 配置文件格式错误，使用默认配置: " + e.getMessage());
        }
    }
    
    /**
     * 获取MQTT Broker URL
     */
    public String getBrokerUrl() {
        return "tcp://" + broker + ":" + port;
    }
    
    // Getters
    public String getBroker() {
        return broker;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public String getClientIdPrefix() {
        return clientIdPrefix;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public int getKeepAlive() {
        return keepAlive;
    }
    
    public int getQos() {
        return qos;
    }
    
    public boolean isCleanSession() {
        return cleanSession;
    }
    
    /**
     * 生成唯一的客户端ID
     */
    public String generateClientId(String type) {
        return clientIdPrefix + "_" + type + "_" + System.currentTimeMillis();
    }
}
