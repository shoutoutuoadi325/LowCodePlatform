# IoT Device Simulator

独立的IoT设备模拟器，通过MQTT协议与IoT平台通信。

## 功能特点

- 🔌 真实的MQTT通信，非直接Java调用
- 🎯 可独立运行，与平台完全分离
- 🔧 可扩展的设备框架
- 📊 自动上报遥测数据
- ⚡ 实时响应平台命令
- 💓 心跳机制保持连接

## 已实现的设备类型

### 温度传感器 (Temperature Sensor)

- **设备ID**: temp-sensor-001, temp-sensor-002
- **功能**:
  - 每10秒上报温度数据
  - 温度在目标值附近随机波动（±0.5℃）
  - 支持温度校准命令
  - 温度异常时自动告警（> 35℃ 或 < 0℃）

### 智能灯 (Smart Light)

- **设备ID**: light-001, light-002, light-003
- **功能**:
  - 开/关控制
  - 亮度调节（0-100%）
  - 颜色设置（十六进制格式）
  - 状态变化实时上报

## 快速开始

### 方式1：使用Docker（推荐）

在项目根目录执行：

```bash
docker-compose up device-simulator
```

### 方式2：本地运行

1. 确保MQTT Broker正在运行：
   ```bash
   docker run -d -p 1883:1883 eclipse-mosquitto:2.0
   ```

2. 编译项目：
   ```bash
   mvn clean package
   ```

3. 运行：
   ```bash
   java -jar target/device-simulator-1.0.0.jar
   ```

### 方式3：Maven运行

```bash
mvn spring-boot:run
```

## 配置

修改 `src/main/resources/application.yml`:

```yaml
mqtt:
  broker:
    url: tcp://localhost:1883  # MQTT Broker地址
```

## 添加新设备

### 步骤1：创建设备类

继承 `VirtualDevice` 并实现抽象方法：

```java
public class MyDevice extends VirtualDevice {
    
    public MyDevice(String deviceId, String deviceName, String brokerUrl) {
        super(deviceId, "my_device_type", deviceName, brokerUrl);
    }
    
    @Override
    protected Map<String, Object> getCurrentProperties() {
        // 返回设备当前属性
        Map<String, Object> props = new HashMap<>();
        props.put("myProperty", "value");
        return props;
    }
    
    @Override
    protected void handleCommand(String topic, String payload) {
        // 处理平台发来的命令
        // 解析payload，执行相应操作
    }
}
```

### 步骤2：在管理器中注册

在 `DeviceSimulatorManager.startSimulation()` 中添加：

```java
MyDevice device = new MyDevice("my-device-001", "我的设备", brokerUrl);
device.start();
devices.add(device);
```

## MQTT通信协议

### 设备上报主题

- `iot/devices/{deviceId}/telemetry` - 遥测数据
- `iot/devices/{deviceId}/event` - 事件上报
- `iot/devices/{deviceId}/online` - 上线通知
- `iot/devices/{deviceId}/heartbeat` - 心跳

### 设备订阅主题

- `iot/devices/{deviceId}/command` - 接收命令
- `iot/devices/{deviceId}/config` - 接收配置

## 消息格式示例

### 遥测数据 (Telemetry)

```json
{
  "messageType": "TELEMETRY",
  "deviceId": "temp-sensor-001",
  "timestamp": 1697456789000,
  "messageId": "uuid-123",
  "properties": {
    "temperature": 22.5,
    "status": "normal"
  }
}
```

### 命令 (Command)

```json
{
  "messageType": "COMMAND",
  "deviceId": "light-001",
  "timestamp": 1697456789000,
  "messageId": "uuid-456",
  "command": "turnOn",
  "parameters": {
    "brightness": 80
  }
}
```

### 事件 (Event)

```json
{
  "messageType": "EVENT",
  "deviceId": "temp-sensor-001",
  "timestamp": 1697456789000,
  "messageId": "uuid-789",
  "eventIdentifier": "temperatureAlert",
  "eventType": "warning",
  "eventData": {
    "temperature": 36.0,
    "threshold": 35.0
  }
}
```

## 日志查看

### Docker环境

```bash
docker-compose logs -f device-simulator
```

### 本地运行

日志会输出到控制台，级别为INFO。

## 故障排查

1. **无法连接MQTT Broker**
   - 检查MQTT Broker是否运行：`docker ps | grep mosquitto`
   - 检查网络连接
   - 验证配置的broker URL

2. **设备未上报数据**
   - 查看日志，确认设备已启动
   - 使用MQTT客户端工具（如MQTT.fx）监听主题

3. **命令无响应**
   - 确认设备已订阅命令主题
   - 检查命令格式是否正确
   - 查看设备日志

## 开发建议

- 遥测上报间隔可在 `VirtualDevice.start()` 中调整
- 每个设备类型应实现自己的状态模拟逻辑
- 建议为复杂设备实现状态机模式
- 生产环境应实现MQTT认证

## 技术栈

- Spring Boot 3.1.5
- Eclipse Paho MQTT Client
- Jackson (JSON处理)
- Lombok

## 许可证

MIT License
