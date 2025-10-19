# SQL Server 数据库迁移方案

## 1. SQL Server 方案优势分析

### 🎯 为什么选择 SQL Server

**企业级特性**
- ✅ 强大的事务处理能力，支持 ACID 特性
- ✅ 优秀的并发控制和锁机制
- ✅ 内置的高可用性和灾难恢复功能
- ✅ 企业级安全特性和权限管理

**性能优势**
- ✅ 智能查询优化器，自动优化查询性能
- ✅ 内存优化表和列存储索引
- ✅ 支持分区表，处理大数据量
- ✅ 内置缓存机制，提升查询速度

**开发友好**
- ✅ 与 Spring Boot 完美集成
- ✅ 丰富的数据类型支持
- ✅ 强大的 T-SQL 语言
- ✅ 优秀的管理工具（SSMS）

**IoT 平台适配性**
- ✅ 支持 JSON 数据类型，适合设备配置存储
- ✅ 时序数据处理能力
- ✅ 全文搜索功能
- ✅ 地理空间数据支持

## 2. 环境准备和 SQL Server 安装

### 2.1 Docker 方式安装（推荐）

**优势**: 快速部署、环境隔离、易于管理

```bash
# 拉取 SQL Server 2022 镜像
docker pull mcr.microsoft.com/mssql/server:2022-latest

# 创建 SQL Server 容器
docker run -e "ACCEPT_EULA=Y" \
  -e "MSSQL_SA_PASSWORD=YourStrong@Passw0rd" \
  -p 1433:1433 \
  --name sqlserver \
  --hostname sqlserver \
  -d mcr.microsoft.com/mssql/server:2022-latest
```

### 2.2 本地安装方式

**下载地址**: https://www.microsoft.com/zh-cn/sql-server/sql-server-downloads

**安装步骤**:
1. 下载 SQL Server 2022 Developer Edition（免费）
2. 运行安装程序，选择"基本"安装类型
3. 设置 SA 密码（至少8位，包含大小写字母、数字和特殊字符）
4. 安装 SQL Server Management Studio (SSMS)

### 2.3 连接验证

```bash
# 使用 sqlcmd 测试连接
sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd"

# 或使用 Docker 容器内的 sqlcmd
docker exec -it sqlserver /opt/mssql-tools/bin/sqlcmd \
  -S localhost -U sa -P "YourStrong@Passw0rd"
```

## 3. Docker Compose 配置更新

### 3.1 更新 docker-compose.yml

```yaml
version: '3.8'

services:
  # SQL Server 数据库
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2022-latest
    container_name: sqlserver
    environment:
      - ACCEPT_EULA=Y
      - MSSQL_SA_PASSWORD=YourStrong@Passw0rd
      - MSSQL_PID=Developer
    ports:
      - "1433:1433"
    volumes:
      - sqlserver_data:/var/opt/mssql
      - ./sql/init:/docker-entrypoint-initdb.d
    networks:
      - iot-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -Q 'SELECT 1'"]
      interval: 30s
      timeout: 10s
      retries: 5

  # MQTT Broker
  mosquitto:
    image: eclipse-mosquitto:2.0
    container_name: mqtt-broker
    ports:
      - "1883:1883"
      - "9001:9001"
    volumes:
      - ./mosquitto/config:/mosquitto/config
      - ./mosquitto/data:/mosquitto/data
      - ./mosquitto/log:/mosquitto/log
    networks:
      - iot-network
    restart: unless-stopped
    
  # 设备服务
  device-service:
    build: 
      context: ./backend/device-service
      dockerfile: Dockerfile
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MQTT_BROKER_URL=tcp://mosquitto:1883
      - DB_HOST=sqlserver
      - DB_PORT=1433
      - DB_NAME=devicedb
      - DB_USERNAME=sa
      - DB_PASSWORD=YourStrong@Passw0rd
    depends_on:
      sqlserver:
        condition: service_healthy
      mosquitto:
        condition: service_started
    networks:
      - iot-network
    restart: unless-stopped
    
  # 场景服务
  scene-service:
    build: 
      context: ./backend/scene-service
      dockerfile: Dockerfile
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DEVICE_SERVICE_URL=http://device-service:8081
      - DB_HOST=sqlserver
      - DB_PORT=1433
      - DB_NAME=scenedb
      - DB_USERNAME=sa
      - DB_PASSWORD=YourStrong@Passw0rd
    depends_on:
      sqlserver:
        condition: service_healthy
      device-service:
        condition: service_started
    networks:
      - iot-network
    restart: unless-stopped
  
  # 设备模拟器
  device-simulator:
    build:
      context: ./device-simulator
      dockerfile: Dockerfile
    environment:
      - MQTT_BROKER_URL=tcp://mosquitto:1883
    depends_on:
      - mosquitto
      - device-service
    networks:
      - iot-network
    restart: unless-stopped
    
  # 前端服务
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "8080:80"
    depends_on:
      - device-service
      - scene-service
    networks:
      - iot-network
    restart: unless-stopped

volumes:
  sqlserver_data:
    driver: local

networks:
  iot-network:
    driver: bridge
```

