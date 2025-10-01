# 架构设计文档

## 系统架构概述

曦源平台采用前后端分离的微服务架构，确保系统的可扩展性和可维护性。

```
┌─────────────────────────────────────────────────────────┐
│                     Frontend (Vue.js)                    │
│               http://localhost:3000                      │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/REST
┌────────────────────▼────────────────────────────────────┐
│              API Gateway (Spring Cloud Gateway)          │
│               http://localhost:8080                      │
└────────┬──────────────────────────────┬─────────────────┘
         │                              │
         │ HTTP/REST                    │ HTTP/REST
         │                              │
┌────────▼──────────┐         ┌────────▼──────────────────┐
│  Device Service   │         │    Scene Service          │
│  Port: 8081       │◄────────┤    Port: 8082             │
└───────────────────┘         └───────────────────────────┘
         │                              │
         │                              │
    ┌────▼────┐                   ┌────▼────┐
    │模拟设备  │                   │场景引擎  │
    └─────────┘                   └─────────┘
```

## 微服务模块

### 1. API Gateway (端口 8080)

**职责：**
- 统一入口：所有外部请求的单一入口点
- 路由转发：将请求路由到相应的后端服务
- 跨域处理：配置 CORS 支持前端访问
- 负载均衡：支持服务实例的负载均衡

**技术栈：**
- Spring Cloud Gateway
- Spring Boot 3.1.5
- Java 17

**关键配置：**
```yaml
spring.cloud.gateway.routes:
  - /api/devices/** → device-service:8081
  - /api/scenes/** → scene-service:8082
```

### 2. Device Service (端口 8081)

**职责：**
- 设备管理：维护设备的注册、状态、配置
- 设备控制：执行设备的各种操作命令
- 状态查询：提供设备实时状态查询
- 模拟设备：提供各类设备的模拟实现

**技术栈：**
- Spring Boot Web
- Spring Boot Actuator
- Lombok

**设备类型：**
- 灯光设备 (LightDevice)
- 空调设备 (AirConditionerDevice)
- 投影仪设备 (ProjectorDevice)
- 窗帘设备 (CurtainDevice)
- 传感器设备 (温度、湿度、运动、光照)

**扩展性：**
- 支持通过实现 `SimulatedDevice` 抽象类添加新设备类型
- 后续可替换为 Home Assistant 适配器连接真实设备

### 3. Scene Service (端口 8082)

**职责：**
- 场景管理：创建、更新、删除场景配置
- 场景执行：按照配置顺序执行场景动作
- 动作编排：支持动作的延迟执行和参数配置
- 触发器管理：支持基于条件的自动触发（待实现）

**技术栈：**
- Spring Boot Web
- RestTemplate (服务间通信)
- Spring Boot Actuator

**场景模型：**
```java
Scene {
  id: String
  name: String
  description: String
  actions: List<SceneAction>
  triggers: List<SceneTrigger>
  enabled: boolean
}

SceneAction {
  deviceId: String
  action: String
  parameters: Map<String, Object>
  delaySeconds: Integer
}
```

### 4. Common Module

**职责：**
- 共享模型：定义设备、场景等核心数据模型
- 枚举类型：设备类型、设备状态等枚举
- 工具类：通用工具方法（待扩展）

**核心模型：**
- Device: 设备模型
- DeviceType: 设备类型枚举
- DeviceStatus: 设备状态枚举
- Scene: 场景模型
- SceneAction: 场景动作模型
- SceneTrigger: 场景触发器模型

## 前端架构

### 技术栈

- **框架**: Vue.js 3 (Composition API)
- **构建工具**: Vite 4
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **UI风格**: 自定义 CSS（响应式设计）

### 目录结构

```
frontend/src/
├── components/          # 可复用组件
│   ├── DeviceCard.vue  # 设备卡片组件
│   └── SceneCard.vue   # 场景卡片组件
├── views/              # 页面视图
│   ├── DeviceView.vue  # 设备管理页面
│   └── SceneView.vue   # 场景编排页面
├── stores/             # Pinia 状态存储
│   ├── deviceStore.js  # 设备状态管理
│   └── sceneStore.js   # 场景状态管理
├── utils/              # 工具函数
│   └── api.js         # API 封装
├── assets/            # 静态资源
│   └── style.css      # 全局样式
├── App.vue            # 根组件
├── main.js            # 应用入口
└── router.js          # 路由配置
```

