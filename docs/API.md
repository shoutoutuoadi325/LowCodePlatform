# API 接口文档

## 设备管理服务 API (Device Service)

基础URL: `http://localhost:8081`

### 1. 获取所有设备

**请求:**
```
GET /api/devices
```

**响应:**
```json
[
  {
    "id": 1,
    "deviceId": "device-001",
    "name": "教室前排灯光",
    "type": "LIGHT",
    "location": "前排左侧",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "status": "ONLINE",
    "properties": {},
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

### 2. 获取指定设备

**请求:**
```
GET /api/devices/{id}
```

**路径参数:**
- `id`: 设备数据库ID

**响应:**
```json
{
  "id": 1,
  "deviceId": "device-001",
  "name": "教室前排灯光",
  "type": "LIGHT",
  "status": "ONLINE"
}
```

### 3. 通过设备ID获取设备

**请求:**
```
GET /api/devices/by-device-id/{deviceId}
```

**路径参数:**
- `deviceId`: 设备唯一标识

### 4. 创建设备

**请求:**
```
POST /api/devices
Content-Type: application/json

{
  "name": "教室空调",
  "type": "HVAC",
  "building": "理科楼",
  "floor": "3F",
  "room": "301教室",
  "location": "后排右侧",
  "status": "ONLINE"
}
```

**响应:**
```json
{
  "id": 2,
  "deviceId": "auto-generated-uuid",
  "name": "教室空调",
  "type": "HVAC",
  "status": "ONLINE"
}
```

### 5. 更新设备

**请求:**
```
PUT /api/devices/{id}
Content-Type: application/json

{
  "name": "教室中央空调",
  "location": "天花板中央",
  "status": "ONLINE"
}
```

### 6. 删除设备

**请求:**
```
DELETE /api/devices/{id}
```

**响应:**
```
204 No Content
```

### 7. 控制设备

**请求:**
```
POST /api/devices/{deviceId}/control
Content-Type: application/json

{
  "action": "turn_on"
}
```

或设置状态:
```json
{
  "action": "set_state",
  "parameters": {
    "brightness": 80,
    "color": "#FF0000"
  }
}
```

**响应:**
```json
{
  "success": true,
  "deviceId": "device-001",
  "action": "turn_on"
}
```

**支持的操作:**
- `turn_on`: 打开设备
- `turn_off`: 关闭设备
- `set_state`: 设置设备状态（需要 parameters）

### 8. 获取设备状态

**请求:**
```
GET /api/devices/{deviceId}/state
```

**响应:**
```json
{
  "power": "on",
  "brightness": 100,
  "color": "#FFFFFF",
  "lastUpdate": 1640000000000
}
```

### 9. 按位置查询设备

**请求:**
```
GET /api/devices/location?building=理科楼&floor=3F
```

**查询参数:**
- `building`: 教学楼名称
- `floor`: 楼层

### 10. 按房间查询设备

**请求:**
```
GET /api/devices/room/{room}
```

**路径参数:**
- `room`: 房间号或名称

---

## 场景管理服务 API (Scene Service)

基础URL: `http://localhost:8082`

### 1. 获取所有场景

**请求:**
```
GET /api/scenes
```

**响应:**
```json
[
  {
    "id": 1,
    "sceneId": "scene-001",
    "name": "上课模式",
    "description": "打开灯光、投影仪，关闭窗帘",
    "status": "ACTIVE",
    "triggers": [
      {
        "id": 1,
        "type": "MANUAL",
        "deviceId": null,
        "condition": null,
        "parameters": {}
      }
    ],
    "actions": [
      {
        "id": 1,
        "deviceId": "light-001",
        "action": "turn_on",
        "parameters": {},
        "delaySeconds": 0,
        "order": 0
      },
      {
        "id": 2,
        "deviceId": "projector-001",
        "action": "turn_on",
        "parameters": {},
        "delaySeconds": 2,
        "order": 1
      }
    ],
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

### 2. 获取活跃场景

**请求:**
```
GET /api/scenes/active
```

### 3. 获取指定场景

**请求:**
```
GET /api/scenes/{id}
```

### 4. 通过场景ID获取场景

**请求:**
```
GET /api/scenes/by-scene-id/{sceneId}
```

### 5. 创建场景

**请求:**
```
POST /api/scenes
Content-Type: application/json

