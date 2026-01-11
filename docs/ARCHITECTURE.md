# 架构设计文档

## 系统架构

### 整体架构

本系统采用前后端分离的微服务架构，主要包含以下组件：

```
┌─────────────────────────────────────────────────────────────┐
│                       前端应用 (Vue 3)                        │
│                     端口: 8080                                │
└────────────┬────────────────────────────────┬────────────────┘
             │                                │
             │ HTTP/REST                      │ HTTP/REST
             │                                │
┌────────────▼───────────────┐   ┌──────────▼─────────────────┐
│   设备管理服务               │   │   场景编排服务              │
│   (Device Service)          │   │   (Scene Service)          │
│   端口: 8081                │   │   端口: 8082               │
│                             │   │                            │
│   - 设备 CRUD               │   │   - 场景 CRUD              │
│   - 设备控制                │   │   - 场景执行               │
│   - 设备状态查询            │◄──┤   - 触发器管理             │
│   - 模拟设备管理            │   │   - 动作编排               │
└─────────────────────────────┘   └────────────────────────────┘
```

### 核心组件

#### 1. 设备管理服务 (Device Service)

**职责：**
- 管理物联网设备的生命周期
- 提供设备控制接口
- 模拟设备状态和行为
- 支持多种设备类型

**核心类：**
- `Device`: 设备实体模型
- `DeviceService`: 设备业务逻辑
- `DeviceController`: REST API 控制器
- `SimulatedDeviceManager`: 设备模拟器

**设备模型：**
```java
Device {
  id: Long
  deviceId: String (唯一标识)
  name: String
  type: DeviceType
  location: String
  building: String
  floor: String
  room: String
  status: DeviceStatus
  properties: Map<String, String>
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
}
```

#### 2. 场景编排服务 (Scene Service)

**职责：**
- 管理场景配置
- 执行场景动作序列
- 处理触发器逻辑
- 协调设备控制

**核心类：**
- `Scene`: 场景实体模型
- `Trigger`: 触发器模型
- `Action`: 动作模型
- `SceneService`: 场景业务逻辑
- `SceneExecutor`: 场景执行引擎

**场景模型：**
```java
Scene {
  id: Long
  sceneId: String
  name: String
  description: String
  status: SceneStatus
  triggers: List<Trigger>
  actions: List<Action>
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
}

Trigger {
  type: TriggerType
  deviceId: String
  condition: String
  parameters: Map<String, String>
}

Action {
  deviceId: String
  action: String
  parameters: Map<String, String>
  delaySeconds: Integer
  order: Integer
}
```

#### 3. 前端应用 (Frontend)

**职责：**
- 提供用户交互界面
- 可视化设备和场景管理
- 图形化场景编排设计器

**核心页面：**
- **Dashboard**: 仪表板，展示系统概览
- **DeviceManagement**: 设备管理页面
- **SceneManagement**: 场景列表和管理
- **SceneDesigner**: 可视化场景设计器

## 数据流

### 设备控制流程

```
用户操作 → 前端 → 设备服务 API → 设备服务 → 模拟设备管理器 → 更新设备状态
```

### 场景执行流程

```
触发场景 → 场景服务 → 场景执行引擎 → 遍历动作列表 → 
调用设备服务 API → 设备服务 → 执行设备控制 → 返回结果
```

## 设计模式

### 1. 仓储模式 (Repository Pattern)

使用 Spring Data JPA 实现数据访问层抽象：

```java
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceId(String deviceId);
    List<Device> findByType(DeviceType type);
}
```

### 2. 服务层模式 (Service Layer Pattern)

业务逻辑封装在服务层：

```java
@Service
public class DeviceService {
    @Transactional
    public Device createDevice(Device device) { ... }
    
    public boolean controlDevice(String deviceId, String action, Map params) { ... }
}
```

### 3. REST API 模式

使用 RESTful API 设计原则：

- GET: 查询资源
- POST: 创建资源
- PUT: 更新资源
- DELETE: 删除资源

### 4. 策略模式 (Strategy Pattern)

设备模拟器使用策略模式支持不同设备类型：

```java
public interface DeviceSimulator {
    boolean turnOn(String deviceId);
    boolean turnOff(String deviceId);
    boolean setState(String deviceId, Map<String, Object> state);
}
```

## 扩展性设计

### 1. 支持新设备类型

添加新设备类型只需：
1. 在 `DeviceType` 枚举中添加类型
2. 在 `SimulatedDeviceManager` 中添加初始化逻辑
3. 更新前端设备选项

### 2. 集成真实设备

通过适配器模式集成 Home Assistant：

```java
public class HomeAssistantAdapter implements DeviceSimulator {
    private final RestTemplate restTemplate;
    private final String homeAssistantUrl;
    
    @Override
    public boolean turnOn(String deviceId) {
        // 调用 Home Assistant API
        return callHomeAssistant(deviceId, "turn_on");
    }
}
```

### 3. 添加新触发器类型

1. 在 `TriggerType` 枚举中添加类型
2. 在场景执行引擎中实现触发器检测逻辑
3. 更新前端场景设计器

### 4. 添加新服务

可以轻松添加新的微服务：
- 用户管理服务
- 规则引擎服务
- 通知服务
- 数据分析服务

## 安全性考虑

### 当前实现

- CORS 配置允许跨域访问
- 基本的输入验证
- 异常处理和错误响应

### 未来改进

- 添加身份认证和授权（JWT）
- API 网关层统一鉴权
- 设备访问权限控制
- 数据加密传输（HTTPS）
- 审计日志

## 性能优化

### 当前优化

- H2 内存数据库（开发环境）
- JPA 二级缓存
- 异步场景执行
- 前端组件懒加载

### 未来优化

- Redis 缓存设备状态
- 消息队列处理设备事件
- 数据库连接池优化
- 前端资源 CDN 加速

## 可观测性

### 日志

使用 SLF4J + Logback 统一日志：
- DEBUG: 详细调试信息
- INFO: 关键操作日志
- ERROR: 错误和异常

### 监控

可集成：
- Spring Boot Actuator
- Prometheus + Grafana
- 分布式追踪（Zipkin）

## 部署架构

### Docker Compose 部署

```yaml
services:
  device-service: 端口 8081
  scene-service: 端口 8082
  frontend: 端口 8080
  
network: iot-network (bridge)
```

### 生产环境建议

- 使用 PostgreSQL/MySQL 替代 H2
- 添加 Redis 缓存层
- 使用 Nginx 作为反向代理
- 配置服务发现（Eureka/Consul）
- 部署到 Kubernetes 集群

## 技术栈选择理由

### 后端

**Spring Boot**: 
- 成熟的企业级框架
- 丰富的生态系统
- 易于开发和测试
- 良好的微服务支持

**Spring Data JPA**: 
- 简化数据访问
- 支持多种数据库
- 声明式事务管理

### 前端

**Vue 3**: 
- 轻量级且高性能
- 渐进式框架
- 优秀的开发体验
- Composition API 提高代码复用

**Element Plus**: 
- 丰富的组件库
- 中文文档完善
- 适合后台管理系统
- 支持主题定制

## 未来规划

1. **设备管理增强**
   - 设备分组管理
   - 设备拓扑图
   - 设备健康监控

2. **场景编排增强**
   - 可视化流程图编辑器
   - 条件分支和循环
   - 场景模板库

3. **规则引擎**
   - 复杂的条件判断
   - 定时任务调度
   - 事件驱动触发

4. **数据分析**
   - 设备使用统计
   - 能耗分析
   - 趋势预测

5. **移动端支持**
   - 响应式设计
   - 移动端应用
   - 小程序