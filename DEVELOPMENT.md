# 开发指南

## 开发环境设置

### 必需软件

1. **Java Development Kit (JDK) 17+**
   ```bash
   java -version
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **Node.js 18+ 和 npm**
   ```bash
   node -version
   npm -version
   ```

4. **Git**
   ```bash
   git --version
   ```

### 推荐工具

- **IDE**: IntelliJ IDEA (后端) / VS Code (前端)
- **API 测试**: Postman 或 Insomnia
- **容器**: Docker Desktop
- **版本控制**: Git

## 项目克隆和初始化

```bash
# 克隆项目
git clone https://github.com/shoutoutuoadi325/xiyuan.git
cd xiyuan

# 构建 common 模块
cd backend/common
mvn clean install

# 返回项目根目录
cd ../..
```

## 后端开发

### 1. Common 模块开发

Common 模块包含所有服务共享的数据模型和工具类。

**添加新的设备类型：**

```java
// 1. 在 DeviceType.java 中添加枚举
public enum DeviceType {
    // ...
    NEW_DEVICE("新设备类型");
}

// 2. 创建设备模型（如需要）
// 在 com.xiyuan.common.model 包中添加
```

**重新编译和安装：**

```bash
cd backend/common
mvn clean install
```

### 2. Device Service 开发

**添加新的模拟设备：**

```java
// 1. 创建设备类
// backend/device-service/src/main/java/com/xiyuan/device/simulator/NewDevice.java

public class NewDevice extends SimulatedDevice {
    public NewDevice(String id, String name, String location, String floor, String room) {
        super(id, name, DeviceType.NEW_DEVICE, location, floor, room);
    }

    @Override
    protected void initializeState() {
        // 初始化设备状态
        updateState("key", "value");
    }

    @Override
    public void executeAction(String action, Map<String, Object> parameters) {
        // 实现设备动作
        switch (action) {
            case "actionName":
                // 执行动作
                break;
        }
    }
}

// 2. 在 DeviceService.java 的 initializeSimulatedDevices() 中注册设备
addDevice(new NewDevice("new-001", "新设备1", "教学楼", "1楼", "101教室"));
```

**运行 Device Service：**

```bash
cd backend/device-service
mvn spring-boot:run
```

服务将在 http://localhost:8081 启动

**测试 API：**

```bash
# 获取所有设备
curl http://localhost:8081/api/devices

# 执行设备动作
curl -X POST http://localhost:8081/api/devices/light-101/action \
  -H "Content-Type: application/json" \
  -d '{"action":"turnOn","parameters":{"brightness":100}}'
```

### 3. Scene Service 开发

**运行 Scene Service：**

```bash
cd backend/scene-service
mvn spring-boot:run
```

服务将在 http://localhost:8082 启动

**测试 API：**

```bash
# 获取所有场景
curl http://localhost:8082/api/scenes

# 执行场景
curl -X POST http://localhost:8082/api/scenes/scene-class/execute
```

### 4. API Gateway 开发

**运行 API Gateway：**

```bash
cd backend/api-gateway
mvn spring-boot:run
```

网关将在 http://localhost:8080 启动

**路由配置：**

在 `application.yml` 中添加新路由：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: new-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/newservice/**
```

## 前端开发

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 开发模式运行

```bash
npm run dev
```

前端将在 http://localhost:3000 启动，并自动代理 API 请求到后端。

### 3. 项目结构说明

```
src/
├── components/       # 可复用组件
├── views/           # 页面视图（路由对应）
├── stores/          # Pinia 状态管理
├── utils/           # 工具函数和 API 封装
├── assets/          # 静态资源（样式、图片等）
├── App.vue          # 根组件
├── main.js          # 应用入口
└── router.js        # 路由配置
```

### 4. 添加新页面

```javascript
// 1. 创建视图组件 src/views/NewView.vue
<template>
  <div>
    <h2>新页面</h2>
  </div>
</template>

<script setup>
// 页面逻辑
</script>

// 2. 在 router.js 中添加路由
{
  path: '/new',
  name: 'new',
  component: () => import('./views/NewView.vue')
}

// 3. 在 App.vue 导航栏添加链接
<router-link to="/new" class="navbar-item">新功能</router-link>
```

### 5. 添加新的 API 调用

