package com.xiyuan.iot.device.service;

import com.xiyuan.iot.device.model.Device;
import com.xiyuan.iot.device.model.DeviceStatus;
import com.xiyuan.iot.device.mqtt.MqttGatewayService;
import com.xiyuan.iot.device.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    
    @Autowired(required = false)
    private MqttGatewayService mqttGatewayService;
    
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    @Transactional
    public Device createDevice(Device device) {
        if (device.getDeviceId() == null || device.getDeviceId().isEmpty()) {
            device.setDeviceId(UUID.randomUUID().toString());
        }
        
        if (device.getStatus() == null) {
            device.setStatus(DeviceStatus.OFFLINE);
        }
        
        Device savedDevice = deviceRepository.save(device);
        log.info("Created device: {}", savedDevice.getDeviceId());
        
        return savedDevice;
    }
    
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
    
    public Device getDevice(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found: " + id));
    }
    
    public Device getDeviceByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));
    }
    
    @Transactional
    public Device updateDevice(Long id, Device device) {
        Device existingDevice = getDevice(id);
        existingDevice.setName(device.getName());
        existingDevice.setLocation(device.getLocation());
        existingDevice.setBuilding(device.getBuilding());
        existingDevice.setFloor(device.getFloor());
        existingDevice.setRoom(device.getRoom());
        existingDevice.setStatus(device.getStatus());
        if (device.getProperties() != null) {
            existingDevice.setProperties(device.getProperties());
        }
        return deviceRepository.save(existingDevice);
    }
    
    @Transactional
    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }
    
    /**
     * 控制设备 - 通过MQTT发送命令
     */
    public boolean controlDevice(String deviceId, String action, Map<String, Object> parameters) {
        Device device = getDeviceByDeviceId(deviceId);
        
        if (device.getStatus() == DeviceStatus.OFFLINE || device.getStatus() == DeviceStatus.DISABLED) {
            log.warn("Cannot control device {} - status: {}", deviceId, device.getStatus());
            return false;
        }
        
        // 通过MQTT发送命令到设备
        if (mqttGatewayService != null) {
            return mqttGatewayService.sendCommand(deviceId, action, parameters);
        } else {
            log.warn("MQTT service is disabled, cannot send command to device {}", deviceId);
            return false;
        }
    }
    
    /**
     * 获取设备状态 - 从数据库读取最新上报的属性
     */
    public Map<String, String> getDeviceState(String deviceId) {
        Device device = getDeviceByDeviceId(deviceId);
        return device.getProperties();
    }
    
    public List<Device> getDevicesByLocation(String building, String floor) {
        return deviceRepository.findByBuildingAndFloor(building, floor);
    }
    
    public List<Device> getDevicesByRoom(String room) {
        return deviceRepository.findByRoom(room);
    }
}
