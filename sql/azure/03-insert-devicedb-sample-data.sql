-- Azure SQL Server - 设备数据库示例数据插入脚本
-- 注意：此脚本需要在 devicedb 数据库上下文中执行

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

IF @temp_id IS NOT NULL AND NOT EXISTS (SELECT * FROM device_property_definitions WHERE device_type_model_id = @temp_id)
BEGIN
    INSERT INTO device_property_definitions (device_type_model_id, property_name, property_identifier, data_type, unit, min_value, max_value, is_readonly, description)
    VALUES 
    (@temp_id, '温度', 'temperature', 'FLOAT', '°C', '-40', '80', 1, '环境温度'),
    (@temp_id, '湿度', 'humidity', 'FLOAT', '%', '0', '100', 1, '环境湿度'),
    (@temp_id, '电池电量', 'battery', 'INTEGER', '%', '0', '100', 1, '传感器电池电量');
END

-- 插入设备操作定义
IF @bulb_id IS NOT NULL AND NOT EXISTS (SELECT * FROM device_operation_definitions WHERE device_type_model_id = @bulb_id)
BEGIN
    INSERT INTO device_operation_definitions (device_type_model_id, operation_name, operation_identifier, description, parameters, return_type)
    VALUES 
    (@bulb_id, '开灯', 'turn_on', '打开智能灯泡', '{}', 'BOOLEAN'),
    (@bulb_id, '关灯', 'turn_off', '关闭智能灯泡', '{}', 'BOOLEAN'),
    (@bulb_id, '设置亮度', 'set_brightness', '设置灯泡亮度', '{"brightness": {"type": "INTEGER", "min": 0, "max": 100}}', 'BOOLEAN'),
    (@bulb_id, '设置颜色', 'set_color', '设置灯泡颜色', '{"color": {"type": "STRING", "pattern": "^#[0-9A-Fa-f]{6}$"}}', 'BOOLEAN');
END

-- 插入设备事件定义
IF @bulb_id IS NOT NULL AND NOT EXISTS (SELECT * FROM device_event_definitions WHERE device_type_model_id = @bulb_id)
BEGIN
    INSERT INTO device_event_definitions (device_type_model_id, event_name, event_identifier, description, event_level, parameters)
    VALUES 
    (@bulb_id, '状态变化', 'state_changed', '灯泡状态发生变化', 'INFO', '{"old_state": {"type": "OBJECT"}, "new_state": {"type": "OBJECT"}}'),
    (@bulb_id, '连接断开', 'disconnected', '灯泡连接断开', 'WARN', '{"timestamp": {"type": "DATETIME"}}'),
    (@bulb_id, '故障报警', 'fault_alarm', '灯泡发生故障', 'ERROR', '{"error_code": {"type": "STRING"}, "error_message": {"type": "STRING"}}');
END

-- 插入示例设备
IF NOT EXISTS (SELECT * FROM devices WHERE device_id = 'bulb_001')
BEGIN
    INSERT INTO devices (device_id, device_name, device_type, status, location, description, properties)
    VALUES 
    ('bulb_001', '客厅主灯', 'smart_bulb', 'ONLINE', '客厅', 'Philips Hue智能灯泡', '{"power": true, "brightness": 80, "color_temp": 4000, "color": "#FFFFFF"}'),
    ('bulb_002', '卧室台灯', 'smart_bulb', 'OFFLINE', '主卧', 'Philips Hue智能灯泡', '{"power": false, "brightness": 50, "color_temp": 3000, "color": "#FFE4B5"}'),
    ('temp_001', '客厅温度传感器', 'temperature_sensor', 'ONLINE', '客厅', '小米温湿度传感器', '{"temperature": 23.5, "humidity": 65, "battery": 85}'),
    ('lock_001', '前门智能锁', 'smart_lock', 'ONLINE', '前门', 'Aqara智能门锁', '{"locked": true, "battery": 92}');
END