```javascript
// 在 src/utils/api.js 中添加
export const newApi = {
  getData: () => apiClient.get('/new/data'),
  postData: (data) => apiClient.post('/new/data', data)
}

// 在组件中使用
import { newApi } from '@/utils/api'

const fetchData = async () => {
  const response = await newApi.getData()
  console.log(response.data)
}
```

### 6. 创建新的 Store

```javascript
// src/stores/newStore.js
import { defineStore } from 'pinia'
import { newApi } from '../utils/api'

export const useNewStore = defineStore('new', {
  state: () => ({
    data: [],
    loading: false
  }),

  actions: {
    async fetchData() {
      this.loading = true
      try {
        const response = await newApi.getData()
        this.data = response.data
      } finally {
        this.loading = false
      }
    }
  }
})
```

### 7. 样式开发

在 `src/assets/style.css` 中添加全局样式，或在组件的 `<style scoped>` 中添加局部样式。

## 调试技巧

### 后端调试

1. **使用 IntelliJ IDEA 调试**
   - 设置断点
   - 以 Debug 模式运行 Spring Boot 应用

2. **查看日志**
   ```bash
   # 调整日志级别
   # 在 application.yml 中添加
   logging:
     level:
       com.xiyuan: DEBUG
   ```

3. **使用 Actuator 监控**
   ```bash
   # 健康检查
   curl http://localhost:8081/actuator/health
   
   # 查看所有端点
   curl http://localhost:8081/actuator
   ```

### 前端调试

1. **使用 Vue DevTools**
   - 安装浏览器扩展
   - 查看组件树、状态、事件

2. **控制台调试**
   ```javascript
   console.log('Debug info:', variable)
   debugger // 设置断点
   ```

3. **网络请求调试**
   - 使用浏览器开发者工具的 Network 标签
   - 查看 Axios 请求和响应

## 代码规范

### Java 代码规范

- 使用 4 空格缩进
- 类名使用 PascalCase
- 方法名使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 使用 Lombok 减少样板代码

### JavaScript 代码规范

- 使用 2 空格缩进
- 使用 const/let，避免 var
- 函数使用箭头函数或 function 关键字
- 组件名使用 PascalCase
- 文件名使用 camelCase 或 kebab-case

## 测试

### 后端测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=DeviceServiceTest
```

### 前端测试

```bash
# 运行测试（待添加测试框架）
npm run test
```

## 构建和部署

### 后端构建

```bash
# 构建单个服务
cd backend/device-service
mvn clean package

# JAR 文件位于 target/ 目录
```

### 前端构建

```bash
cd frontend
npm run build

# 构建产物位于 dist/ 目录
```

### Docker 构建

```bash
# 构建所有服务
docker-compose build

# 构建单个服务
docker-compose build device-service
```

## 常见问题

### Q: Maven 依赖下载失败？

A: 配置国内镜像源，在 `~/.m2/settings.xml` 中添加：

```xml
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### Q: npm install 很慢？

A: 使用淘宝镜像：

```bash
npm config set registry https://registry.npmmirror.com
```

### Q: 跨域问题？

A: 确保后端服务已配置 CORS，或在开发模式下使用 Vite 代理。

### Q: 服务间调用失败？

A: 检查服务是否都已启动，以及 application.yml 中的 URL 配置是否正确。

## Git 工作流

### 分支策略

- `main`: 主分支，稳定版本
- `develop`: 开发分支
- `feature/*`: 功能分支
- `bugfix/*`: 修复分支

### 提交规范

```
类型(范围): 简短描述

详细描述（可选）

关联 Issue: #123
```

类型：
- feat: 新功能
- fix: 修复
- docs: 文档
- style: 格式
- refactor: 重构
- test: 测试
- chore: 构建/工具

## 性能优化建议

### 后端优化

1. 使用连接池管理数据库连接
2. 添加缓存层（Redis）
3. 异步处理长时间任务
4. 优化 SQL 查询

### 前端优化

1. 使用虚拟滚动处理大列表
2. 懒加载路由和组件
3. 优化图片和资源加载
4. 使用 Web Workers 处理复杂计算

## 下一步

- 阅读 [ARCHITECTURE.md](ARCHITECTURE.md) 了解系统架构
- 查看 [API 文档](#) 了解接口详情
- 加入开发者社区讨论

## 获取帮助

- 提交 Issue: https://github.com/shoutoutuoadi325/xiyuan/issues
- 查看文档: https://github.com/shoutoutuoadi325/xiyuan/wiki
