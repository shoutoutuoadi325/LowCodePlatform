package com.xiyuan.iot.device.model.metamodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备操作定义 - 元模型
 * 定义设备类型支持的操作（如开关、设置温度等）
 */
@Entity
@Table(name = "device_operation_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceOperationDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 操作标识符（如turnOn, turnOff, setTemperature）
     */
    @Column(nullable = false)
    private String identifier;
    
    /**
     * 操作名称（如"打开", "关闭", "设置温度"）
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 输入参数定义（JSON格式）
     * 例如：[{"name":"temperature","type":"double","required":true}]
     */
    @Column(length = 2000)
    private String inputParameters;
    
    /**
     * 返回值类型
     */
    private String returnType;
    
    /**
     * 是否异步操作
     */
    private boolean async;
    
    /**
     * 所属设备类型模型
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_model_id")
    private DeviceTypeModel deviceTypeModel;
}