### 3.2 创建初始化脚本目录

```bash
# 创建 SQL 初始化脚本目录
mkdir -p sql/init

# 创建数据库初始化脚本
touch sql/init/01-create-databases.sql
touch sql/init/02-create-tables.sql
touch sql/init/03-insert-sample-data.sql
```

## 4. Spring Boot 配置修改

### 4.1 更新 pom.xml 依赖

**设备服务 pom.xml** (`backend/device-service/pom.xml`):

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- SQL Server JDBC 驱动 -->
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- HikariCP 连接池 -->
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
    </dependency>
    
    <!-- 移除 H2 依赖 -->
    <!-- 
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    -->
    
    <!-- 其他依赖保持不变 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-integration</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-mqtt</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 4.2 更新 application.yml 配置

**设备服务配置** (`backend/device-service/src/main/resources/application.yml`):

```yaml
server:
  port: 8081

spring:
  application:
    name: device-service
  
  # SQL Server 数据源配置
  datasource:
    url: jdbc:sqlserver://${DB_HOST:localhost}:${DB_PORT:1433};databaseName=${DB_NAME:devicedb};encrypt=false;trustServerCertificate=true
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:YourStrong@Passw0rd}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    
    # HikariCP 连接池配置
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
      leak-detection-threshold: 60000
      pool-name: DeviceServiceHikariCP
      
  # JPA 配置
  jpa:
    hibernate:
      ddl-auto: update
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServerDialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
        
  # SQL Server 特定配置
  sql:
    init:
      mode: always
      continue-on-error: true

# MQTT 配置
mqtt:
  broker:
    url: ${MQTT_BROKER_URL:tcp://localhost:1883}
  client:
    id: device-service-${random.uuid}
  username: ${MQTT_USERNAME:}
  password: ${MQTT_PASSWORD:}

# 服务发现配置
eureka:
  client:
    enabled: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

# 日志配置
logging:
  level:
    com.xiyuan.iot: INFO
    org.springframework.integration: WARN
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    com.microsoft.sqlserver: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# 管理端点配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

**场景服务配置** (`backend/scene-service/src/main/resources/application.yml`):

```yaml
server:
  port: 8082

spring:
  application:
    name: scene-service
  
  # SQL Server 数据源配置
  datasource:
    url: jdbc:sqlserver://${DB_HOST:localhost}:${DB_PORT:1433};databaseName=${DB_NAME:scenedb};encrypt=false;trustServerCertificate=true
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:YourStrong@Passw0rd}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    
    # HikariCP 连接池配置
    hikari:
      maximum-pool-size: 15
      minimum-idle: 3
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
      pool-name: SceneServiceHikariCP
      
  # JPA 配置
  jpa:
    hibernate:
      ddl-auto: update
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServerDialect
        format_sql: true
        jdbc:
          batch_size: 15

# 设备服务配置
device:
  service:
    url: ${DEVICE_SERVICE_URL:http://localhost:8081}

# 服务发现配置
eureka:
  client:
    enabled: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

# 日志配置
logging:
  level:
    com.xiyuan.iot: INFO
    org.hibernate.SQL: DEBUG
    com.microsoft.sqlserver: INFO
```

## 5. 数据库初始化脚本

### 5.1 创建数据库脚本

**文件**: `sql/init/01-create-databases.sql`

```sql
-- 创建设备数据库
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'devicedb')
BEGIN
    CREATE DATABASE devicedb;
END
GO

-- 创建场景数据库
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'scenedb')
BEGIN
    CREATE DATABASE scenedb;
END
GO

-- 使用设备数据库
USE devicedb;
GO

-- 创建设备服务用户（可选）
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'device_user')
BEGIN
    CREATE LOGIN device_user WITH PASSWORD = 'Device@123456';
    CREATE USER device_user FOR LOGIN device_user;
    ALTER ROLE db_datareader ADD MEMBER device_user;
    ALTER ROLE db_datawriter ADD MEMBER device_user;
    ALTER ROLE db_ddladmin ADD MEMBER device_user;
END
GO

-- 使用场景数据库
USE scenedb;
GO

-- 创建场景服务用户（可选）
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'scene_user')
BEGIN
    CREATE LOGIN scene_user WITH PASSWORD = 'Scene@123456';
    CREATE USER scene_user FOR LOGIN scene_user;
    ALTER ROLE db_datareader ADD MEMBER scene_user;
    ALTER ROLE db_datawriter ADD MEMBER scene_user;
    ALTER ROLE db_ddladmin ADD MEMBER scene_user;
END
GO
```

### 5.2 创建表结构脚本

**文件**: `sql/init/02-create-tables.sql`

```sql
-- 使用设备数据库
USE devicedb;
GO

