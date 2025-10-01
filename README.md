# 曦源 IoT 低代码平台

复旦大学曦源项目：基于元建模的物联网设备描述与应用开发方法

## 项目简介

这是一个面向教学楼的低代码物联网平台，采用微服务架构，支持设备管理和场景可视化编排。平台提供了图形化的界面来管理物联网设备，并通过拖拽方式创建自动化场景。

### 核心特性

- **设备管理**：支持多种教学楼设备（灯光、空调、传感器、投影仪等）
- **场景编排**：可视化的场景设计器，支持拖拽式编排
- **模拟设备**：内置设备模拟器，方便开发和测试
- **微服务架构**：易于扩展和维护
- **组件化设计**：便于后期集成 Home Assistant 等真实设备

## 技术栈

### 后端
- **Java 17** + **Spring Boot 3.1.5**
- **Spring Cloud**：微服务支持
- **Spring Data JPA**：数据持久化
- **H2 Database**：内存数据库（开发环境）

### 前端
- **Vue 3**：渐进式 JavaScript 框架
- **Element Plus**：UI 组件库
- **Axios**：HTTP 客户端
- **Pinia**：状态管理
- **Vue Router**：路由管理

### 部署
- **Docker** + **Docker Compose**：容器化部署
- **Maven**：后端构建工具
- **Node.js** + **npm**：前端构建工具

## 项目结构

```
LowCodePlatform/
├── backend/
│   ├── device-service/          # 设备管理服务
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/
│   │   │       │   └── com/xiyuan/iot/device/
│   │   │       │       ├── model/           # 设备模型
│   │   │       │       ├── controller/      # REST API
│   │   │       │       ├── service/         # 业务逻辑
│   │   │       │       ├── repository/      # 数据访问
│   │   │       │       └── simulator/       # 设备模拟器
│   │   │       └── resources/
│   │   │           └── application.yml
│   │   └── pom.xml
│   │
│   └── scene-service/           # 场景编排服务
│       ├── src/
│       │   └── main/
│       │       ├── java/
│       │       │   └── com/xiyuan/iot/scene/
│       │       │       ├── model/           # 场景模型
│       │       │       ├── controller/      # REST API
│       │       │       ├── service/         # 业务逻辑
│       │       │       ├── repository/      # 数据访问
│       │       │       └── engine/          # 场景执行引擎
│       │       └── resources/
│       │           └── application.yml
│       └── pom.xml
│
├── frontend/                    # Vue 前端应用
│   ├── src/
│   │   ├── components/          # 可复用组件
│   │   ├── views/               # 页面视图
│   │   │   ├── Dashboard.vue           # 仪表板
│   │   │   ├── DeviceManagement.vue    # 设备管理
│   │   │   ├── SceneManagement.vue     # 场景管理
│   │   │   └── SceneDesigner.vue       # 场景设计器
│   │   ├── services/            # API 服务
│   │   ├── router/              # 路由配置
│   │   ├── App.vue              # 根组件
│   │   └── main.js              # 入口文件
│   ├── public/
│   ├── package.json
│   └── vue.config.js
│
├── docker-compose.yml           # Docker Compose 配置
└── README.md                    # 项目文档
```

## 快速开始

### 环境要求

- Java 17 或更高版本
- Node.js 18 或更高版本
- Maven 3.6 或更高版本
- Docker & Docker Compose（可选，用于容器化部署）

### 本地开发

#### 1. 启动后端服务

**设备管理服务（端口 8081）：**

```bash
cd backend/device-service
mvn spring-boot:run
```

**场景编排服务（端口 8082）：**

```bash
cd backend/scene-service
mvn spring-boot:run
```

#### 2. 启动前端应用

```bash
cd frontend
npm install
npm run serve
```

前端应用将在 `http://localhost:8080` 启动。

### Docker 部署

使用 Docker Compose 一键部署所有服务：

```bash
docker-compose up -d
```

访问应用：`http://localhost:8080`

