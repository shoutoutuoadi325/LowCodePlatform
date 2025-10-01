# 曦源 XiYuan

复旦大学曦源项目：基于元建模的物联网设备描述与应用开发方法

## 项目简介

曦源是一个低代码物联网管理平台，专注于教学楼场景的智能化管理。该平台采用微服务架构，提供可视化的场景编排功能，支持多种IoT设备的统一管理和自动化控制。

## 技术架构

### 后端 (Java + Spring Boot)

采用微服务架构，包含以下服务：

- **API Gateway** (8080) - 统一网关，路由请求到各个微服务
- **Device Service** (8081) - 设备管理服务，提供模拟设备功能
- **Scene Service** (8082) - 场景编排服务，支持场景的创建和执行
- **Common Module** - 通用模型和工具类

### 前端 (Vue.js 3)

- 基于 Vue 3 + Vite + Pinia
- 响应式设计，支持设备管理和场景编排
- 提供可视化的场景拖拽编排界面

## 功能特性

### 设备管理

支持的设备类型：
- 🔆 灯光控制（开关、亮度、颜色调节）
- ❄️ 空调控制（温度、模式、风速调节）
- 📽️ 投影仪控制（开关、输入源切换）
- 🪟 窗帘控制（开关、位置调节）
- 🌡️ 温湿度传感器
- 🚶 运动传感器

### 场景编排

- 📝 可视化创建和编辑场景
- ⏱️ 支持动作延迟执行
- 🔄 一键执行复杂场景
- 📊 预设常用场景（上课模式、下课模式、节能模式等）

## 快速开始

### 前置要求

- Java 17+
- Maven 3.6+
- Node.js 18+
- Docker & Docker Compose (可选)

### 使用 Docker Compose 启动

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

启动后访问：
- 前端界面: http://localhost:3000
- API 网关: http://localhost:8080

### 本地开发模式

#### 后端服务

```bash
# 1. 编译 common 模块
cd backend/common
mvn clean install

# 2. 启动 Device Service
cd backend/device-service
mvn spring-boot:run

# 3. 启动 Scene Service
cd backend/scene-service
mvn spring-boot:run

# 4. 启动 API Gateway
cd backend/api-gateway
mvn spring-boot:run
```

#### 前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:3000

## 项目结构

```
xiyuan/
├── backend/
│   ├── api-gateway/          # API 网关服务
│   ├── device-service/       # 设备管理服务
│   ├── scene-service/        # 场景编排服务
│   └── common/               # 公共模块
├── frontend/                 # Vue.js 前端
│   ├── src/
│   │   ├── components/      # 可复用组件
│   │   ├── views/           # 页面视图
│   │   ├── stores/          # 状态管理
│   │   └── utils/           # 工具函数
│   └── package.json
├── docker-compose.yml        # Docker 编排配置
└── README.md
```

## API 接口文档

### 设备管理 API

- `GET /api/devices` - 获取所有设备
- `GET /api/devices/{id}` - 获取指定设备
- `GET /api/devices/room/{room}` - 按教室获取设备
- `POST /api/devices/{id}/action` - 执行设备动作
- `GET /api/devices/{id}/state` - 获取设备状态

### 场景管理 API

- `GET /api/scenes` - 获取所有场景
- `GET /api/scenes/{id}` - 获取指定场景
- `POST /api/scenes` - 创建场景
- `PUT /api/scenes/{id}` - 更新场景
- `DELETE /api/scenes/{id}` - 删除场景
- `POST /api/scenes/{id}/execute` - 执行场景

## 使用说明

### 设备管理

1. 访问设备管理页面，查看所有可用设备
2. 可按教室筛选设备
3. 点击设备卡片上的按钮控制设备
4. 实时查看设备状态变化

### 场景编排

1. 点击"创建新场景"按钮
2. 填写场景名称和描述
3. 添加动作：
   - 选择设备
   - 选择要执行的动作
   - 设置动作参数（如需要）
   - 设置延迟时间（可选）
4. 保存场景
5. 点击"执行场景"按钮运行场景

### 预设场景示例

**上课模式**
- 打开教室灯光（亮度100%）
- 关闭窗帘
- 开启投影仪（延迟2秒）

**下课模式**
- 关闭投影仪
- 打开窗帘（延迟1秒）

**节能模式**
- 降低灯光亮度至50%
- 将空调温度设置为28°C

## 后续扩展

### 集成 Home Assistant

当前版本使用模拟设备，后续可以通过以下方式集成真实设备：

1. 配置 Home Assistant API 端点
2. 在 Device Service 中实现 Home Assistant 适配器
3. 替换模拟设备为真实设备接口
4. 支持设备自动发现和注册

### 功能扩展计划

- 🔔 设备告警和通知
- 📈 设备数据统计和可视化
- 🤖 基于AI的智能场景推荐
- 📱 移动端应用支持
- 🔐 用户权限和多租户管理
- ⏰ 定时任务和触发器

## 开源协议

本项目采用 Apache License 2.0 开源协议。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

复旦大学曦源项目组
