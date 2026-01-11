package com.xiyuan.iot.device.simulator;

import java.util.Map;

public interface DeviceSimulator {
    boolean turnOn(String deviceId);
    boolean turnOff(String deviceId);
    Map<String, Object> getState(String deviceId);
    boolean setState(String deviceId, Map<String, Object> state);
    boolean isSupported(String deviceType);
}
