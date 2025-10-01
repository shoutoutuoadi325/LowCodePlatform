package com.xiyuan.iot.scene.model;

public enum TriggerType {
    MANUAL,           // 手动触发
    SCHEDULE,         // 定时触发
    DEVICE_STATE,     // 设备状态变化
    SENSOR_VALUE,     // 传感器数值
    TIME_BASED,       // 基于时间
    LOCATION_BASED    // 基于位置
}
