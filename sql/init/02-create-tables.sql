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