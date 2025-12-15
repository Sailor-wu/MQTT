# MQTT Java 案例方案

这是一个完整的MQTT Java客户端示例项目，使用Eclipse Paho MQTT库实现消息的发布和订阅功能。

## 📋 项目简介

本项目演示了如何在Java应用中使用MQTT协议进行消息传递。适用于物联网（IoT）应用、实时数据传输、消息队列等场景。

## 🚀 功能特性

- ✅ **发布者（Publisher）**: 定时发送模拟传感器数据
- ✅ **订阅者（Subscriber）**: 实时接收并显示消息
- ✅ **通用客户端（MqttClient）**: 可复用的MQTT客户端类
- ✅ **配置管理**: 使用properties文件管理配置
- ✅ **自动重连**: 连接断开时自动重连
- ✅ **QoS支持**: 支持三种服务质量等级
- ✅ **JSON支持**: 使用Gson处理JSON消息
- ✅ **异常处理**: 完善的错误处理机制

## 📦 技术栈

- **Java**: 11+
- **Maven**: 3.6+
- **Eclipse Paho MQTT**: 1.2.5
- **Gson**: 2.10.1
- **SLF4J**: 2.0.9

## 🔧 项目结构

```
java/
├── pom.xml                                    # Maven配置文件
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── mqtt/
│       │               ├── MqttConfig.java       # 配置类
│       │               ├── MqttPublisher.java    # 发布者
│       │               ├── MqttSubscriber.java   # 订阅者
│       │               ├── MqttClient.java       # 通用客户端
│       │               └── MqttDemo.java         # 演示类
│       └── resources/
│           └── mqtt.properties                # 配置文件
└── README.md                                  # 项目文档
```

## 🛠️ 安装和运行

### 前置要求

- Java 11 或更高版本
- Maven 3.6 或更高版本

### 1. 安装依赖

在项目根目录（`java/`）下执行：

```powershell
mvn clean install
```

### 2. 配置MQTT连接

编辑 `src/main/resources/mqtt.properties` 文件：

```properties
# MQTT Broker地址
mqtt.broker=broker.emqx.io
mqtt.port=1883

# 主题
mqtt.topic=test/topic

# 客户端ID前缀
mqtt.client.id.prefix=mqtt_client

# 用户名和密码（可选）
mqtt.username=
mqtt.password=

# 其他配置
mqtt.keepalive=60
mqtt.qos=1
mqtt.clean.session=true
```

### 3. 运行示例

#### 方式一：使用Maven运行

**运行订阅者**：
```powershell
mvn exec:java -Dexec.mainClass="com.example.mqtt.MqttSubscriber"
```

**运行发布者**（在新终端）：
```powershell
mvn exec:java -Dexec.mainClass="com.example.mqtt.MqttPublisher"
```

**运行演示程序**：
```powershell
mvn exec:java -Dexec.mainClass="com.example.mqtt.MqttDemo"
```

#### 方式二：打包后运行

**1. 打包项目**：
```powershell
mvn clean package
```

**2. 运行订阅者**：
```powershell
java -cp target/mqtt-demo-1.0.0.jar com.example.mqtt.MqttSubscriber
```

**3. 运行发布者**（在新终端）：
```powershell
java -cp target/mqtt-demo-1.0.0.jar com.example.mqtt.MqttPublisher
```

#### 方式三：在IDE中运行

1. 将项目导入IDE（IntelliJ IDEA、Eclipse等）
2. 直接运行对应的main方法：
   - `MqttSubscriber.java` - 订阅者
   - `MqttPublisher.java` - 发布者
   - `MqttDemo.java` - 演示程序

## 💡 代码示例

### 发布消息

```java
import com.example.mqtt.MqttPublisher;
import java.util.HashMap;
import java.util.Map;

public class Example {
    public static void main(String[] args) {
        MqttPublisher publisher = new MqttPublisher();
        
        // 连接到Broker
        if (publisher.connect()) {
            // 发布文本消息
            publisher.publish("Hello MQTT!");
            
            // 发布对象消息（自动转换为JSON）
            Map<String, Object> data = new HashMap<>();
            data.put("temperature", 25.5);
            data.put("humidity", 60.0);
            data.put("status", "normal");
            publisher.publish(data);
            
            // 断开连接
            publisher.disconnect();
        }
    }
}
```

### 订阅消息

```java
import com.example.mqtt.MqttSubscriber;

public class Example {
    public static void main(String[] args) {
        MqttSubscriber subscriber = new MqttSubscriber();
        
        // 连接并订阅
        if (subscriber.connect()) {
            // 保持运行，接收消息
            subscriber.keepRunning();
        }
    }
}
```

