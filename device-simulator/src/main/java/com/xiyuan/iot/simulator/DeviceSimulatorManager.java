package com.xiyuan.iot.simulator;

import com.xiyuan.iot.simulator.device.VirtualDevice;
import com.xiyuan.iot.simulator.device.impl.SmartLightDevice;
import com.xiyuan.iot.simulator.device.impl.TemperatureSensorDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备模拟器管理器
 * 管理多个虚拟设备
 */
@Component
@Slf4j
public class DeviceSimulatorManager {
    
    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;
    
    private final List<VirtualDevice> devices = new ArrayList<>();
    
    /**
     * 启动模拟
     */
    public void startSimulation() {
        log.info("Starting device simulation with broker: {}", brokerUrl);
        
        // 创建并启动温度传感器
        createAndStartTemperatureSensor("temp-sensor-001", "办公室温度传感器");
        createAndStartTemperatureSensor("temp-sensor-002", "会议室温度传感器");
        
        // 创建并启动智能灯
        createAndStartSmartLight("light-001", "办公室主灯");
        createAndStartSmartLight("light-002", "会议室主灯");
        createAndStartSmartLight("light-003", "走廊灯");
        
        log.info("Started {} virtual devices", devices.size());
    }
    
    /**
     * 创建并启动温度传感器
     */
    private void createAndStartTemperatureSensor(String deviceId, String deviceName) {
        try {
            TemperatureSensorDevice device = new TemperatureSensorDevice(deviceId, deviceName, brokerUrl);
            device.start();
            devices.add(device);
            log.info("Started temperature sensor: {} ({})", deviceId, deviceName);
        } catch (Exception e) {
            log.error("Failed to start temperature sensor: {}", deviceId, e);
        }
    }
    
    /**
     * 创建并启动智能灯
     */
    private void createAndStartSmartLight(String deviceId, String deviceName) {
        try {
            SmartLightDevice device = new SmartLightDevice(deviceId, deviceName, brokerUrl);
            device.start();
            devices.add(device);
            log.info("Started smart light: {} ({})", deviceId, deviceName);
        } catch (Exception e) {
            log.error("Failed to start smart light: {}", deviceId, e);
        }
    }
    
    /**
     * 停止所有设备
     */
    @PreDestroy
    public void stopAllDevices() {
        log.info("Stopping all virtual devices...");
        devices.forEach(device -> {
            try {
                device.disconnect();
            } catch (Exception e) {
                log.error("Error stopping device", e);
            }
        });
        devices.clear();
        log.info("All devices stopped");
    }
}
