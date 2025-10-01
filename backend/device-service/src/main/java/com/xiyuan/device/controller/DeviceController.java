package com.xiyuan.device.controller;

import com.xiyuan.common.model.Device;
import com.xiyuan.device.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getDevice(@PathVariable String deviceId) {
        return deviceService.getDevice(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/room/{room}")
    public ResponseEntity<List<Device>> getDevicesByRoom(@PathVariable String room) {
        return ResponseEntity.ok(deviceService.getDevicesByRoom(room));
    }

    @PostMapping("/{deviceId}/action")
    public ResponseEntity<Map<String, Object>> executeAction(
            @PathVariable String deviceId,
            @RequestBody Map<String, Object> request) {
        String action = (String) request.get("action");
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
        
        boolean success = deviceService.executeAction(deviceId, action, parameters);
        if (success) {
            Map<String, Object> state = deviceService.getDeviceState(deviceId);
            return ResponseEntity.ok(Map.of("success", true, "state", state));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceId}/state")
    public ResponseEntity<Map<String, Object>> getDeviceState(@PathVariable String deviceId) {
        Map<String, Object> state = deviceService.getDeviceState(deviceId);
        if (!state.isEmpty()) {
            return ResponseEntity.ok(state);
        }
        return ResponseEntity.notFound().build();
    }
}
