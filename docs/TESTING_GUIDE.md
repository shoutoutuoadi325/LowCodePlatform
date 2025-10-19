# LowCodePlatform 测试指南

## 📋 目录

1. [环境准备](#环境准备)
2. [快速开始](#快速开始)
3. [数据生成](#数据生成)
4. [功能测试](#功能测试)
5. [API测试](#api测试)
6. [常见问题](#常见问题)
7. [测试清单](#测试清单)

## 🛠 环境准备

### 系统要求

- **Node.js**: 版本 14.0 或更高
- **Docker**: 用于运行后端服务
- **Docker Compose**: 用于编排服务
- **浏览器**: Chrome、Firefox、Safari 或 Edge

### 端口占用检查

确保以下端口未被占用：
- `8080`: 前端服务（生产模式）
- `8081`: 设备服务
- `8082`: 场景服务  
- `8083`: 前端服务（开发模式）
- `1883`: MQTT Broker
- `9001`: MQTT WebSocket

### 环境变量配置

创建 `.env` 文件（可选）：
```bash
# 服务地址配置
VUE_APP_DEVICE_SERVICE_URL=http://localhost:8081
VUE_APP_SCENE_SERVICE_URL=http://localhost:8082
VUE_APP_SYSTEM_SERVICE_URL=http://localhost:8081
VUE_APP_SIMULATOR_SERVICE_URL=http://localhost:8081

# MQTT配置
MQTT_BROKER_URL=tcp://localhost:1883
```

## 🚀 快速开始

### 方式一：使用快速启动脚本（推荐）

```bash
# 1. 进入项目目录
cd LowCodePlatform

# 2. 安装依赖
npm install

# 3. 运行快速启动脚本
node scripts/quick-start.js
```

### 方式二：手动启动

```bash
# 1. 启动所有服务
docker-compose up -d

# 2. 等待服务启动（约30-60秒）
docker-compose ps

# 3. 生成测试数据
node scripts/generate-test-data.js

# 4. 启动前端开发服务器
cd frontend
npm install
npm run serve
```

### 验证服务状态

访问以下地址验证服务是否正常：

- **前端界面**: http://localhost:8083
- **设备服务健康检查**: http://localhost:8081/api/device-types/test
- **场景服务健康检查**: http://localhost:8082/api/scenes

## 📊 数据生成

### 自动生成测试数据

运行数据生成脚本：
```bash
node scripts/generate-test-data.js
```

### 生成的数据内容

#### 设备类型（5种）
1. **智能灯泡** - 可调光调色LED灯
2. **温度传感器** - 温湿度监测设备
3. **智能门锁** - 指纹识别门锁
4. **智能空调** - 变频空调
5. **安防摄像头** - 高清监控设备

#### 设备实例（9个）
- 客厅主灯、卧室台灯
- 客厅温度传感器、卧室温度传感器
- 大门智能锁
- 客厅空调、卧室空调
- 入口监控、客厅监控

#### 场景配置（4个）
- **回家模式** - 门锁解锁时自动开灯开空调
- **睡眠模式** - 晚上10点自动调暗灯光
- **离家模式** - 门锁上锁时关闭所有设备
- **安防模式** - 夜间11点开启监控录制

### 手动添加数据

如需添加自定义数据，可以通过前端界面或API直接创建：

```bash
# 创建设备类型
curl -X POST http://localhost:8081/api/device-types \
  -H "Content-Type: application/json" \
  -d '{
    "typeIdentifier": "custom_device",
    "typeName": "自定义设备",
    "description": "测试设备",
    "category": "测试类别"
  }'

# 创建设备实例
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "test_001",
    "deviceName": "测试设备",
    "deviceType": "custom_device",
    "location": "测试位置"
  }'
```

## 🧪 功能测试

### 1. 系统概览测试

**测试目标**: 验证系统首页显示正常

**测试步骤**:
1. 访问 http://localhost:8083
2. 检查页面是否正常加载
3. 验证导航菜单是否显示
4. 检查系统状态卡片

**预期结果**:
- 页面加载无错误
- 显示设备数量、场景数量等统计信息
- 系统状态显示为"运行中"

### 2. 设备管理测试

**测试目标**: 验证设备的增删改查功能

**测试步骤**:
1. 点击"设备管理"菜单
2. 查看设备列表
3. 点击"添加设备"按钮
4. 填写设备信息并保存
5. 编辑已有设备
6. 删除测试设备

**预期结果**:
- 设备列表正常显示（应有9个设备）
- 可以成功添加新设备
- 设备信息可以正常编辑
- 设备可以正常删除
- 设备状态显示正确（在线/离线）

**测试数据**:
```json
{
  "deviceId": "test_bulb_001",
  "deviceName": "测试灯泡",
  "deviceType": "smart_bulb",
  "location": "测试房间",
  "building": "测试楼",
  "floor": "1楼",
  "room": "测试房间"
}
```

### 3. 设备类型管理测试

**测试目标**: 验证设备类型的管理功能

**测试步骤**:
1. 点击"设备类型管理"菜单
2. 查看设备类型列表
3. 点击某个设备类型查看详情
4. 查看设备属性、操作、事件定义
5. 尝试添加新的设备类型

**预期结果**:
- 显示5种设备类型
- 设备类型详情页面正常显示
- 属性、操作、事件信息完整
- 可以添加新的设备类型

### 4. 场景管理测试

**测试目标**: 验证场景的创建和管理功能

**测试步骤**:
1. 点击"场景管理"菜单
2. 查看场景列表
3. 点击"创建场景"
4. 配置场景触发条件
5. 配置场景执行动作
6. 保存并测试场景

**预期结果**:
- 显示4个预设场景
- 场景创建向导正常工作
- 可以配置多种触发条件
- 可以配置多个执行动作
- 场景可以正常保存和执行

**测试场景示例**:
```json
{
  "name": "测试场景",
  "description": "用于测试的场景",
  "triggers": [
    {
      "triggerType": "DEVICE_STATE",
      "deviceId": "temp_living_001",
      "property": "temperature",
      "operator": "GREATER_THAN",
      "value": "25"
    }
  ],
  "actions": [
    {
      "deviceId": "ac_living_001",
      "action": "turn_on",
      "parameters": {"temperature": 22}
    }
  ]
}
```

### 5. 场景设计器测试

**测试目标**: 验证可视化场景设计功能

**测试步骤**:
1. 点击"场景设计器"菜单
2. 拖拽设备到画布
3. 连接设备创建逻辑关系
4. 配置触发条件和动作
5. 保存设计的场景

**预期结果**:
- 设备列表正常显示
- 可以拖拽设备到画布
- 设备间可以建立连接
- 可以配置逻辑关系
- 设计的场景可以保存

### 6. 设备监控测试

**测试目标**: 验证设备状态监控功能

**测试步骤**:
1. 点击"设备监控"菜单
2. 查看设备状态列表
3. 点击某个设备查看详细状态
4. 查看设备历史数据
5. 测试设备控制功能

**预期结果**:
- 设备状态实时更新
- 设备详情页面显示完整信息
- 历史数据图表正常显示
- 设备控制命令可以正常发送

### 7. 数据分析测试

**测试目标**: 验证数据统计和分析功能

**测试步骤**:
1. 点击"数据分析"菜单
2. 查看设备统计图表
3. 查看场景执行统计
4. 切换不同时间范围
5. 导出数据报告

**预期结果**:
- 统计图表正常显示
- 数据按时间范围正确筛选
- 图表数据与实际情况一致
- 报告可以正常导出

### 8. 系统配置测试

**测试目标**: 验证系统配置管理功能

**测试步骤**:
1. 点击"系统配置"菜单
2. 查看系统信息
3. 修改MQTT配置
4. 测试MQTT连接
5. 保存配置更改

**预期结果**:
- 系统信息正确显示
- MQTT配置可以修改
- 连接测试功能正常
- 配置更改可以保存

### 9. 模拟器管理测试

**测试目标**: 验证设备模拟器功能

**测试步骤**:
1. 点击"模拟器管理"菜单
2. 查看模拟器列表
3. 启动/停止模拟器
4. 配置模拟器参数
5. 查看模拟数据

**预期结果**:
- 模拟器列表正常显示
- 可以控制模拟器启停
- 模拟器参数可以配置
- 模拟数据正常生成

## 🔌 API测试

### 设备服务API测试

```bash
# 获取所有设备类型
curl http://localhost:8081/api/device-types

# 获取所有设备
curl http://localhost:8081/api/devices

# 获取特定设备状态
curl http://localhost:8081/api/devices/bulb_living_001/state

# 控制设备
curl -X POST http://localhost:8081/api/devices/bulb_living_001/control \
  -H "Content-Type: application/json" \
  -d '{"action": "turn_on", "parameters": {"brightness": 80}}'
```

### 场景服务API测试

```bash
# 获取所有场景
curl http://localhost:8082/api/scenes

# 获取活跃场景
curl http://localhost:8082/api/scenes/active

# 执行场景
curl -X POST http://localhost:8082/api/scenes/scene_home_001/execute
```

### API响应示例

**设备列表响应**:
```json
[
  {
    "id": 1,
    "deviceId": "bulb_living_001",
    "deviceName": "客厅主灯",
    "deviceType": "smart_bulb",
    "status": "ONLINE",
    "location": "客厅",
    "building": "A栋",
    "floor": "1楼",
    "room": "客厅"
  }
]
```

**场景列表响应**:
```json
[
  {
    "id": 1,
    "sceneId": "scene_home_001",
    "name": "回家模式",
    "description": "回家时自动执行的场景",
    "status": "ACTIVE",
    "triggers": [...],
    "actions": [...]
  }
]
```

## ❓ 常见问题

### Q1: 服务启动失败

**问题**: Docker容器启动失败或服务无法访问

**解决方案**:
1. 检查端口是否被占用：`netstat -an | findstr :8081`
2. 查看Docker日志：`docker-compose logs device-service`
3. 重启服务：`docker-compose restart`
4. 清理并重建：`docker-compose down && docker-compose up -d`

### Q2: 前端页面空白或报错

**问题**: 前端页面无法正常显示

**解决方案**:
1. 检查控制台错误信息
2. 确认后端服务是否正常运行
3. 清除浏览器缓存
4. 重新安装前端依赖：`cd frontend && npm install`

### Q3: 数据生成失败

**问题**: 运行数据生成脚本时出错

**解决方案**:
1. 确认后端服务已启动
2. 检查网络连接
3. 查看脚本错误信息
4. 手动测试API连接：`curl http://localhost:8081/api/device-types/test`

### Q4: 设备状态不更新

**问题**: 设备状态显示不正确或不更新

**解决方案**:
1. 检查MQTT服务是否正常
2. 确认设备模拟器是否运行
3. 查看设备服务日志
4. 重启设备模拟器

### Q5: 场景执行失败

**问题**: 场景无法正常执行

**解决方案**:
1. 检查场景配置是否正确
2. 确认目标设备是否在线
3. 查看场景服务日志
4. 验证设备控制API是否正常

### Q6: MQTT连接问题

**问题**: MQTT连接失败或消息无法发送

**解决方案**:
1. 检查MQTT Broker是否运行：`docker ps | grep mosquitto`
2. 测试MQTT连接：`telnet localhost 1883`
3. 查看MQTT日志：`docker logs mqtt-broker`
4. 重启MQTT服务：`docker-compose restart mosquitto`

## ✅ 测试清单

### 环境检查
- [ ] Node.js 版本 ≥ 14.0
- [ ] Docker 和 Docker Compose 已安装
- [ ] 所需端口未被占用
- [ ] 网络连接正常

### 服务启动
- [ ] MQTT Broker 正常运行
- [ ] 设备服务正常运行
- [ ] 场景服务正常运行
- [ ] 前端服务正常运行
- [ ] 设备模拟器正常运行

### 数据生成
- [ ] 设备类型创建成功（5个）
- [ ] 设备实例创建成功（9个）
- [ ] 场景配置创建成功（4个）
- [ ] 数据生成脚本无错误

### 功能测试
- [ ] 系统概览页面正常
- [ ] 设备管理功能正常
- [ ] 设备类型管理功能正常
- [ ] 场景管理功能正常
- [ ] 场景设计器功能正常
- [ ] 设备监控功能正常
- [ ] 数据分析功能正常
- [ ] 系统配置功能正常
- [ ] 模拟器管理功能正常

### API测试
- [ ] 设备服务API正常响应
- [ ] 场景服务API正常响应
- [ ] 设备控制API正常工作
- [ ] 场景执行API正常工作

### 性能测试
- [ ] 页面加载时间 < 3秒
- [ ] API响应时间 < 1秒
- [ ] 设备状态更新及时
- [ ] 场景执行响应及时

## 📞 技术支持

如果在测试过程中遇到问题，可以：

1. 查看项目文档：`docs/` 目录
2. 检查日志文件：`docker-compose logs`
3. 提交Issue：项目GitHub仓库
4. 联系开发团队

---

**测试愉快！** 🎉