package com.xiyuan.iot.simulator.device.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiyuan.iot.simulator.device.VirtualDevice;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 温度传感器模拟器
 */
@Slf4j
public class TemperatureSensorDevice extends VirtualDevice {
    
    private final Random random = new Random();
    private double currentTemperature = 22.0;  // 初始温度22℃
    private double targetTemperature = 22.0;
    
    public TemperatureSensorDevice(String deviceId, String deviceName, String brokerUrl) {
        super(deviceId, "temperature_sensor", deviceName, brokerUrl);
    }
    
    @Override
    protected void onStart() {
        // 初始化属性
        updateProperty("temperature", currentTemperature);
        updateProperty("unit", "℃");
        updateProperty("status", "normal");
    }
    
    @Override
    protected Map<String, Object> getCurrentProperties() {
        // 模拟温度变化
        simulateTemperatureChange();
        
        Map<String, Object> props = new HashMap<>();
        props.put("temperature", currentTemperature);
        props.put("unit", "℃");
        props.put("status", getTemperatureStatus());
        props.put("lastUpdate", System.currentTimeMillis());
        
        return props;
    }
    
    @Override
    protected void handleCommand(String topic, String payload) {
        try {
            log.info("Temperature sensor {} received command: {}", deviceId, payload);
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> command = mapper.readValue(payload, Map.class);
            
            String commandType = (String) command.get("command");
            Map<String, Object> parameters = (Map<String, Object>) command.get("parameters");
            
            if ("calibrate".equals(commandType) && parameters != null) {
                // 校准温度
                Object tempObj = parameters.get("temperature");
                if (tempObj != null) {
                    targetTemperature = ((Number) tempObj).doubleValue();
                    log.info("Temperature sensor {} calibrated to {}", deviceId, targetTemperature);
                }
            }
            
            // 发送响应
            sendResponse(command.get("messageId").toString(), "success");
            
        } catch (Exception e) {
            log.error("Error handling command for temperature sensor: {}", deviceId, e);
        }
    }
    
    /**
     * 模拟温度变化
     */
    private void simulateTemperatureChange() {
        // 在目标温度附近随机波动 ±0.5℃
        double variation = (random.nextDouble() - 0.5) * 1.0;
        currentTemperature = targetTemperature + variation;
        
        // 保留一位小数
        currentTemperature = Math.round(currentTemperature * 10.0) / 10.0;
        
        updateProperty("temperature", currentTemperature);
        
        // 模拟异常温度告警
        if (currentTemperature > 35.0 || currentTemperature < 0.0) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("temperature", currentTemperature);
            eventData.put("threshold", currentTemperature > 35.0 ? 35.0 : 0.0);
            
            reportEvent("temperatureAlert", "warning", eventData);
        }
    }
    
    /**
     * 获取温度状态
     */
    private String getTemperatureStatus() {
        if (currentTemperature > 35.0) {
            return "high";
        } else if (currentTemperature < 0.0) {
            return "low";
        } else {
            return "normal";
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
