# 示例数据

本文档提供了一些示例数据和场景，帮助您快速体验平台功能。

## 示例设备

### 理科楼301教室设备

#### 1. 教室灯光

```bash
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室前排灯光",
    "type": "LIGHT",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "前排",
    "status": "ONLINE"
  }'
```

#### 2. 教室空调

```bash
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室中央空调",
    "type": "HVAC",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "天花板中央",
    "status": "ONLINE"
  }'
```

#### 3. 投影仪

```bash
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室投影仪",
    "type": "PROJECTOR",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "天花板前方",
    "status": "ONLINE"
  }'
```

#### 4. 投影幕

```bash
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室投影幕",
    "type": "SCREEN",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "前方黑板上方",
    "status": "ONLINE"
  }'
```

#### 5. 窗帘

```bash
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室窗帘",
    "type": "CURTAIN",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "左侧窗户",
    "status": "ONLINE"
  }'
```

#### 6. 温度传感器

```bash
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室温度传感器",
    "type": "TEMPERATURE_SENSOR",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "后墙中央",
    "status": "ONLINE"
  }'
```

#### 7. 人体感应传感器

```bash
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室人体感应",
    "type": "MOTION_SENSOR",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "门口上方",
    "status": "ONLINE"
  }'
```

## 示例场景

### 场景1：上课模式

创建一个完整的上课准备场景：

```bash
curl -X POST http://localhost:8082/api/scenes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "上课模式",
    "description": "准备教室进入上课状态：开启灯光和投影设备，调节空调",
    "status": "ACTIVE",
    "triggers": [
      {
        "type": "MANUAL"
      }
    ],
    "actions": [
      {
        "deviceId": "获取设备列表后填入实际的deviceId",
        "action": "turn_on",
        "parameters": {},
        "delaySeconds": 0,
        "order": 0
      },
      {
        "deviceId": "投影仪deviceId",
        "action": "turn_on",
        "parameters": {},
        "delaySeconds": 2,
        "order": 1
      },
      {
        "deviceId": "投影幕deviceId",
        "action": "set_state",
        "parameters": {
          "position": "down"
        },
        "delaySeconds": 3,
        "order": 2
      },
      {
        "deviceId": "窗帘deviceId",
        "action": "set_state",
        "parameters": {
          "position": "50"
        },
        "delaySeconds": 4,
        "order": 3
      },
      {
        "deviceId": "空调deviceId",
        "action": "set_state",
        "parameters": {
          "temperature": "22",
          "mode": "cool"
        },
        "delaySeconds": 5,
        "order": 4
      }
    ]
  }'
```

### 场景2：下课模式

```bash
curl -X POST http://localhost:8082/api/scenes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "下课模式",
    "description": "关闭教学设备，保持照明和通风",
    "status": "ACTIVE",
    "triggers": [
      {
        "type": "MANUAL"
      }
    ],
    "actions": [
      {
        "deviceId": "投影仪deviceId",
        "action": "turn_off",
        "parameters": {},
        "delaySeconds": 0,
        "order": 0
      },
      {
        "deviceId": "投影幕deviceId",
        "action": "set_state",
        "parameters": {
          "position": "up"
        },
        "delaySeconds": 2,
        "order": 1
      },
      {
        "deviceId": "窗帘deviceId",
        "action": "set_state",
        "parameters": {
          "position": "100"
        },
        "delaySeconds": 3,
        "order": 2
      }
    ]
  }'
```

### 场景3：节能模式

```bash
curl -X POST http://localhost:8082/api/scenes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "节能模式",
    "description": "关闭所有设备以节约能源",
    "status": "ACTIVE",
    "triggers": [
      {
        "type": "MANUAL"
      }
    ],
    "actions": [
      {
        "deviceId": "灯光deviceId",
        "action": "turn_off",
        "parameters": {},
        "delaySeconds": 0,
        "order": 0
      },
      {
        "deviceId": "空调deviceId",
        "action": "turn_off",
        "parameters": {},
        "delaySeconds": 1,
        "order": 1
      },
      {
        "deviceId": "投影仪deviceId",
        "action": "turn_off",
        "parameters": {},
        "delaySeconds": 1,
        "order": 2
      }
    ]
  }'
```

### 场景4：晚间模式

