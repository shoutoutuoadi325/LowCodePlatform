package com.xiyuan.iot.device.mqtt.message;

import lombok.*;

import java.util.Map;

/**
 * 设备遥测消息（属性上报）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryMessage extends DeviceMessage {
    
    /**
     * 属性数据（key: 属性标识符, value: 属性值）
     */
    private Map<String, Object> properties;
    
    @Builder
    public TelemetryMessage(String deviceId, long timestamp, String messageId, Map<String, Object> properties) {
        super(MessageType.TELEMETRY, deviceId, timestamp, messageId);
        this.properties = properties;
    }
}