-- 设备类型模型表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='device_type_models' AND xtype='U')
BEGIN
    CREATE TABLE device_type_models (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        type_identifier NVARCHAR(100) NOT NULL UNIQUE,
        type_name NVARCHAR(200) NOT NULL,
        description NVARCHAR(1000),
        category NVARCHAR(100),
        manufacturer NVARCHAR(200),
        model NVARCHAR(200),
        version NVARCHAR(50),
        icon_url NVARCHAR(500),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
    
    CREATE INDEX IX_device_type_models_category ON device_type_models(category);
    CREATE INDEX IX_device_type_models_manufacturer ON device_type_models(manufacturer);
END
GO

-- 设备属性定义表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='device_property_definitions' AND xtype='U')
BEGIN
    CREATE TABLE device_property_definitions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        device_type_model_id BIGINT NOT NULL,
        property_name NVARCHAR(100) NOT NULL,
        property_identifier NVARCHAR(100) NOT NULL,
        data_type NVARCHAR(50) NOT NULL,
        unit NVARCHAR(50),
        min_value NVARCHAR(100),
        max_value NVARCHAR(100),
        default_value NVARCHAR(100),
        is_required BIT DEFAULT 0,
        is_readonly BIT DEFAULT 0,
        description NVARCHAR(500),
        created_at DATETIME2 DEFAULT GETDATE(),
        
        FOREIGN KEY (device_type_model_id) REFERENCES device_type_models(id) ON DELETE CASCADE
    );
    
    CREATE INDEX IX_device_property_definitions_type_id ON device_property_definitions(device_type_model_id);
END
GO

-- 设备操作定义表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='device_operation_definitions' AND xtype='U')
BEGIN
    CREATE TABLE device_operation_definitions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        device_type_model_id BIGINT NOT NULL,
        operation_name NVARCHAR(100) NOT NULL,
        operation_identifier NVARCHAR(100) NOT NULL,
        description NVARCHAR(500),
        parameters NVARCHAR(MAX), -- JSON格式存储参数定义
        return_type NVARCHAR(50),
        is_async BIT DEFAULT 0,
        timeout_seconds INT DEFAULT 30,
        created_at DATETIME2 DEFAULT GETDATE(),
        
        FOREIGN KEY (device_type_model_id) REFERENCES device_type_models(id) ON DELETE CASCADE
    );
    
    CREATE INDEX IX_device_operation_definitions_type_id ON device_operation_definitions(device_type_model_id);
END
GO

-- 设备事件定义表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='device_event_definitions' AND xtype='U')
BEGIN
    CREATE TABLE device_event_definitions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        device_type_model_id BIGINT NOT NULL,
        event_name NVARCHAR(100) NOT NULL,
        event_identifier NVARCHAR(100) NOT NULL,
        description NVARCHAR(500),
        event_level NVARCHAR(20) DEFAULT 'INFO', -- INFO, WARN, ERROR
        parameters NVARCHAR(MAX), -- JSON格式存储事件参数
        created_at DATETIME2 DEFAULT GETDATE(),
        
        FOREIGN KEY (device_type_model_id) REFERENCES device_type_models(id) ON DELETE CASCADE
    );
    
    CREATE INDEX IX_device_event_definitions_type_id ON device_event_definitions(device_type_model_id);
END
GO

-- 设备表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='devices' AND xtype='U')
BEGIN
    CREATE TABLE devices (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        device_id NVARCHAR(100) NOT NULL UNIQUE,
        device_name NVARCHAR(200) NOT NULL,
        device_type NVARCHAR(100) NOT NULL,
        status NVARCHAR(20) DEFAULT 'OFFLINE',
        location NVARCHAR(200),
        description NVARCHAR(500),
        properties NVARCHAR(MAX), -- JSON格式存储设备属性
        last_seen DATETIME2,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
    
    CREATE INDEX IX_devices_device_type ON devices(device_type);
    CREATE INDEX IX_devices_status ON devices(status);
    CREATE INDEX IX_devices_location ON devices(location);
END
GO

-- 使用场景数据库
USE scenedb;
GO

-- 场景表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='scenes' AND xtype='U')
BEGIN
    CREATE TABLE scenes (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        scene_id NVARCHAR(100) NOT NULL UNIQUE,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(1000),
        status NVARCHAR(20) DEFAULT 'INACTIVE', -- ACTIVE, INACTIVE
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
    
    CREATE INDEX IX_scenes_status ON scenes(status);
END
GO

-- 触发器表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='triggers' AND xtype='U')
BEGIN
    CREATE TABLE triggers (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        scene_id BIGINT NOT NULL,
        trigger_type NVARCHAR(50) NOT NULL, -- TIME, DEVICE_STATE, MANUAL
        trigger_config NVARCHAR(MAX), -- JSON格式存储触发器配置
        is_enabled BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE(),
        
        FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE
    );
    
    CREATE INDEX IX_triggers_scene_id ON triggers(scene_id);
    CREATE INDEX IX_triggers_type ON triggers(trigger_type);
END
GO

