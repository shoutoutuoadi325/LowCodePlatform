package com.xiyuan.iot.device.mqtt.message;

import lombok.*;

import java.util.Map;

/**
 * 设备事件消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage extends DeviceMessage {
    
    /**
     * 事件标识符
     */
    private String eventIdentifier;
    
    /**
     * 事件类型（info, warning, error）
     */
    private String eventType;
    
    /**
     * 事件数据
     */
    private Map<String, Object> eventData;
    
    @Builder
    public EventMessage(String deviceId, long timestamp, String messageId,
                       String eventIdentifier, String eventType, Map<String, Object> eventData) {
        super(MessageType.EVENT, deviceId, timestamp, messageId);
        this.eventIdentifier = eventIdentifier;
        this.eventType = eventType;
        this.eventData = eventData;
    }
}
