package com.xiyuan.common.model;

import com.xiyuan.common.enums.DeviceStatus;
import com.xiyuan.common.enums.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private String id;
    private String name;
    private DeviceType type;
    private DeviceStatus status;
    private String location;
    private String floor;
    private String room;
    private Map<String, Object> properties;
    private Map<String, Object> state;
}
