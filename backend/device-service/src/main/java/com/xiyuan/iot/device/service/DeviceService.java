package com.xiyuan.iot.device.service;

import com.xiyuan.iot.device.model.Device;
import com.xiyuan.iot.device.model.DeviceStatus;
import com.xiyuan.iot.device.repository.DeviceRepository;
import com.xiyuan.iot.device.simulator.SimulatedDeviceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final SimulatedDeviceManager deviceSimulator;
    
    @Transactional
    public Device createDevice(Device device) {
        if (device.getDeviceId() == null || device.getDeviceId().isEmpty()) {
            device.setDeviceId(UUID.randomUUID().toString());
        }
        
        if (device.getStatus() == null) {
            device.setStatus(DeviceStatus.OFFLINE);
        }
        
        Device savedDevice = deviceRepository.save(device);
        deviceSimulator.initializeDevice(savedDevice.getDeviceId(), savedDevice.getType());
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
    
    public boolean controlDevice(String deviceId, String action, Map<String, Object> parameters) {
        Device device = getDeviceByDeviceId(deviceId);
        
        if (device.getStatus() == DeviceStatus.OFFLINE || device.getStatus() == DeviceStatus.DISABLED) {
            log.warn("Cannot control device {} - status: {}", deviceId, device.getStatus());
            return false;
        }
        
        switch (action.toLowerCase()) {
            case "on":
            case "turn_on":
                return deviceSimulator.turnOn(deviceId);
            case "off":
            case "turn_off":
                return deviceSimulator.turnOff(deviceId);
            case "set_state":
                return deviceSimulator.setState(deviceId, parameters);
            default:
                log.warn("Unknown action: {}", action);
                return false;
        }
    }
    
    public Map<String, Object> getDeviceState(String deviceId) {
        getDeviceByDeviceId(deviceId);
        return deviceSimulator.getState(deviceId);
    }
    
    public List<Device> getDevicesByLocation(String building, String floor) {
        return deviceRepository.findByBuildingAndFloor(building, floor);
    }
    
    public List<Device> getDevicesByRoom(String room) {
        return deviceRepository.findByRoom(room);
    }
}