-- 动作表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='actions' AND xtype='U')
BEGIN
    CREATE TABLE actions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        scene_id BIGINT NOT NULL,
        device_id NVARCHAR(100) NOT NULL,
        action NVARCHAR(100) NOT NULL,
        parameters NVARCHAR(MAX), -- JSON格式存储动作参数
        execution_order INT DEFAULT 0,
        delay_seconds INT DEFAULT 0,
        created_at DATETIME2 DEFAULT GETDATE(),
        
        FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE
    );
    
    CREATE INDEX IX_actions_scene_id ON actions(scene_id);
    CREATE INDEX IX_actions_device_id ON actions(device_id);
END
GO
```

### 5.3 插入示例数据脚本

**文件**: `sql/init/03-insert-sample-data.sql`

```sql
-- 使用设备数据库
USE devicedb;
GO

-- 插入设备类型模型示例数据
IF NOT EXISTS (SELECT * FROM device_type_models WHERE type_identifier = 'smart_bulb')
BEGIN
    INSERT INTO device_type_models (type_identifier, type_name, description, category, manufacturer, model, version)
    VALUES 
    ('smart_bulb', '智能灯泡', '支持调光调色的智能LED灯泡', '照明设备', 'Philips', 'Hue White and Color', '1.0'),
    ('temperature_sensor', '温度传感器', '高精度数字温度传感器', '传感器', 'Xiaomi', 'WSDCGQ11LM', '1.0'),
    ('smart_lock', '智能门锁', '指纹识别智能门锁', '安防设备', 'Aqara', 'ZNMS12LM', '1.0'),
    ('air_conditioner', '智能空调', '变频智能空调', '家电设备', 'Gree', 'KFR-35GW', '1.0'),
    ('security_camera', '安防摄像头', '1080P高清网络摄像头', '安防设备', 'Hikvision', 'DS-2CD2T25FWD-I5', '1.0');
END
GO

-- 插入设备属性定义
DECLARE @bulb_id BIGINT = (SELECT id FROM device_type_models WHERE type_identifier = 'smart_bulb');
DECLARE @temp_id BIGINT = (SELECT id FROM device_type_models WHERE type_identifier = 'temperature_sensor');
DECLARE @lock_id BIGINT = (SELECT id FROM device_type_models WHERE type_identifier = 'smart_lock');

IF @bulb_id IS NOT NULL AND NOT EXISTS (SELECT * FROM device_property_definitions WHERE device_type_model_id = @bulb_id)
BEGIN
    INSERT INTO device_property_definitions (device_type_model_id, property_name, property_identifier, data_type, unit, min_value, max_value, default_value, description)
    VALUES 
    (@bulb_id, '开关状态', 'power', 'BOOLEAN', '', 'false', 'true', 'false', '灯泡开关状态'),
    (@bulb_id, '亮度', 'brightness', 'INTEGER', '%', '0', '100', '50', '灯泡亮度百分比'),
    (@bulb_id, '色温', 'color_temp', 'INTEGER', 'K', '2700', '6500', '4000', '灯泡色温值'),
    (@bulb_id, '颜色', 'color', 'STRING', '', '', '', '#FFFFFF', '灯泡颜色RGB值');
END
GO

IF @temp_id IS NOT NULL AND NOT EXISTS (SELECT * FROM device_property_definitions WHERE device_type_model_id = @temp_id)
BEGIN
    INSERT INTO device_property_definitions (device_type_model_id, property_name, property_identifier, data_type, unit, min_value, max_value, is_readonly, description)
    VALUES 
    (@temp_id, '温度', 'temperature', 'FLOAT', '°C', '-40', '80', 1, '环境温度'),
    (@temp_id, '湿度', 'humidity', 'FLOAT', '%', '0', '100', 1, '环境湿度'),
    (@temp_id, '电池电量', 'battery', 'INTEGER', '%', '0', '100', 1, '传感器电池电量');
END
GO

-- 插入设备操作定义
IF @bulb_id IS NOT NULL AND NOT EXISTS (SELECT * FROM device_operation_definitions WHERE device_type_model_id = @bulb_id)
BEGIN
    INSERT INTO device_operation_definitions (device_type_model_id, operation_name, operation_identifier, description, parameters)
    VALUES 
    (@bulb_id, '开灯', 'turn_on', '打开灯泡', '{}'),
    (@bulb_id, '关灯', 'turn_off', '关闭灯泡', '{}'),
    (@bulb_id, '设置亮度', 'set_brightness', '设置灯泡亮度', '{"brightness": {"type": "integer", "min": 0, "max": 100}}'),
    (@bulb_id, '设置颜色', 'set_color', '设置灯泡颜色', '{"color": {"type": "string", "pattern": "^#[0-9A-Fa-f]{6}$"}}');
END
GO

