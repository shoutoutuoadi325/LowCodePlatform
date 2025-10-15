# 快速开始指南

## 使用Docker Compose启动（推荐）

这是最简单的启动方式，一条命令启动所有服务。

### 前提条件

- 已安装 Docker
- 已安装 Docker Compose

### 启动步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd LowCodePlatform
   ```

2. **一键启动所有服务**
   ```bash
   docker-compose up --build
   ```

3. **访问应用**
   - 前端: http://localhost:8080
   - Device Service API: http://localhost:8081
   - Scene Service API: http://localhost:8082

### 查看运行状态

```bash
# 查看所有容器
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看特定服务的日志
docker-compose logs -f device-simulator
docker-compose logs -f device-service
```

### 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

## 验证系统运行

### 1. 查看设备类型

```bash
curl http://localhost:8081/api/device-types
```

应该返回2个默认设备类型：temperature_sensor 和 smart_light

### 2. 查看设备列表

访问前端: http://localhost:8080/devices

### 3. 监控MQTT消息

使用MQTT客户端工具连接到 `localhost:1883`，订阅主题：
```
iot/devices/+/telemetry
```

你应该能看到设备每10秒上报一次遥测数据。

### 4. 测试设备控制

通过前端或API控制设备：

```bash
# 打开灯
curl -X POST http://localhost:8081/api/devices/control \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "light-001",
    "action": "turnOn",
    "parameters": {"brightness": 80}
  }'
```

## 运行的服务

启动后会运行以下服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| mosquitto | 1883, 9001 | MQTT消息代理 |
| device-service | 8081 | 设备管理服务 |
| scene-service | 8082 |l 场景编排服务 |
| device-simulator | - | 设备模拟器（5个虚拟设备） |
| frontend | 8080 | Web前端 |

## 模拟的设备

系统会自动启动以下虚拟设备：

1. **temp-sensor-001** - 办公室温度传感器
2. **temp-sensor-002** - 会议室温度传感器
3. **light-001** - 办公室主灯
4. **light-002** - 会议室主灯
5. **light-003** - 走廊灯

这些设备会：
- 自动连接到MQTT Broker
- 每10秒上报遥测数据
- 每30秒发送心跳
- 响应平台的控制命令

## 常见问题

### 端口冲突

如果端口已被占用，修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "18080:80"  # 修改为其他端口
```

### 查看MQTT消息

安装mosquitto-clients:
```bash
docker exec -it mqtt-broker sh
mosquitto_sub -t "iot/devices/#"
```

### 重新构建服务

```bash
docker-compose build --no-cache
docker-compose up
```

## 下一步

- 📖 阅读 [设备架构文档](docs/DEVICE_ARCHITECTURE.md)
- 📖 阅读 [重构总结](docs/REFACTORING_SUMMARY.md)
- 🔧 尝试添加新的设备类型
- 🎨 创建自动化场景

## 开发模式

如果要进行开发调试，请参考主 README.md 中的"本地开发"部分。
