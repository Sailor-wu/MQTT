package com.example.mqtt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT订阅者
 * 订阅指定主题并接收消息
 */
public class MqttSubscriber {
    private org.eclipse.paho.client.mqttv3.MqttClient client;
    private MqttConfig config;
    private Gson gson;
    private boolean connected = false;
    private int messageCount = 0;
    
    public MqttSubscriber() {
        this.config = new MqttConfig();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    /**
     * 连接到MQTT Broker并订阅主题
     */
    public boolean connect() {
        try {
            String clientId = config.generateClientId("subscriber");
            System.out.println("正在连接到 " + config.getBrokerUrl() + "...");
            System.out.println("客户端ID: " + clientId);
            
            // 创建MQTT客户端
            client = new org.eclipse.paho.client.mqttv3.MqttClient(config.getBrokerUrl(), clientId, new MemoryPersistence());
            
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
                    System.out.println("自动重连中...");
                }
                
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    handleMessage(topic, message);
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 订阅者通常不需要处理消息发送
                }
            });
            
            // 连接到Broker
            client.connect(options);
            connected = true;
            
            System.out.println("✓ 成功连接到MQTT Broker: " + config.getBrokerUrl());
            
            // 订阅主题
            subscribe(config.getTopic());
            
            return true;
            
        } catch (MqttException e) {
            System.err.println("✗ 连接失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 订阅主题
     */
    public boolean subscribe(String topic) {
        if (!connected || client == null || !client.isConnected()) {
            System.err.println("✗ 未连接到Broker，无法订阅");
            return false;
        }
        
        try {
            System.out.println("正在订阅主题: " + topic);
            client.subscribe(topic, config.getQos());
            System.out.println("✓ 订阅成功");
            System.out.println("等待接收消息...\n");
            return true;
            
        } catch (MqttException e) {
            System.err.println("✗ 订阅失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 处理接收到的消息
     */
    private void handleMessage(String topic, MqttMessage message) {
        messageCount++;
        
        try {
            String payload = new String(message.getPayload(), "UTF-8");
            
            System.out.println("← [" + messageCount + "] 收到消息 (主题: " + topic + ")");
            System.out.println("  QoS: " + message.getQos());
            
            // 尝试解析JSON
            try {
                JsonObject jsonObject = gson.fromJson(payload, JsonObject.class);
                System.out.println("  内容:");
                System.out.println(gson.toJson(jsonObject));
            } catch (JsonSyntaxException e) {
                // 不是JSON格式，直接显示文本
                System.out.println("  内容: " + payload);
            }
            
            System.out.println();  // 空行分隔
            
        } catch (Exception e) {
            System.err.println("✗ 处理消息时出错: " + e.getMessage());
        }
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
                System.out.println("\n总共接收了 " + messageCount + " 条消息");
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
     * 保持运行
     */
    public void keepRunning() {
        try {
            // 保持程序运行，直到被中断
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("\n程序被中断");
        }
    }
    
    /**
     * 主函数 - 演示订阅消息
     */
    public static void main(String[] args) {
        MqttSubscriber subscriber = new MqttSubscriber();
        
        // 连接到Broker并订阅
        if (!subscriber.connect()) {
            return;
        }
        
        System.out.println("按 Ctrl+C 停止\n");
        
        // 添加关闭钩子，确保程序退出时断开连接
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\n中断信号收到，正在停止...");
            subscriber.disconnect();
            System.out.println("程序结束");
        }));
        
        // 保持运行
        subscriber.keepRunning();
    }
}