### 组件设计

#### DeviceCard 组件

**功能：**
- 展示设备基本信息和状态
- 提供设备控制按钮
- 支持不同设备类型的个性化控制

**Props：**
- device: Device 对象

**Events：**
- action: 触发设备动作

#### SceneCard 组件

**功能：**
- 展示场景信息和动作列表
- 提供场景执行、编辑、删除功能

**Props：**
- scene: Scene 对象

**Events：**
- execute: 执行场景
- edit: 编辑场景
- delete: 删除场景

## 数据流

### 设备控制流程

```
用户点击控制按钮
  ↓
DeviceCard 组件触发 action 事件
  ↓
DeviceView 调用 deviceStore.executeDeviceAction()
  ↓
发送 POST /api/devices/{id}/action 请求
  ↓
API Gateway 路由到 Device Service
  ↓
Device Service 执行设备动作
  ↓
更新设备状态
  ↓
返回新状态给前端
  ↓
前端刷新设备列表显示
```

### 场景执行流程

```
用户点击执行场景
  ↓
SceneView 调用 sceneStore.executeScene()
  ↓
发送 POST /api/scenes/{id}/execute 请求
  ↓
API Gateway 路由到 Scene Service
  ↓
Scene Service 遍历场景动作
  ↓
对每个动作：
  - 等待延迟时间（如有）
  - 调用 Device Service API
  - 执行设备动作
  ↓
返回执行结果
  ↓
前端显示执行成功提示
```

## 部署架构

### Docker 容器化部署

所有服务都已 Docker 化，支持一键部署：

```
docker-compose up -d
```

**容器网络：**
- xiyuan-network: 自定义桥接网络
- 容器间通过服务名通信

**端口映射：**
- 3000 → frontend:80
- 8080 → api-gateway:8080
- 8081 → device-service:8081
- 8082 → scene-service:8082

## 扩展性设计

### 设备扩展

1. 在 Common 模块添加新的 DeviceType 枚举
2. 在 Device Service 实现新的设备类（继承 SimulatedDevice）
3. 在 DeviceService 的 initializeSimulatedDevices() 中初始化设备实例
4. 前端在 DeviceCard 和 SceneView 中添加相应的控制逻辑

### 场景扩展

1. 支持更复杂的触发条件（时间、传感器数据等）
2. 支持条件分支和循环逻辑
3. 支持场景嵌套调用
4. 支持场景模板和市场

### 集成真实设备

通过适配器模式集成 Home Assistant 或其他 IoT 平台：

```java
interface DeviceAdapter {
  Device getDevice(String id);
  boolean executeAction(String id, String action, Map params);
  Map<String, Object> getState(String id);
}

class HomeAssistantAdapter implements DeviceAdapter {
  // 实现与 Home Assistant API 的交互
}
```

## 安全性考虑

### 当前实现

- CORS 配置支持跨域请求
- RESTful API 设计遵循最佳实践

### 后续增强

- JWT 认证和授权
- API 速率限制
- HTTPS 加密传输
- 设备访问权限控制
- 操作审计日志

## 性能优化

### 后端优化

- 设备状态使用 ConcurrentHashMap 内存缓存
- 支持并发设备操作
- 异步场景执行（可选）

### 前端优化

- Vite 构建优化
- 组件懒加载
- API 请求合并和缓存
- 响应式数据最小化

## 监控和维护

### Spring Boot Actuator

所有服务都启用了 Actuator 端点：

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标

### 日志

- 使用 SLF4J + Logback
- 支持日志级别配置
- 建议集成 ELK 或类似日志聚合系统

## 总结

曦源平台采用现代化的微服务架构，具有良好的可扩展性和可维护性。通过模块化设计，可以轻松添加新的设备类型和功能。前后端分离的架构使得团队可以并行开发，提高开发效率。
