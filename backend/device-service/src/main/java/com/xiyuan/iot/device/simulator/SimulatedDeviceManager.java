package com.xiyuan.iot.device.simulator;

import com.xiyuan.iot.device.model.DeviceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SimulatedDeviceManager implements DeviceSimulator {
    
    private final Map<String, Map<String, Object>> deviceStates = new ConcurrentHashMap<>();
    
    @Override
    public boolean turnOn(String deviceId) {
        log.info("Turning ON device: {}", deviceId);
        Map<String, Object> state = deviceStates.computeIfAbsent(deviceId, k -> new HashMap<>());
        state.put("power", "on");
        state.put("lastUpdate", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public boolean turnOff(String deviceId) {
        log.info("Turning OFF device: {}", deviceId);
        Map<String, Object> state = deviceStates.computeIfAbsent(deviceId, k -> new HashMap<>());
        state.put("power", "off");
        state.put("lastUpdate", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public Map<String, Object> getState(String deviceId) {
        return deviceStates.getOrDefault(deviceId, new HashMap<>());
    }
    
    @Override
    public boolean setState(String deviceId, Map<String, Object> state) {
        log.info("Setting state for device {}: {}", deviceId, state);
        Map<String, Object> currentState = deviceStates.computeIfAbsent(deviceId, k -> new HashMap<>());
        currentState.putAll(state);
        currentState.put("lastUpdate", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public boolean isSupported(String deviceType) {
        try {
            DeviceType.valueOf(deviceType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public void initializeDevice(String deviceId, DeviceType type) {
        Map<String, Object> initialState = new HashMap<>();
        initialState.put("power", "off");
        initialState.put("type", type.name());
        initialState.put("lastUpdate", System.currentTimeMillis());
        
        switch (type) {
            case LIGHT:
                initialState.put("brightness", 100);
                initialState.put("color", "#FFFFFF");
                break;
            case HVAC:
                initialState.put("temperature", 22.0);
                initialState.put("mode", "auto");
                initialState.put("fanSpeed", "medium");
                break;
            case TEMPERATURE_SENSOR:
                initialState.put("temperature", 22.0);
                break;
            case HUMIDITY_SENSOR:
                initialState.put("humidity", 50.0);
                break;
            case MOTION_SENSOR:
                initialState.put("motion", false);
                break;
            case DOOR_LOCK:
                initialState.put("locked", true);
                break;
            case WINDOW:
            case CURTAIN:
                initialState.put("position", 0);
                break;
            case PROJECTOR:
                initialState.put("input", "HDMI1");
                break;
            case SCREEN:
                initialState.put("position", "up");
                break;
            case FAN:
                initialState.put("speed", 0);
                break;
            case AIR_QUALITY_SENSOR:
                initialState.put("pm25", 35.0);
                initialState.put("co2", 400.0);
                break;
            default:
                break;
        }
        
        deviceStates.put(deviceId, initialState);
    }
}