-- 插入示例设备
IF NOT EXISTS (SELECT * FROM devices WHERE device_id = 'bulb_living_room_001')
BEGIN
    INSERT INTO devices (device_id, device_name, device_type, status, location, description, properties)
    VALUES 
    ('bulb_living_room_001', '客厅主灯', 'smart_bulb', 'ONLINE', '客厅', '客厅中央吸顶灯', '{"power": false, "brightness": 50, "color_temp": 4000, "color": "#FFFFFF"}'),
    ('bulb_bedroom_001', '卧室台灯', 'smart_bulb', 'ONLINE', '主卧', '床头台灯', '{"power": false, "brightness": 30, "color_temp": 3000, "color": "#FFFFFF"}'),
    ('temp_living_room_001', '客厅温度传感器', 'temperature_sensor', 'ONLINE', '客厅', '客厅环境监测', '{"temperature": 23.5, "humidity": 45.2, "battery": 85}'),
    ('lock_front_door_001', '前门智能锁', 'smart_lock', 'ONLINE', '玄关', '入户门智能锁', '{"locked": true, "battery": 78}');
END
GO

-- 使用场景数据库
USE scenedb;
GO

-- 插入示例场景
IF NOT EXISTS (SELECT * FROM scenes WHERE scene_id = 'scene_home_mode')
BEGIN
    INSERT INTO scenes (scene_id, name, description, status)
    VALUES 
    ('scene_home_mode', '回家模式', '回家时自动开启客厅灯光，调节到舒适亮度', 'ACTIVE'),
    ('scene_sleep_mode', '睡眠模式', '睡觉时关闭所有灯光，确保门锁锁定', 'ACTIVE'),
    ('scene_away_mode', '离家模式', '离家时关闭所有设备，启用安防模式', 'INACTIVE');
END
GO

-- 插入示例触发器
DECLARE @home_scene_id BIGINT = (SELECT id FROM scenes WHERE scene_id = 'scene_home_mode');
DECLARE @sleep_scene_id BIGINT = (SELECT id FROM scenes WHERE scene_id = 'scene_sleep_mode');

IF @home_scene_id IS NOT NULL AND NOT EXISTS (SELECT * FROM triggers WHERE scene_id = @home_scene_id)
BEGIN
    INSERT INTO triggers (scene_id, trigger_type, trigger_config)
    VALUES 
    (@home_scene_id, 'MANUAL', '{"description": "手动触发回家模式"}'),
    (@home_scene_id, 'TIME', '{"time": "18:00", "days": ["MON", "TUE", "WED", "THU", "FRI"]}');
END
GO

IF @sleep_scene_id IS NOT NULL AND NOT EXISTS (SELECT * FROM triggers WHERE scene_id = @sleep_scene_id)
BEGIN
    INSERT INTO triggers (scene_id, trigger_type, trigger_config)
    VALUES 
    (@sleep_scene_id, 'TIME', '{"time": "23:00", "days": ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"]}');
END
GO

-- 插入示例动作
IF @home_scene_id IS NOT NULL AND NOT EXISTS (SELECT * FROM actions WHERE scene_id = @home_scene_id)
BEGIN
    INSERT INTO actions (scene_id, device_id, action, parameters, execution_order)
    VALUES 
    (@home_scene_id, 'bulb_living_room_001', 'turn_on', '{}', 1),
    (@home_scene_id, 'bulb_living_room_001', 'set_brightness', '{"brightness": 70}', 2),
    (@home_scene_id, 'bulb_living_room_001', 'set_color_temp', '{"color_temp": 3500}', 3);
END
GO

IF @sleep_scene_id IS NOT NULL AND NOT EXISTS (SELECT * FROM actions WHERE scene_id = @sleep_scene_id)
BEGIN
    INSERT INTO actions (scene_id, device_id, action, parameters, execution_order)
    VALUES 
    (@sleep_scene_id, 'bulb_living_room_001', 'turn_off', '{}', 1),
    (@sleep_scene_id, 'bulb_bedroom_001', 'turn_off', '{}', 2);
END
GO

PRINT 'SQL Server 数据库初始化完成！';
```

## 6. 从 H2 到 SQL Server 的数据迁移步骤

### 6.1 迁移准备

```bash
# 1. 备份当前 H2 数据（如果有重要数据）
# H2 数据在内存中，重启后会丢失，所以主要是导出表结构

# 2. 停止当前服务
docker-compose down

# 3. 清理旧的容器和卷
docker system prune -f
docker volume prune -f
```

### 6.2 执行迁移

```bash
# 1. 创建 SQL 脚本目录
mkdir -p sql/init

# 2. 复制初始化脚本到目录
# (将上面的 SQL 脚本保存到对应文件)

# 3. 更新 Docker Compose 配置
# (使用上面提供的 docker-compose.yml)

# 4. 启动 SQL Server
docker-compose up -d sqlserver

# 5. 等待 SQL Server 启动完成
docker-compose logs -f sqlserver

# 6. 验证数据库连接
docker exec -it sqlserver /opt/mssql-tools/bin/sqlcmd \
  -S localhost -U sa -P "YourStrong@Passw0rd" \
  -Q "SELECT name FROM sys.databases;"

# 7. 启动应用服务
docker-compose up -d
```

### 6.3 迁移验证

```bash
# 1. 检查服务状态
docker-compose ps

# 2. 查看应用日志
docker-compose logs device-service
docker-compose logs scene-service

