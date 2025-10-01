package com.xiyuan.device.simulator;

import com.xiyuan.common.enums.DeviceStatus;
import com.xiyuan.common.enums.DeviceType;
import com.xiyuan.common.model.Device;

import java.util.HashMap;
import java.util.Map;

public abstract class SimulatedDevice {
    protected Device device;

    public SimulatedDevice(String id, String name, DeviceType type, String location, String floor, String room) {
        this.device = Device.builder()
                .id(id)
                .name(name)
                .type(type)
                .status(DeviceStatus.ONLINE)
                .location(location)
                .floor(floor)
                .room(room)
                .properties(new HashMap<>())
                .state(new HashMap<>())
                .build();
        initializeState();
    }

    protected abstract void initializeState();

    public abstract void executeAction(String action, Map<String, Object> parameters);

    public Device getDevice() {
        return device;
    }

    public String getId() {
        return device.getId();
    }

    public Map<String, Object> getState() {
        return device.getState();
    }

    public void updateState(String key, Object value) {
        device.getState().put(key, value);
    }
}
