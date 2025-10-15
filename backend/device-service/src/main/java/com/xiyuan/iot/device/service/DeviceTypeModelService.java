package com.xiyuan.iot.device.service;

import com.xiyuan.iot.device.model.metamodel.DeviceTypeModel;
import com.xiyuan.iot.device.repository.DeviceTypeModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备类型模型管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTypeModelService {
    
    private final DeviceTypeModelRepository deviceTypeModelRepository;
    
    /**
     * 创建设备类型模型
     */
    @Transactional
    public DeviceTypeModel createDeviceTypeModel(DeviceTypeModel model) {
        if (deviceTypeModelRepository.existsByTypeIdentifier(model.getTypeIdentifier())) {
            throw new RuntimeException("Device type already exists: " + model.getTypeIdentifier());
        }
        
        DeviceTypeModel saved = deviceTypeModelRepository.save(model);
        log.info("Created device type model: {}", saved.getTypeIdentifier());
        return saved;
    }
    
    /**
     * 获取所有设备类型模型
     */
    public List<DeviceTypeModel> getAllDeviceTypeModels() {
        return deviceTypeModelRepository.findAll();
    }
    
    /**
     * 根据ID获取设备类型模型
     */
    public DeviceTypeModel getDeviceTypeModel(Long id) {
        return deviceTypeModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device type model not found: " + id));
    }
    
    /**
     * 根据类型标识符获取设备类型模型
     */
    public DeviceTypeModel getDeviceTypeModelByIdentifier(String typeIdentifier) {
        return deviceTypeModelRepository.findByTypeIdentifier(typeIdentifier)
                .orElseThrow(() -> new RuntimeException("Device type model not found: " + typeIdentifier));
    }
    
    /**
     * 更新设备类型模型
     */
    @Transactional
    public DeviceTypeModel updateDeviceTypeModel(Long id, DeviceTypeModel model) {
        DeviceTypeModel existing = getDeviceTypeModel(id);
        
        existing.setTypeName(model.getTypeName());
        existing.setDescription(model.getDescription());
        existing.setCategory(model.getCategory());
        existing.setManufacturer(model.getManufacturer());
        existing.setModel(model.getModel());
        existing.setVersion(model.getVersion());
        existing.setProtocol(model.getProtocol());
        existing.setEnabled(model.isEnabled());
        
        return deviceTypeModelRepository.save(existing);
    }
    
    /**
     * 删除设备类型模型
     */
    @Transactional
    public void deleteDeviceTypeModel(Long id) {
        deviceTypeModelRepository.deleteById(id);
        log.info("Deleted device type model: {}", id);
    }
}
