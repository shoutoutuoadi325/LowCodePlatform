# IoT平台设备架构重构说明

## 概述

本次重构实现了基于MQTT协议的真实设备通信架构，替代了之前硬编码的Java设备模拟器。新架构遵循了IoT行业标准实践，支持任意设备类型的接入。

## 架构特点

### 1. 设备元模型 (Device Metamodel)

引入了设备类型的元数据建模，包括：

- **DeviceTypeModel**: 设备类型定义
  - 属性定义 (DevicePropertyDefinition): 定义设备的属性（如温度、湿度、亮度等）
  - 操作定义 (DeviceOperationDefinition): 定义设备支持的操作（如开关、设置温度等）
  - 事件定义 (DeviceEventDefinition): 定义设备可能产生的事件（如告警、状态变化等）

这种元模型设计使得：
- ✅ 可以动态定义新的设备类型，无需修改代码
- ✅ 设备类型的属性、操作和事件都是可配置的
- ✅ 为未来接入真实协议和设备提供了统一的抽象层

### 2. MQTT通信协议

采用标准的MQTT协议进行设备与平台的通信：

#### 主题设计

**设备 → 平台 (Device to Platform)**
```
iot/devices/{deviceId}/telemetry   - 遥测数据（属性上报）
iot/devices/{deviceId}/event       - 事件上报
iot/devices/{deviceId}/response    - 命令响应
iot/devices/{deviceId}/register    - 设备注册
iot/devices/{deviceId}/online      - 设备上线
iot/devices/{deviceId}/heartbeat   - 心跳
```

**平台 → 设备 (Platform to Device)**
```
iot/devices/{deviceId}/command     - 控制命令
iot/devices/{deviceId}/config      - 配置更新
```

#### 消息格式

所有消息都使用JSON格式，包含统一的基础字段：
```json
{
  "messageType": "TELEMETRY|EVENT|COMMAND|...",
  "deviceId": "设备ID",
  "timestamp": 1697456789000,
  "messageId": "消息唯一ID"
}
```

### 3. 独立的设备模拟器

创建了 `device-simulator` 独立项目，特点：

- **可独立运行**: 完全独立于IoT平台的进程
- **真实的通信**: 通过MQTT与平台通信，而不是直接调用Java方法
- **可扩展框架**: 
  - `VirtualDevice` 抽象基类提供固定的设备框架
  - 具体设备类型（如 `TemperatureSensorDevice`, `SmartLightDevice`）继承基类
  - 新设备类型只需实现抽象方法即可

#### 已实现的设备类型

1. **温度传感器** (`TemperatureSensorDevice`)
   - 定时上报温度数据
   - 温度在目标值附近随机波动
   - 支持温度校准命令
   - 异常温度自动告警

2. **智能灯** (`SmartLightDevice`)
   - 支持开关控制
   - 支持亮度调节 (0-100)
   - 支持颜色设置
   - 状态变化实时上报

### 4. MQTT Broker

使用 Eclipse Mosquitto 作为MQTT消息代理：
- 端口1883: MQTT协议
- 端口9001: WebSocket支持
- 数据持久化
- 生产环境建议启用认证

## 系统组件

```
┌─────────────────────────────────────────────────────────────┐
│                     IoT Platform                            │
│  ┌───────────────────┐         ┌──────────────────┐        │
│  │  Device Service   │◄────────┤  Scene Service   │        │
│  │  - 元模型管理      │         │  - 场景引擎       │        │
│  │  - MQTT通信       │         └──────────────────┘        │
│  │  - 设备管理       │                                      │
│  └─────────┬─────────┘                                      │
│            │                                                 │
└────────────┼─────────────────────────────────────────────────┘
             │ MQTT
             ▼
    ┌────────────────┐
    │   Mosquitto    │  MQTT Broker
    │   (Eclipse)    │
    └────────┬───────┘
             │ MQTT
             ▼
┌────────────────────────────────────────────────────────────┐
│              Device Simulator (独立进程)                    │
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │ Temperature      │  │  Smart Light     │  ...          │
│  │ Sensor           │  │  Device          │               │
│  └──────────────────┘  └──────────────────┘               │
└────────────────────────────────────────────────────────────┘
```

