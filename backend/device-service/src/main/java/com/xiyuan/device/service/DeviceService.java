package com.xiyuan.device.service;

import com.xiyuan.common.model.Device;
import com.xiyuan.device.simulator.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceService {
    private final Map<String, SimulatedDevice> devices = new ConcurrentHashMap<>();

    public DeviceService() {
        initializeSimulatedDevices();
    }

    private void initializeSimulatedDevices() {
        // 教室1 设备
        addDevice(new LightDevice("light-101", "教室1主灯", "教学楼", "1楼", "101教室"));
        addDevice(new AirConditionerDevice("ac-101", "教室1空调", "教学楼", "1楼", "101教室"));
        addDevice(new ProjectorDevice("proj-101", "教室1投影仪", "教学楼", "1楼", "101教室"));
        addDevice(new CurtainDevice("curtain-101", "教室1窗帘", "教学楼", "1楼", "101教室"));

        // 教室2 设备
        addDevice(new LightDevice("light-102", "教室2主灯", "教学楼", "1楼", "102教室"));
        addDevice(new AirConditionerDevice("ac-102", "教室2空调", "教学楼", "1楼", "102教室"));
        addDevice(new ProjectorDevice("proj-102", "教室2投影仪", "教学楼", "1楼", "102教室"));
        addDevice(new CurtainDevice("curtain-102", "教室2窗帘", "教学楼", "1楼", "102教室"));

        // 走廊设备
        addDevice(new LightDevice("light-corridor-1", "1楼走廊灯", "教学楼", "1楼", "走廊"));
    }

    private void addDevice(SimulatedDevice device) {
        devices.put(device.getId(), device);
    }

    public List<Device> getAllDevices() {
        return devices.values().stream()
                .map(SimulatedDevice::getDevice)
                .toList();
    }

    public Optional<Device> getDevice(String deviceId) {
        SimulatedDevice simDevice = devices.get(deviceId);
        return Optional.ofNullable(simDevice != null ? simDevice.getDevice() : null);
    }

    public List<Device> getDevicesByRoom(String room) {
        return devices.values().stream()
                .map(SimulatedDevice::getDevice)
                .filter(d -> room.equals(d.getRoom()))
                .toList();
    }

    public boolean executeAction(String deviceId, String action, Map<String, Object> parameters) {
        SimulatedDevice device = devices.get(deviceId);
        if (device != null) {
            device.executeAction(action, parameters);
            return true;
        }
        return false;
    }

    public Map<String, Object> getDeviceState(String deviceId) {
        SimulatedDevice device = devices.get(deviceId);
        return device != null ? device.getState() : Collections.emptyMap();
    }
}
