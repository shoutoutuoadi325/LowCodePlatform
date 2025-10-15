package com.xiyuan.iot.device.model.metamodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备事件定义 - 元模型
 * 定义设备类型可能产生的事件（如警报、状态变化等）
 */
@Entity
@Table(name = "device_event_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEventDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 事件标识符（如motionDetected, temperatureAlert）
     */
    @Column(nullable = false)
    private String identifier;
    
    /**
     * 事件名称（如"检测到移动", "温度告警"）
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 事件类型（info, warning, error）
     */
    private String eventType;
    
    /**
     * 事件输出参数定义（JSON格式）
     */
    @Column(length = 2000)
    private String outputParameters;
    
    /**
     * 所属设备类型模型
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_model_id")
    private DeviceTypeModel deviceTypeModel;
}