# 3. 测试 API 接口
curl http://localhost:8081/api/device-types
curl http://localhost:8082/api/scenes

# 4. 检查数据库表
docker exec -it sqlserver /opt/mssql-tools/bin/sqlcmd \
  -S localhost -U sa -P "YourStrong@Passw0rd" \
  -d devicedb \
  -Q "SELECT COUNT(*) FROM device_type_models;"
```

## 7. 连接池和性能优化配置

### 7.1 HikariCP 连接池优化

```yaml
spring:
  datasource:
    hikari:
      # 连接池大小配置
      maximum-pool-size: 20          # 最大连接数
      minimum-idle: 5                # 最小空闲连接数
      
      # 超时配置
      connection-timeout: 20000      # 连接超时时间(ms)
      idle-timeout: 300000           # 空闲超时时间(ms)
      max-lifetime: 1200000          # 连接最大生命周期(ms)
      
      # 性能配置
      leak-detection-threshold: 60000 # 连接泄漏检测阈值(ms)
      validation-timeout: 5000       # 连接验证超时时间(ms)
      
      # 连接池名称
      pool-name: IoTPlatformHikariCP
      
      # 连接属性
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false
```

### 7.2 JPA 性能优化

```yaml
spring:
  jpa:
    properties:
      hibernate:
        # 批处理配置
        jdbc:
          batch_size: 20
          batch_versioned_data: true
        order_inserts: true
        order_updates: true
        
        # 查询优化
        use_sql_comments: true
        format_sql: false
        show_sql: false
        
        # 缓存配置
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
        
        # 连接释放模式
        connection:
          release_mode: after_transaction
```

### 7.3 SQL Server 索引优化

```sql
-- 为常用查询创建索引
USE devicedb;
GO

-- 设备查询索引
CREATE INDEX IX_devices_type_status ON devices(device_type, status);
CREATE INDEX IX_devices_location_status ON devices(location, status);

-- 设备类型查询索引
CREATE INDEX IX_device_type_models_category_manufacturer ON device_type_models(category, manufacturer);

-- 属性定义查询索引
CREATE INDEX IX_device_property_definitions_type_identifier ON device_property_definitions(device_type_model_id, property_identifier);

USE scenedb;
GO

-- 场景查询索引
CREATE INDEX IX_scenes_status_name ON scenes(status, name);

-- 触发器查询索引
CREATE INDEX IX_triggers_scene_enabled ON triggers(scene_id, is_enabled);

-- 动作查询索引
CREATE INDEX IX_actions_scene_order ON actions(scene_id, execution_order);
```

## 8. 监控和备份策略

### 8.1 数据库监控

**性能监控查询**:

```sql
-- 查看当前连接数
SELECT 
    DB_NAME(dbid) as DatabaseName,
    COUNT(dbid) as NumberOfConnections,
    loginame as LoginName
FROM sys.sysprocesses 
WHERE dbid > 0 
GROUP BY dbid, loginame;

-- 查看慢查询
SELECT TOP 10
    total_elapsed_time/execution_count AS avg_elapsed_time,
    total_logical_reads/execution_count AS avg_logical_reads,
    execution_count,
    SUBSTRING(st.text, (qs.statement_start_offset/2)+1,
        ((CASE qs.statement_end_offset
            WHEN -1 THEN DATALENGTH(st.text)
            ELSE qs.statement_end_offset
        END - qs.statement_start_offset)/2) + 1) AS statement_text
FROM sys.dm_exec_query_stats AS qs
CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) AS st
ORDER BY avg_elapsed_time DESC;

-- 查看数据库大小
SELECT 
    DB_NAME() AS DatabaseName,
    (SELECT SUM(size) FROM sys.database_files WHERE type = 0) * 8 / 1024 AS DataFileSizeMB,
    (SELECT SUM(size) FROM sys.database_files WHERE type = 1) * 8 / 1024 AS LogFileSizeMB;
```

### 8.2 自动备份配置

**Docker Compose 备份服务**:

```yaml
# 添加到 docker-compose.yml
services:
  # ... 其他服务 ...
  
  # 数据库备份服务
  sqlserver-backup:
    image: mcr.microsoft.com/mssql-tools
    container_name: sqlserver-backup
    environment:
      - SQLCMDSERVER=sqlserver
      - SQLCMDUSER=sa
      - SQLCMDPASSWORD=YourStrong@Passw0rd
    volumes:
      - ./backups:/backups
      - ./scripts/backup.sh:/backup.sh
    depends_on:
      - sqlserver
    networks:
      - iot-network
    # 每天凌晨2点执行备份
    command: >
      sh -c "
        while true; do
          sleep 7200;
          /backup.sh;
        done
      "
```

**备份脚本** (`scripts/backup.sh`):

```bash
#!/bin/bash

# 设置变量
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
SERVER="sqlserver"
USERNAME="sa"
PASSWORD="YourStrong@Passw0rd"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份设备数据库
echo "开始备份设备数据库..."
sqlcmd -S $SERVER -U $USERNAME -P $PASSWORD -Q "
BACKUP DATABASE devicedb 
TO DISK = '/backups/devicedb_$DATE.bak'
WITH FORMAT, INIT, COMPRESSION;"

