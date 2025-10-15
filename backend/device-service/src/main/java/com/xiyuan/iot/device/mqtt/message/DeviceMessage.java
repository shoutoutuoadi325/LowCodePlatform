package com.xiyuan.iot.device.mqtt.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备消息基类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceMessage {
    
    /**
     * 消息类型
     */
    private MessageType messageType;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 时间戳
     */
    private long timestamp;
    
    /**
     * 消息ID（用于追踪）
     */
    private String messageId;
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        // 平台 -> 设备
        COMMAND,        // 控制命令
        CONFIG,         // 配置更新
        
        // 设备 -> 平台
        TELEMETRY,      // 遥测数据（属性上报）
        EVENT,          // 事件上报
        RESPONSE,       // 命令响应
        HEARTBEAT,      // 心跳
        
        // 双向
        REGISTER,       // 设备注册
        ONLINE,         // 设备上线
        OFFLINE         // 设备离线
    }
}