{
  "name": "下课模式",
  "description": "关闭投影仪和灯光",
  "status": "ACTIVE",
  "triggers": [
    {
      "type": "MANUAL",
      "parameters": {}
    }
  ],
  "actions": [
    {
      "deviceId": "projector-001",
      "action": "turn_off",
      "parameters": {},
      "delaySeconds": 0,
      "order": 0
    },
    {
      "deviceId": "light-001",
      "action": "turn_off",
      "parameters": {},
      "delaySeconds": 2,
      "order": 1
    }
  ]
}
```

### 6. 更新场景

**请求:**
```
PUT /api/scenes/{id}
Content-Type: application/json

{
  "name": "下课模式（更新）",
  "description": "关闭所有设备",
  "status": "ACTIVE"
}
```

### 7. 删除场景

**请求:**
```
DELETE /api/scenes/{id}
```

### 8. 执行场景

**请求:**
```
POST /api/scenes/{sceneId}/execute
```

**响应:**
```json
{
  "success": true,
  "sceneId": "scene-001",
  "message": "Scene executed successfully"
}
```

---

## 数据模型

### DeviceType 枚举

```
LIGHT              - 灯光
HVAC               - 空调
TEMPERATURE_SENSOR - 温度传感器
HUMIDITY_SENSOR    - 湿度传感器
MOTION_SENSOR      - 人体感应
DOOR_LOCK          - 门锁
WINDOW             - 窗户
CURTAIN            - 窗帘
PROJECTOR          - 投影仪
SCREEN             - 投影幕
FAN                - 风扇
AIR_QUALITY_SENSOR - 空气质量传感器
CAMERA             - 摄像头
SPEAKER            - 扬声器
POWER_SWITCH       - 电源开关
```

### DeviceStatus 枚举

```
ONLINE   - 在线
OFFLINE  - 离线
ERROR    - 错误
DISABLED - 已禁用
```

### TriggerType 枚举

```
MANUAL         - 手动触发
SCHEDULE       - 定时触发
DEVICE_STATE   - 设备状态变化
SENSOR_VALUE   - 传感器数值
TIME_BASED     - 基于时间
LOCATION_BASED - 基于位置
```

### SceneStatus 枚举

```
ACTIVE   - 启用
INACTIVE - 未启用
ERROR    - 错误
```

---

## 错误处理

所有API在发生错误时返回适当的HTTP状态码：

- `200 OK`: 请求成功
- `201 Created`: 资源创建成功
- `204 No Content`: 删除成功
- `400 Bad Request`: 请求参数错误
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器内部错误

错误响应示例：
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Device not found: device-999",
  "path": "/api/devices/device-999"
}
```

---

## 使用示例

### 示例1: 创建设备并控制

```bash
# 1. 创建灯光设备
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室灯光",
    "type": "LIGHT",
    "building": "理科楼",
    "floor": "3F",
    "room": "301",
    "status": "ONLINE"
  }'

# 响应: {"id": 1, "deviceId": "abc-123", ...}

# 2. 打开灯光
curl -X POST http://localhost:8081/api/devices/abc-123/control \
  -H "Content-Type: application/json" \
  -d '{"action": "turn_on"}'

# 3. 设置灯光亮度
curl -X POST http://localhost:8081/api/devices/abc-123/control \
  -H "Content-Type: application/json" \
  -d '{
    "action": "set_state",
    "parameters": {
      "brightness": 80
    }
  }'
```

### 示例2: 创建并执行场景

```bash
# 1. 创建"上课模式"场景
curl -X POST http://localhost:8082/api/scenes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "上课模式",
    "description": "准备上课环境",
    "status": "ACTIVE",
    "triggers": [
      {"type": "MANUAL"}
    ],
    "actions": [
      {
        "deviceId": "light-001",
        "action": "turn_on",
        "order": 0
      },
      {
        "deviceId": "projector-001",
        "action": "turn_on",
        "delaySeconds": 2,
        "order": 1
      }
    ]
  }'

# 响应: {"id": 1, "sceneId": "scene-xyz", ...}

# 2. 执行场景
curl -X POST http://localhost:8082/api/scenes/scene-xyz/execute

# 响应: {"success": true, "message": "Scene executed successfully"}
```

### 示例3: 查询特定位置的设备

```bash
# 查询理科楼3楼的所有设备
curl http://localhost:8081/api/devices/location?building=理科楼&floor=3F

# 查询301教室的所有设备
curl http://localhost:8081/api/devices/room/301教室
```