# 备份场景数据库
echo "开始备份场景数据库..."
sqlcmd -S $SERVER -U $USERNAME -P $PASSWORD -Q "
BACKUP DATABASE scenedb 
TO DISK = '/backups/scenedb_$DATE.bak'
WITH FORMAT, INIT, COMPRESSION;"

# 清理7天前的备份
find $BACKUP_DIR -name "*.bak" -mtime +7 -delete

echo "备份完成: $DATE"
```

### 8.3 恢复脚本

**数据库恢复脚本** (`scripts/restore.sh`):

```bash
#!/bin/bash

# 使用方法: ./restore.sh devicedb_20241215_020000.bak

if [ $# -eq 0 ]; then
    echo "使用方法: $0 <backup_file>"
    echo "示例: $0 devicedb_20241215_020000.bak"
    exit 1
fi

BACKUP_FILE=$1
SERVER="sqlserver"
USERNAME="sa"
PASSWORD="YourStrong@Passw0rd"

# 检查备份文件是否存在
if [ ! -f "/backups/$BACKUP_FILE" ]; then
    echo "错误: 备份文件 $BACKUP_FILE 不存在"
    exit 1
fi

# 确定数据库名称
if [[ $BACKUP_FILE == *"devicedb"* ]]; then
    DATABASE="devicedb"
elif [[ $BACKUP_FILE == *"scenedb"* ]]; then
    DATABASE="scenedb"
else
    echo "错误: 无法从文件名确定数据库类型"
    exit 1
fi

echo "开始恢复数据库 $DATABASE..."

# 设置数据库为单用户模式
sqlcmd -S $SERVER -U $USERNAME -P $PASSWORD -Q "
ALTER DATABASE $DATABASE SET SINGLE_USER WITH ROLLBACK IMMEDIATE;"

# 恢复数据库
sqlcmd -S $SERVER -U $USERNAME -P $PASSWORD -Q "
RESTORE DATABASE $DATABASE 
FROM DISK = '/backups/$BACKUP_FILE'
WITH REPLACE;"

# 设置数据库为多用户模式
sqlcmd -S $SERVER -U $USERNAME -P $PASSWORD -Q "
ALTER DATABASE $DATABASE SET MULTI_USER;"

echo "数据库 $DATABASE 恢复完成"
```

## 9. 开发和生产环境配置

### 9.1 开发环境配置

**application-dev.yml**:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=devicedb_dev;encrypt=false;trustServerCertificate=true
    username: sa
    password: YourStrong@Passw0rd
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      
  jpa:
    hibernate:
      ddl-auto: create-drop  # 开发环境可以重建表
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    com.xiyuan.iot: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 9.2 生产环境配置

**application-prod.yml**:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME};encrypt=true;trustServerCertificate=false
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      leak-detection-threshold: 60000
      
  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境不自动修改表结构
    show-sql: false
    properties:
      hibernate:
        format_sql: false

logging:
  level:
    com.xiyuan.iot: INFO
    org.hibernate.SQL: WARN
  file:
    name: /var/log/iot-platform/application.log
    max-size: 100MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### 9.3 环境变量配置

**生产环境 .env 文件**:

```bash
# 数据库配置
DB_HOST=sqlserver-prod.company.com
DB_PORT=1433
DB_NAME=devicedb_prod
DB_USERNAME=iot_app_user
DB_PASSWORD=SecurePassword@2024

# MQTT 配置
MQTT_BROKER_URL=tcp://mqtt-prod.company.com:1883
MQTT_USERNAME=iot_platform
MQTT_PASSWORD=MqttSecurePassword@2024

# 应用配置
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC

# 安全配置
ENCRYPT_KEY=YourEncryptionKey2024
JWT_SECRET=YourJWTSecretKey2024
```

## 10. 常见问题和解决方案

### 10.1 连接问题

**问题**: 无法连接到 SQL Server

```bash
# 检查 SQL Server 是否启动
docker ps | grep sqlserver

# 检查端口是否开放
netstat -an | grep 1433

# 测试连接
telnet localhost 1433

# 查看 SQL Server 日志
docker logs sqlserver
```

**解决方案**:
1. 确保 SQL Server 容器正在运行
2. 检查防火墙设置
3. 验证连接字符串中的参数
4. 确认用户名密码正确

### 10.2 性能问题

**问题**: 查询响应慢

**诊断查询**:
```sql
-- 查看等待统计
SELECT 
    wait_type,
    waiting_tasks_count,
    wait_time_ms,
    max_wait_time_ms,
    signal_wait_time_ms
FROM sys.dm_os_wait_stats
WHERE waiting_tasks_count > 0
ORDER BY wait_time_ms DESC;

-- 查看缺失的索引
SELECT 
    migs.avg_total_user_cost * (migs.avg_user_impact / 100.0) * (migs.user_seeks + migs.user_scans) AS improvement_measure,
    'CREATE INDEX [missing_index_' + CONVERT(varchar, mig.index_group_handle) + '_' + CONVERT(varchar, mid.index_handle)
    + '_' + LEFT(PARSENAME(mid.statement, 1), 20) + ']'
    + ' ON ' + mid.statement
    + ' (' + ISNULL(mid.equality_columns,'')
    + CASE WHEN mid.equality_columns IS NOT NULL AND mid.inequality_columns IS NOT NULL THEN ',' ELSE '' END
    + ISNULL(mid.inequality_columns, '')
    + ')'
    + ISNULL(' INCLUDE (' + mid.included_columns + ')', '') AS create_index_statement
FROM sys.dm_db_missing_index_groups mig
INNER JOIN sys.dm_db_missing_index_group_stats migs ON migs.group_handle = mig.index_group_handle
INNER JOIN sys.dm_db_missing_index_details mid ON mig.index_handle = mid.index_handle
WHERE migs.avg_total_user_cost * (migs.avg_user_impact / 100.0) * (migs.user_seeks + migs.user_scans) > 10
ORDER BY improvement_measure DESC;
```

**解决方案**:
1. 添加适当的索引
2. 优化查询语句
3. 调整连接池配置
4. 增加服务器资源

### 10.3 数据一致性问题

**问题**: 数据不一致或丢失

**检查脚本**:
```sql
-- 检查外键约束
SELECT 
    OBJECT_NAME(parent_object_id) AS TableName,
    name AS ConstraintName,
    type_desc AS ConstraintType
FROM sys.foreign_keys;

-- 检查数据完整性
DBCC CHECKDB('devicedb') WITH NO_INFOMSGS;
DBCC CHECKDB('scenedb') WITH NO_INFOMSGS;
```

**解决方案**:
1. 启用事务日志备份
2. 使用适当的事务隔离级别
3. 实施数据验证规则
4. 定期检查数据完整性

### 10.4 内存使用问题

**问题**: SQL Server 内存使用过高

**监控查询**:
```sql
-- 查看内存使用情况
SELECT 
    (physical_memory_kb/1024) AS Physical_Memory_MB,
    (virtual_memory_kb/1024) AS Virtual_Memory_MB,
    (committed_kb/1024) AS Committed_Memory_MB,
    (committed_target_kb/1024) AS Target_Memory_MB
FROM sys.dm_os_sys_info;

-- 查看缓冲池使用情况
SELECT 
    (bpool_committed*8)/1024 AS BPool_Committed_MB,
    (bpool_commit_target*8)/1024 AS BPool_Target_MB,
    (bpool_visible*8)/1024 AS BPool_Visible_MB
FROM sys.dm_os_sys_info;
```

**解决方案**:
1. 设置最大服务器内存限制
2. 优化查询以减少内存使用
3. 清理不必要的缓存
4. 增加物理内存

### 10.5 启动脚本

**完整启动脚本** (`scripts/start-sqlserver.sh`):

```bash
#!/bin/bash

echo "=== SQL Server IoT 平台启动脚本 ==="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker 未运行，请先启动 Docker"
    exit 1
fi

# 停止现有服务
echo "停止现有服务..."
docker-compose down

# 清理旧数据（可选）
read -p "是否清理旧数据? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "清理旧数据..."
    docker volume rm $(docker volume ls -q | grep lowcodeplatform) 2>/dev/null || true
fi

# 创建必要目录
echo "创建必要目录..."
mkdir -p sql/init
mkdir -p backups
mkdir -p logs

# 启动 SQL Server
echo "启动 SQL Server..."
docker-compose up -d sqlserver

# 等待 SQL Server 启动
echo "等待 SQL Server 启动..."
for i in {1..30}; do
    if docker exec sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd" -Q "SELECT 1" > /dev/null 2>&1; then
        echo "SQL Server 启动成功!"
        break
    fi
    echo "等待中... ($i/30)"
    sleep 2
done

# 启动其他服务
echo "启动应用服务..."
docker-compose up -d

# 显示服务状态
echo "=== 服务状态 ==="
docker-compose ps

echo "=== 访问地址 ==="
echo "前端界面: http://localhost:8083"
echo "设备服务: http://localhost:8081"
echo "场景服务: http://localhost:8082"

echo "=== 数据库连接信息 ==="
echo "服务器: localhost:1433"
echo "用户名: sa"
echo "密码: YourStrong@Passw0rd"
echo "设备数据库: devicedb"
echo "场景数据库: scenedb"

echo "启动完成!"
```

## 总结

通过以上配置，您的 IoT 平台将从 H2 内存数据库成功迁移到 SQL Server，获得以下优势：

1. **数据持久化**: 数据不会因重启而丢失
2. **企业级特性**: 事务支持、并发控制、安全性
3. **高性能**: 查询优化、索引支持、连接池
4. **可扩展性**: 支持大数据量和高并发
5. **可维护性**: 完善的监控、备份和恢复机制

迁移过程中如遇到问题，请参考常见问题部分或查看相关日志进行排查。