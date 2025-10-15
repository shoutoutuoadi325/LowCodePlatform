package com.xiyuan.iot.device.model.metamodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备类型模型 - 元模型
 * 定义一种设备类型的完整模型，包括属性、操作和事件
 */
@Entity
@Table(name = "device_type_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTypeModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 设备类型标识符（如temperature_sensor, smart_light）
     */
    @Column(nullable = false, unique = true)
    private String typeIdentifier;
    
    /**
     * 设备类型名称（如"温度传感器", "智能灯"）
     */
    @Column(nullable = false)
    private String typeName;
    
    /**
     * 设备类型描述
     */
    private String description;
    
    /**
     * 设备类别（sensor, actuator, hybrid）
     */
    private String category;
    
    /**
     * 制造商
     */
    private String manufacturer;
    
    /**
     * 型号
     */
    private String model;
    
    /**
     * 版本
     */
    private String version;
    
    /**
     * 通信协议（MQTT, CoAP, HTTP等）
     */
    private String protocol;
    
    /**
     * 是否启用
     */
    private boolean enabled;
    
    /**
     * 属性定义列表
     */
    @OneToMany(mappedBy = "deviceTypeModel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DevicePropertyDefinition> properties = new ArrayList<>();
    
    /**
     * 操作定义列表
     */
    @OneToMany(mappedBy = "deviceTypeModel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeviceOperationDefinition> operations = new ArrayList<>();
    
    /**
     * 事件定义列表
     */
    @OneToMany(mappedBy = "deviceTypeModel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeviceEventDefinition> events = new ArrayList<>();
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 辅助方法：添加属性定义
     */
    public void addProperty(DevicePropertyDefinition property) {
        properties.add(property);
        property.setDeviceTypeModel(this);
    }
    
    /**
     * 辅助方法：添加操作定义
     */
    public void addOperation(DeviceOperationDefinition operation) {
        operations.add(operation);
        operation.setDeviceTypeModel(this);
    }
    
    /**
     * 辅助方法：添加事件定义
     */
    public void addEvent(DeviceEventDefinition event) {
        events.add(event);
        event.setDeviceTypeModel(this);
    }
}
