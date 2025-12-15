package com.example.mqtt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * MQTT发布者
 * 定期发送模拟传感器数据到指定主题
 */
public class MqttPublisher {
    private org.eclipse.paho.client.mqttv3.MqttClient client;
    private MqttConfig config;
    private Gson gson;
    private boolean connected = false;
    private int messageCount = 0;
    
    public MqttPublisher() {
        this.config = new MqttConfig();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    /**
     * 连接到MQTT Broker
     */
    public boolean connect() {
        try {
            String clientId = config.generateClientId("publisher");
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
                }
                
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // 发布者通常不需要处理接收到的消息
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 消息发送完成
                }
            });
            
            // 连接到Broker
            client.connect(options);
            connected = true;
            
            System.out.println("✓ 成功连接到MQTT Broker: " + config.getBrokerUrl());
            return true;
            
        } catch (MqttException e) {
            System.err.println("✗ 连接失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 发布消息
     */
    public boolean publish(String message, String topic) {
        if (!connected || client == null || !client.isConnected()) {
            System.err.println("✗ 未连接到Broker，无法发布消息");
            return false;
        }
        
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes("UTF-8"));
            mqttMessage.setQos(config.getQos());
            mqttMessage.setRetained(false);
            
            client.publish(topic, mqttMessage);
            System.out.println("→ 发布到主题 '" + topic + "':");
            System.out.println(message);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ 发布失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发布对象消息（自动转换为JSON）
     */
    public boolean publish(Object data, String topic) {
        String json = gson.toJson(data);
        return publish(json, topic);
    }
    
    /**
     * 发布到默认主题
     */
    public boolean publish(String message) {
        return publish(message, config.getTopic());
    }
    
    /**
     * 发布对象到默认主题
     */
    public boolean publish(Object data) {
        return publish(data, config.getTopic());
    }
    
    /**
     * 创建模拟传感器数据
     */
    private Map<String, Object> createSensorData() {
        messageCount++;
        Random random = new Random();
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", messageCount);
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        data.put("temperature", Math.round(random.nextDouble() * 10 + 20.0 * 100.0) / 100.0);
        data.put("humidity", Math.round(random.nextDouble() * 20 + 40.0 * 100.0) / 100.0);
        data.put("status", "normal");
        
        return data;
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
     * 主函数 - 演示发布消息
     */
    public static void main(String[] args) {
        MqttPublisher publisher = new MqttPublisher();
        
        // 连接到Broker
        if (!publisher.connect()) {
            return;
        }
        
        System.out.println("\n开始发布消息...");
        System.out.println("按 Ctrl+C 停止\n");
        
        // 添加关闭钩子，确保程序退出时断开连接
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\n中断信号收到，正在停止...");
            publisher.disconnect();
            System.out.println("程序结束");
        }));
        
        try {
            // 持续发布消息
            while (true) {
                // 创建并发布模拟数据
                Map<String, Object> sensorData = publisher.createSensorData();
                publisher.publish(sensorData);
                
                // 等待3秒
                Thread.sleep(3000);
            }
            
        } catch (InterruptedException e) {
            System.out.println("\n程序被中断");
        } finally {
            publisher.disconnect();
        }
    }
}