## 功能说明

### 1. 设备管理

支持的设备类型：

- **灯光（LIGHT）**：可控制开关、亮度、颜色
- **空调（HVAC）**：可控制温度、模式、风速
- **温度传感器（TEMPERATURE_SENSOR）**：监测温度
- **湿度传感器（HUMIDITY_SENSOR）**：监测湿度
- **人体感应（MOTION_SENSOR）**：检测人员移动
- **门锁（DOOR_LOCK）**：控制门禁
- **窗户/窗帘（WINDOW/CURTAIN）**：控制开合位置
- **投影仪（PROJECTOR）**：控制开关和输入源
- **投影幕（SCREEN）**：控制升降
- **风扇（FAN）**：控制风速
- **空气质量传感器（AIR_QUALITY_SENSOR）**：监测空气质量

设备管理功能：

- 添加/删除设备
- 查看设备状态
- 控制设备（开/关/设置参数）
- 按位置查询设备

### 2. 场景编排

场景组成：

- **触发器**：定义场景何时执行
  - 手动触发
  - 定时触发
  - 设备状态变化
  - 传感器数值条件

- **动作**：定义执行什么操作
  - 设备开关控制
  - 设备状态设置
  - 动作顺序和延迟

场景管理功能：

- 可视化场景设计器
- 拖拽式编排流程
- 场景启用/禁用
- 手动执行场景

### 3. 仪表板

- 设备统计（总数、在线数）
- 场景统计（总数、活跃数）
- 最近设备列表
- 快速场景执行

## API 文档

### 设备管理 API（端口 8081）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/devices` | 获取所有设备 |
| GET | `/api/devices/{id}` | 获取指定设备 |
| POST | `/api/devices` | 创建设备 |
| PUT | `/api/devices/{id}` | 更新设备 |
| DELETE | `/api/devices/{id}` | 删除设备 |
| POST | `/api/devices/{deviceId}/control` | 控制设备 |
| GET | `/api/devices/{deviceId}/state` | 获取设备状态 |

### 场景管理 API（端口 8082）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/scenes` | 获取所有场景 |
| GET | `/api/scenes/active` | 获取活跃场景 |
| GET | `/api/scenes/{id}` | 获取指定场景 |
| POST | `/api/scenes` | 创建场景 |
| PUT | `/api/scenes/{id}` | 更新场景 |
| DELETE | `/api/scenes/{id}` | 删除场景 |
| POST | `/api/scenes/{sceneId}/execute` | 执行场景 |

## 扩展说明

### 集成 Home Assistant

当前平台使用模拟设备。要集成 Home Assistant 真实设备：

1. 在 `device-service` 中创建 `HomeAssistantAdapter` 实现 `DeviceSimulator` 接口
2. 配置 Home Assistant 的 REST API 地址
3. 实现设备状态映射和控制命令转换
4. 通过配置文件切换模拟/真实设备

### 添加新设备类型

1. 在 `DeviceType` 枚举中添加新类型
2. 在 `SimulatedDeviceManager.initializeDevice()` 中添加初始化逻辑
3. 更新前端的设备类型选项

### 扩展触发器类型

1. 在 `TriggerType` 枚举中添加新类型
2. 实现相应的触发器检测逻辑
3. 更新场景设计器的触发器选项

## 开发指南

### 后端开发

后端使用标准的 Spring Boot 项目结构：

- **Model**：定义数据模型和实体
- **Repository**：数据访问层（JPA）
- **Service**：业务逻辑层
- **Controller**：REST API 控制器

### 前端开发

前端使用 Vue 3 Composition API：

- 使用 `<script setup>` 语法
- Element Plus 作为 UI 组件库
- Axios 进行 HTTP 请求
- 响应式状态管理

## 许可证

本项目采用 Apache License 2.0 开源许可证。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

- 项目：复旦大学曦源项目
- 主题：基于元建模的物联网设备描述与应用开发方法