### 使用通用客户端

```java
import com.example.mqtt.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Example {
    public static void main(String[] args) {
        MqttClient client = new MqttClient("my_client");
        
        // 连接
        if (client.connect()) {
            // 设置消息处理器
            client.setMessageHandler((topic, message) -> {
                String payload = new String(message.getPayload());
                System.out.println("收到: " + payload);
            });
            
            // 订阅
            client.subscribe();
            
            // 发布
            client.publish("Hello from Java!");
            
            // 等待接收消息
            Thread.sleep(5000);
            
            // 断开连接
            client.disconnect();
        }
    }
}
```

## 🌐 MQTT Broker选项

### 1. 公共测试Broker（默认）

- **EMQX**: `broker.emqx.io:1883`
- **Mosquitto**: `test.mosquitto.org:1883`

### 2. 本地Broker

使用Docker运行Mosquitto：

```powershell
docker run -it -p 1883:1883 eclipse-mosquitto
```

然后修改配置：
```properties
mqtt.broker=localhost
mqtt.port=1883
```

### 3. 企业级Broker

- **AWS IoT Core**
- **Azure IoT Hub**
- **Google Cloud IoT Core**
- **自建EMQX/Mosquitto服务器**

## 📊 消息格式

发布者发送的模拟传感器数据格式：

```json
{
  "id": 1,
  "timestamp": "2025-12-15T10:30:00.123",
  "temperature": 25.67,
  "humidity": 55.32,
  "status": "normal"
}
```

## ⚙️ QoS服务质量等级

| QoS | 名称 | 说明 |
|-----|------|------|
| 0 | At most once | 最多一次，消息可能丢失 |
| 1 | At least once | 至少一次，消息至少传递一次（默认） |
| 2 | Exactly once | 恰好一次，消息恰好传递一次 |

在 `mqtt.properties` 中设置：
```properties
mqtt.qos=1
```

## 🔒 安全配置

### 启用用户名/密码认证

在 `mqtt.properties` 中配置：

```properties
mqtt.username=your_username
mqtt.password=your_password
```

### 使用SSL/TLS

修改代码中的Broker URL：

```java
String brokerUrl = "ssl://" + config.getBroker() + ":" + config.getPort();
```

并配置SSL选项：

```java
MqttConnectOptions options = new MqttConnectOptions();
options.setSocketFactory(SSLSocketFactory.getDefault());
```

## ⚠️ 注意事项

1. **公共Broker**: 默认使用公共测试服务器，数据不安全，仅用于测试
2. **主题命名**: 使用唯一的主题名避免与他人冲突
3. **生产环境**: 建议部署私有MQTT Broker并启用认证
4. **防火墙**: 确保1883端口（或自定义端口）未被防火墙阻止
5. **客户端ID**: 每个客户端需要唯一的ID，代码已自动生成

## 🛠️ 故障排查

### Maven依赖下载失败

```powershell
# 清理并重新下载
mvn clean install -U
```

### 连接失败

1. 检查网络连接
2. 验证Broker地址和端口
3. 检查防火墙设置
4. 验证用户名和密码（如果需要）

### 收不到消息

1. 确认订阅的主题与发布的主题一致
2. 检查QoS设置
3. 确认订阅者在发布消息前已经连接

### 编码问题

项目使用UTF-8编码，确保IDE和Maven都配置为UTF-8：

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

## 📚 参考资料

- [MQTT官方网站](https://mqtt.org/)
- [Eclipse Paho Java文档](https://www.eclipse.org/paho/index.php?page=clients/java/index.php)
- [EMQX文档](https://www.emqx.io/docs/)
- [Maven官方文档](https://maven.apache.org/)

## 🌟 应用场景

1. **物联网设备通信**: 传感器数据采集和上报
2. **实时监控系统**: 设备状态监控和告警
3. **智能家居**: 设备控制和状态同步
4. **消息推送**: 实时消息通知系统
5. **车联网**: 车辆数据实时传输
6. **工业自动化**: 设备间的数据交换

## 📝 许可证

本项目仅用于学习和演示目的。

---

**快速开始**: 
1. 安装依赖: `mvn clean install`
2. 运行订阅者: `mvn exec:java -Dexec.mainClass="com.example.mqtt.MqttSubscriber"`
3. 运行发布者: `mvn exec:java -Dexec.mainClass="com.example.mqtt.MqttPublisher"`
4. 查看实时消息传递！
