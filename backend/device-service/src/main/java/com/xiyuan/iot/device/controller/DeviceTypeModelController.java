package com.xiyuan.iot.device.controller;

import com.xiyuan.iot.device.model.metamodel.DeviceTypeModel;
import com.xiyuan.iot.device.service.DeviceTypeModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备类型模型控制器
 * 提供设备类型的元模型管理接口
 */
@RestController
@RequestMapping("/api/device-types")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DeviceTypeModelController {
    
    private final DeviceTypeModelService deviceTypeModelService;
    
    /**
     * 创建设备类型
     */
    @PostMapping
    public ResponseEntity<DeviceTypeModel> createDeviceType(@RequestBody DeviceTypeModel model) {
        log.info("Creating device type: {}", model.getTypeIdentifier());
        DeviceTypeModel created = deviceTypeModelService.createDeviceTypeModel(model);
        return ResponseEntity.ok(created);
    }
    
    /**
     * 测试端点
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("DeviceTypeModelController is working!");
    }
    
    /**
     * 获取所有设备类型
     */
    @GetMapping
    public ResponseEntity<List<DeviceTypeModel>> getAllDeviceTypes() {
        List<DeviceTypeModel> models = deviceTypeModelService.getAllDeviceTypeModels();
        return ResponseEntity.ok(models);
    }
    
    /**
     * 根据ID获取设备类型
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceTypeModel> getDeviceType(@PathVariable Long id) {
        DeviceTypeModel model = deviceTypeModelService.getDeviceTypeModel(id);
        return ResponseEntity.ok(model);
    }
    
    /**
     * 根据类型标识符获取设备类型
     */
    @GetMapping("/identifier/{typeIdentifier}")
    public ResponseEntity<DeviceTypeModel> getDeviceTypeByIdentifier(@PathVariable String typeIdentifier) {
        DeviceTypeModel model = deviceTypeModelService.getDeviceTypeModelByIdentifier(typeIdentifier);
        return ResponseEntity.ok(model);
    }
    
    /**
     * 更新设备类型
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeviceTypeModel> updateDeviceType(
            @PathVariable Long id,
            @RequestBody DeviceTypeModel model) {
        log.info("Updating device type: {}", id);
        DeviceTypeModel updated = deviceTypeModelService.updateDeviceTypeModel(id, model);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 删除设备类型
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceType(@PathVariable Long id) {
        log.info("Deleting device type: {}", id);
        deviceTypeModelService.deleteDeviceTypeModel(id);
        return ResponseEntity.noContent().build();
    }
}
