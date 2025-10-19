-- Azure SQL Server - 场景数据库示例数据插入脚本
-- 注意：此脚本需要在 scenedb 数据库上下文中执行

-- 插入示例场景
IF NOT EXISTS (SELECT * FROM scenes WHERE scene_id = 'scene_001')
BEGIN
    INSERT INTO scenes (scene_id, name, description, status)
    VALUES 
    ('scene_001', '回家模式', '回家时自动开启客厅灯光，调节到舒适亮度', 'ACTIVE'),
    ('scene_002', '离家模式', '离家时关闭所有灯光，锁定门锁', 'ACTIVE'),
    ('scene_003', '睡眠模式', '睡眠时关闭所有灯光，调低空调温度', 'INACTIVE');
END

-- 插入示例触发器
DECLARE @scene1_id BIGINT = (SELECT id FROM scenes WHERE scene_id = 'scene_001');
DECLARE @scene2_id BIGINT = (SELECT id FROM scenes WHERE scene_id = 'scene_002');

IF @scene1_id IS NOT NULL AND NOT EXISTS (SELECT * FROM triggers WHERE scene_id = @scene1_id)
BEGIN
    INSERT INTO triggers (scene_id, trigger_type, trigger_config, is_enabled)
    VALUES 
    (@scene1_id, 'DEVICE_STATE', '{"device_id": "lock_001", "property": "locked", "operator": "equals", "value": false}', 1),
    (@scene2_id, 'MANUAL', '{"description": "手动触发离家模式"}', 1);
END

-- 插入示例动作
IF @scene1_id IS NOT NULL AND NOT EXISTS (SELECT * FROM actions WHERE scene_id = @scene1_id)
BEGIN
    INSERT INTO actions (scene_id, device_id, action, parameters, execution_order)
    VALUES 
    (@scene1_id, 'bulb_001', 'turn_on', '{}', 1),
    (@scene1_id, 'bulb_001', 'set_brightness', '{"brightness": 80}', 2),
    (@scene2_id, 'bulb_001', 'turn_off', '{}', 1),
    (@scene2_id, 'bulb_002', 'turn_off', '{}', 2),
    (@scene2_id, 'lock_001', 'lock', '{}', 3);
END