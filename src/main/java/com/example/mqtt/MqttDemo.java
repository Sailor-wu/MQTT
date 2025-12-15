package com.example.mqtt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * MQTT演示类
 * 演示通用客户端的使用
 */
public class MqttDemo {
    
    public static void main(String[] args) {
        System.out.println("=== MQTT通用客户端演示 ===\n");
        
        // 创建客户端
        MqttClient client = new MqttClient("demo");
        
        // 连接
        if (!client.connect()) {
            return;
        }
        
        // 设置自定义消息处理器
        client.setMessageHandler((topic, message) -> {
            try {
                String payload = new String(message.getPayload(), "UTF-8");
                System.out.println("\n[自定义处理器] 收到消息:");
                System.out.println("  主题: " + topic);
                System.out.println("  内容: " + payload);
                
                // 尝试解析JSON并提取温度
                try {
                    JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
                    if (json.has("temperature")) {
                        double temp = json.get("temperature").getAsDouble();
                        System.out.println("  温度: " + temp + "°C");
                    }
                } catch (Exception e) {
                    // 不是JSON格式，忽略
                }
            } catch (Exception e) {
                System.err.println("处理消息出错: " + e.getMessage());
            }
        });
        
        // 订阅主题
        client.subscribe();
        
        // 发布几条测试消息
        System.out.println("\n发布测试消息...\n");
        Random random = new Random();
        
        for (int i = 0; i < 3; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", i + 1);
            data.put("message", "测试消息 #" + (i + 1));
            data.put("temperature", Math.round(random.nextDouble() * 10 + 20.0 * 100.0) / 100.0);
            
            client.publish(data);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        // 等待接收消息
        System.out.println("\n等待5秒接收消息...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("被中断");
        }
        
        // 断开连接
        client.disconnect();
        System.out.println("\n演示结束");
    }
}
