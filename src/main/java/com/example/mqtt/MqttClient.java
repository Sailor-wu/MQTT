package com.example.mqtt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.function.BiConsumer;

/**
 * MQTT通用客户端类
 * 可用于发布和订阅消息
 */
public class MqttClient {
    private org.eclipse.paho.client.mqttv3.MqttClient client;
    private MqttConfig config;
    private Gson gson;
    private boolean connected = false;
    private BiConsumer<String, MqttMessage> messageHandler;
    
    public MqttClient(String clientType) {
        this.config = new MqttConfig();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    /**
     * 连接到MQTT Broker
     */
    public boolean connect() {
        try {
            String clientId = config.generateClientId("client");
            System.out.println("正在连接到 " + config.getBrokerUrl() + "...");
            System.out.println("客户端ID: " + clientId);
            
            // 创建MQTT客户端
            client = new org.eclipse.paho.client.mqttv3.MqttClient(
                config.getBrokerUrl(), 
                clientId, 
                new MemoryPersistence()
            );
            
            // 配置连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(config.isCleanSession());
            options.setKeepAliveInterval(config.getKeepAlive());
            options.setAutomaticReconnect(true);
            
            // 设置用户名和密码（如果需要）
            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                options.setUserName(config.getUsername());
                if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                    options.setPassword(config.getPassword().toCharArray());
                }
            }
            
            // 设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    connected = false;
                    System.out.println("✗ 连接断开: " + cause.getMessage());
                }
                
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if (messageHandler != null) {
                        messageHandler.accept(topic, message);
                    } else {
                        // 默认处理
                        defaultMessageHandler(topic, message);
                    }
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("  消息已发送");
                }
            });
            
            // 连接到Broker
            client.connect(options);
            connected = true;
            
            System.out.println("✓ 连接成功 (Client ID: " + clientId + ")");
            return true;
            
        } catch (MqttException e) {
            System.err.println("✗ 连接失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 默认消息处理器
     */
    private void defaultMessageHandler(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), "UTF-8");
            System.out.println("← 收到消息 (主题: " + topic + ")");
            System.out.println("  内容: " + payload);
        } catch (Exception e) {
            System.err.println("✗ 处理消息出错: " + e.getMessage());
        }
    }
    
    /**
     * 设置自定义消息处理器
     */
    public void setMessageHandler(BiConsumer<String, MqttMessage> handler) {
        this.messageHandler = handler;
    }
    
    /**
     * 发布消息
     */
    public boolean publish(String message, String topic, int qos) {
        if (!connected || client == null || !client.isConnected()) {
            System.err.println("✗ 未连接，无法发布消息");
            return false;
        }
        
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes("UTF-8"));
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(false);
            
            client.publish(topic, mqttMessage);
            System.out.println("→ 发布消息到 '" + topic + "': " + message);
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ 发布失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发布消息（使用默认主题和QoS）
     */
    public boolean publish(String message) {
        return publish(message, config.getTopic(), config.getQos());
    }
    
    /**
     * 发布对象消息
     */
    public boolean publish(Object data, String topic, int qos) {
        String json = gson.toJson(data);
        return publish(json, topic, qos);
    }
    
    /**
     * 发布对象消息（使用默认主题和QoS）
     */
    public boolean publish(Object data) {
        return publish(data, config.getTopic(), config.getQos());
    }
    
    /**
     * 订阅主题
     */
    public boolean subscribe(String topic, int qos) {
        if (!connected || client == null || !client.isConnected()) {
            System.err.println("✗ 未连接，无法订阅");
            return false;
        }
        
        try {
            System.out.println("正在订阅主题: " + topic);
            client.subscribe(topic, qos);
            System.out.println("✓ 订阅成功");
            return true;
            
        } catch (MqttException e) {
            System.err.println("✗ 订阅失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 订阅主题（使用默认主题和QoS）
     */
    public boolean subscribe() {
        return subscribe(config.getTopic(), config.getQos());
    }
    
    /**
     * 取消订阅
     */
    public boolean unsubscribe(String topic) {
        try {
            if (client != null && client.isConnected()) {
                client.unsubscribe(topic);
                System.out.println("✓ 已取消订阅: " + topic);
                return true;
            }
        } catch (MqttException e) {
            System.err.println("✗ 取消订阅失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("已断开连接");
            }
            if (client != null) {
                client.close();
            }
        } catch (MqttException e) {
            System.err.println("断开连接时出错: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return connected && client != null && client.isConnected();
    }
}
