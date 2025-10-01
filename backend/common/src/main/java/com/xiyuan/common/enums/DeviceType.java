package com.xiyuan.common.enums;

public enum DeviceType {
    LIGHT("灯光"),
    AIR_CONDITIONER("空调"),
    PROJECTOR("投影仪"),
    CURTAIN("窗帘"),
    DOOR("门"),
    WINDOW("窗户"),
    SENSOR_TEMPERATURE("温度传感器"),
    SENSOR_HUMIDITY("湿度传感器"),
    SENSOR_MOTION("运动传感器"),
    SENSOR_LIGHT("光照传感器");

    private final String description;

    DeviceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