```bash
curl -X POST http://localhost:8082/api/scenes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "晚间模式",
    "description": "降低灯光亮度，适合晚自习",
    "status": "ACTIVE",
    "triggers": [
      {
        "type": "SCHEDULE",
        "condition": "18:00"
      }
    ],
    "actions": [
      {
        "deviceId": "灯光deviceId",
        "action": "set_state",
        "parameters": {
          "brightness": "60",
          "power": "on"
        },
        "delaySeconds": 0,
        "order": 0
      },
      {
        "deviceId": "窗帘deviceId",
        "action": "set_state",
        "parameters": {
          "position": "0"
        },
        "delaySeconds": 2,
        "order": 1
      }
    ]
  }'
```

## 初始化脚本

### Bash 脚本（Linux/Mac）

保存为 `init_data.sh`:

```bash
#!/bin/bash

BASE_URL_DEVICE="http://localhost:8081"
BASE_URL_SCENE="http://localhost:8082"

echo "初始化示例设备..."

# 创建设备并保存ID
LIGHT_ID=$(curl -s -X POST ${BASE_URL_DEVICE}/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室前排灯光",
    "type": "LIGHT",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "前排",
    "status": "ONLINE"
  }' | jq -r '.deviceId')

echo "创建灯光设备: $LIGHT_ID"

HVAC_ID=$(curl -s -X POST ${BASE_URL_DEVICE}/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室中央空调",
    "type": "HVAC",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "天花板中央",
    "status": "ONLINE"
  }' | jq -r '.deviceId')

echo "创建空调设备: $HVAC_ID"

PROJECTOR_ID=$(curl -s -X POST ${BASE_URL_DEVICE}/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "教室投影仪",
    "type": "PROJECTOR",
    "building": "理科楼",
    "floor": "3F",
    "room": "301教室",
    "location": "天花板前方",
    "status": "ONLINE"
  }' | jq -r '.deviceId')

echo "创建投影仪设备: $PROJECTOR_ID"

echo ""
echo "初始化完成！"
echo "灯光设备ID: $LIGHT_ID"
echo "空调设备ID: $HVAC_ID"
echo "投影仪设备ID: $PROJECTOR_ID"
```

### PowerShell 脚本（Windows）

保存为 `init_data.ps1`:

```powershell
$BASE_URL_DEVICE = "http://localhost:8081"
$BASE_URL_SCENE = "http://localhost:8082"

Write-Host "初始化示例设备..."

# 创建灯光设备
$lightBody = @{
    name = "教室前排灯光"
    type = "LIGHT"
    building = "理科楼"
    floor = "3F"
    room = "301教室"
    location = "前排"
    status = "ONLINE"
} | ConvertTo-Json

$light = Invoke-RestMethod -Uri "$BASE_URL_DEVICE/api/devices" -Method Post -Body $lightBody -ContentType "application/json"
Write-Host "创建灯光设备: $($light.deviceId)"

# 创建空调设备
$hvacBody = @{
    name = "教室中央空调"
    type = "HVAC"
    building = "理科楼"
    floor = "3F"
    room = "301教室"
    location = "天花板中央"
    status = "ONLINE"
} | ConvertTo-Json

$hvac = Invoke-RestMethod -Uri "$BASE_URL_DEVICE/api/devices" -Method Post -Body $hvacBody -ContentType "application/json"
Write-Host "创建空调设备: $($hvac.deviceId)"

Write-Host ""
Write-Host "初始化完成！"
```

## 测试场景执行

创建设备和场景后，可以通过以下方式测试场景执行：

```bash
# 获取场景列表
curl http://localhost:8082/api/scenes

# 执行场景（将 scene-id 替换为实际的场景ID）
curl -X POST http://localhost:8082/api/scenes/{scene-id}/execute

# 查看设备状态
curl http://localhost:8081/api/devices/{device-id}/state
```

## 使用 Postman

导入以下集合以在 Postman 中测试：

1. 创建新集合"曦源IoT平台"
2. 添加环境变量：
   - `device_service`: `http://localhost:8081`
   - `scene_service`: `http://localhost:8082`
3. 导入上述 API 请求

## 注意事项

1. 确保后端服务已启动
2. 创建场景时需要使用真实的设备ID
3. 建议先创建设备，再创建场景
4. 可以在前端界面中直接操作，更加直观