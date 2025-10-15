package com.xiyuan.iot.device.controller;

import com.xiyuan.iot.device.model.Device;
import com.xiyuan.iot.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeviceController {
    
    private final DeviceService deviceService;
    
    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        return ResponseEntity.ok(deviceService.createDevice(device));
    }
    
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDevice(id));
    }
    
    @GetMapping("/by-device-id/{deviceId}")
    public ResponseEntity<Device> getDeviceByDeviceId(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceService.getDeviceByDeviceId(deviceId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        return ResponseEntity.ok(deviceService.updateDevice(id, device));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{deviceId}/control")
    public ResponseEntity<Map<String, Object>> controlDevice(
            @PathVariable String deviceId,
            @RequestBody Map<String, Object> request) {
        String action = (String) request.get("action");
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) request.getOrDefault("parameters", Map.of());
        
        boolean success = deviceService.controlDevice(deviceId, action, parameters);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "deviceId", deviceId,
                "action", action
        ));
    }
    
    @GetMapping("/{deviceId}/state")
    public ResponseEntity<Map<String, String>> getDeviceState(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceService.getDeviceState(deviceId));
    }
    
    @GetMapping("/location")
    public ResponseEntity<List<Device>> getDevicesByLocation(
            @RequestParam String building,
            @RequestParam String floor) {
        return ResponseEntity.ok(deviceService.getDevicesByLocation(building, floor));
    }
    
    @GetMapping("/room/{room}")
    public ResponseEntity<List<Device>> getDevicesByRoom(@PathVariable String room) {
        return ResponseEntity.ok(deviceService.getDevicesByRoom(room));
    }
}
