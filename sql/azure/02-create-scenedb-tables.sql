-- Azure SQL Server - 场景数据库表结构创建脚本
-- 注意：此脚本需要在 scenedb 数据库上下文中执行

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