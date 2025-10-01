package com.xiyuan.common.enums;

public enum DeviceStatus {
    ONLINE("在线"),
    OFFLINE("离线"),
    ERROR("错误");

    private final String description;

    DeviceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
