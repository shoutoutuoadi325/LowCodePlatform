package com.xiyuan.iot.device.model.metamodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备属性定义 - 元模型
 * 定义设备类型的属性（如温度、湿度、亮度等）
 */
@Entity
@Table(name = "device_property_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevicePropertyDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 属性标识符（如temperature, humidity, brightness）
     */
    @Column(nullable = false)
    private String identifier;
    
    /**
     * 属性名称（如"温度", "湿度"）
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 属性描述
     */
    private String description;
    
    /**
     * 数据类型（int, double, string, boolean, enum）
     */
    @Column(nullable = false)
    private String dataType;
    
    /**
     * 是否只读
     */
    private boolean readOnly;
    
    /**
     * 单位（如 ℃, %, lux）
     */
    private String unit;
    
    /**
     * 最小值（仅适用于数值类型）
     */
    private Double minValue;
    
    /**
     * 最大值（仅适用于数值类型）
     */
    private Double maxValue;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 枚举值（JSON格式，仅适用于enum类型）
     */
    @Column(length = 1000)
    private String enumValues;
    
    /**
     * 所属设备类型模型
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_model_id")
    private DeviceTypeModel deviceTypeModel;
}