## 核心概念

### 固定的框架

- MQTT通信协议
- 消息格式规范
- 设备元模型结构
- VirtualDevice基类

### 可变的部分

- 设备类型定义（通过元模型配置）
- 具体设备实现（继承VirtualDevice）
- 设备数量和实例
- 设备属性和行为

## API接口

### 设备类型管理

```http
# 创建设备类型
POST /api/device-types
Content-Type: application/json

{
  "typeIdentifier": "temperature_sensor",
  "typeName": "温度传感器",
  "category": "sensor",
  "protocol": "MQTT",
  "properties": [...],
  "operations": [...],
  "events": [...]
}

# 获取所有设备类型
GET /api/device-types

# 根据标识符获取设备类型
GET /api/device-types/identifier/{typeIdentifier}
```

### 设备管理

```http
# 创建设备（原有API）
POST /api/devices

# 控制设备（通过MQTT发送命令）
POST /api/devices/{deviceId}/control
Content-Type: application/json

{
  "action": "turnOn",
  "parameters": {
    "brightness": 80
  }
}

# 获取设备状态（从数据库读取最新上报的属性）
GET /api/devices/{deviceId}/state
```

## 运行方式

### 使用Docker Compose（推荐）

```bash
# 构建并启动所有服务
docker-compose up --build

# 后台运行
docker-compose up -d --build

# 查看日志
docker-compose logs -f device-simulator
```

### 本地开发

1. **启动MQTT Broker**
   ```bash
   docker run -d -p 1883:1883 -p 9001:9001 eclipse-mosquitto:2.0
   ```

2. **启动Device Service**
   ```bash
   cd backend/device-service
   mvn spring-boot:run
   ```

3. **启动Device Simulator**
   ```bash
   cd device-simulator
   mvn spring-boot:run
   ```

## 扩展指南

### 添加新设备类型

1. **定义设备元模型** (通过API或数据库)
2. **实现设备模拟器类** (继承 VirtualDevice)
   ```java
   public class MyNewDevice extends VirtualDevice {
       @Override
       protected Map<String, Object> getCurrentProperties() {
           // 返回设备当前属性
       }
       
       @Override
       protected void handleCommand(String topic, String payload) {
           // 处理平台发来的命令
       }
   }
   ```
3. **在DeviceSimulatorManager中注册**

### 接入真实设备

真实设备只需：
1. 实现MQTT客户端
2. 按照约定的主题和消息格式通信
3. 定期上报遥测数据
4. 监听并响应命令

无需修改平台代码！

## 与现有系统的兼容性

- ✅ 保持了原有的REST API接口
- ✅ 数据库模型向后兼容
- ✅ 场景服务可以继续通过Device Service控制设备
- ✅ 前端无需修改（通过同样的API通信）

## 未来改进方向

1. **设备认证和授权**: 实现基于证书或Token的设备认证
2. **设备影子**: 实现设备影子服务，缓存设备状态
3. **规则引擎**: 基于设备事件的规则引擎
4. **多协议支持**: 除MQTT外，支持CoAP、HTTP等协议
5. **KubeEdge集成**: 支持边缘计算场景
6. **OTA更新**: 设备固件远程更新
7. **设备分组**: 批量管理设备

## 技术栈

- **MQTT Broker**: Eclipse Mosquitto 2.0
- **Java**: JDK 17
- **Spring Boot**: 3.1.5
- **MQTT Client**: Eclipse Paho
- **Docker**: 容器化部署

## 总结

这次重构实现了：
- ✅ 设备与平台的真实分离
- ✅ 基于标准协议的通信
- ✅ 可扩展的元模型架构
- ✅ 固定框架 + 可变设备类型的设计
- ✅ 为接入真实设备做好准备

符合专家建议的"提升抽象级别，同时为后续接入真实协议和设备提供基本框架"的要求。
