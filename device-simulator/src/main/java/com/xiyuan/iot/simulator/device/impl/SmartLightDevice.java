package com.xiyuan.iot.simulator.device.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiyuan.iot.simulator.device.VirtualDevice;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能灯模拟器
 */
@Slf4j
public class SmartLightDevice extends VirtualDevice {
    
    private boolean powerOn = false;
    private int brightness = 100;  // 0-100
    private String color = "#FFFFFF";  // 白色
    
    public SmartLightDevice(String deviceId, String deviceName, String brokerUrl) {
        super(deviceId, "smart_light", deviceName, brokerUrl);
    }
    
    @Override
    protected void onStart() {
        // 初始化属性
        updateProperty("power", powerOn);
        updateProperty("brightness", brightness);
        updateProperty("color", color);
    }
    
    @Override
    protected Map<String, Object> getCurrentProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("power", powerOn);
        props.put("brightness", brightness);
        props.put("color", color);
        props.put("lastUpdate", System.currentTimeMillis());
        
        return props;
    }
    
    @Override
    protected void handleCommand(String topic, String payload) {
        try {
            log.info("Smart light {} received command: {}", deviceId, payload);
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> command = mapper.readValue(payload, Map.class);
            
            String commandType = (String) command.get("command");
            Map<String, Object> parameters = (Map<String, Object>) command.get("parameters");
            
            boolean changed = false;
            
            switch (commandType) {
                case "turnOn":
                case "turn_on":
                    powerOn = true;
                    changed = true;
                    log.info("Smart light {} turned ON", deviceId);
                    break;
                    
                case "turnOff":
                case "turn_off":
                    powerOn = false;
                    changed = true;
                    log.info("Smart light {} turned OFF", deviceId);
                    break;
                    
                case "setBrightness":
                case "set_brightness":
                    if (parameters != null && parameters.containsKey("brightness")) {
                        brightness = ((Number) parameters.get("brightness")).intValue();
                        brightness = Math.max(0, Math.min(100, brightness));  // 限制在0-100
                        changed = true;
                        log.info("Smart light {} brightness set to {}", deviceId, brightness);
                    }
                    break;
                    
                case "setColor":
                case "set_color":
                    if (parameters != null && parameters.containsKey("color")) {
                        color = (String) parameters.get("color");
                        changed = true;
                        log.info("Smart light {} color set to {}", deviceId, color);
                    }
                    break;
                    
                default:
                    log.warn("Unknown command: {}", commandType);
            }
            
            // 更新属性
            if (changed) {
                updateProperty("power", powerOn);
                updateProperty("brightness", brightness);
                updateProperty("color", color);
                
                // 立即上报状态变化
                reportTelemetry();
            }
            
            // 发送响应
            sendResponse(command.get("messageId").toString(), "success");
            
        } catch (Exception e) {
            log.error("Error handling command for smart light: {}", deviceId, e);
        }
    }
    
    /**
     * 发送命令响应
     */
    private void sendResponse(String messageId, String status) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("messageType", "RESPONSE");
            response.put("deviceId", deviceId);
            response.put("timestamp", System.currentTimeMillis());
            response.put("messageId", messageId);
            response.put("status", status);
            
            String topic = String.format("iot/devices/%s/response", deviceId);
            String payload = objectMapper.writeValueAsString(response);
            
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("Error sending response", e);
        }
    }
}
