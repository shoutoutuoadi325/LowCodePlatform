package com.xiyuan.iot.device.mqtt.message;

import lombok.*;

/**
 * 设备注册消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMessage extends DeviceMessage {
    
    /**
     * 设备类型标识符
     */
    private String deviceType;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 设备位置
     */
    private String location;
    
    /**
     * 固件版本
     */
    private String firmwareVersion;
    
    @Builder
    public RegisterMessage(String deviceId, long timestamp, String messageId,
                          String deviceType, String deviceName, String location, String firmwareVersion) {
        super(MessageType.REGISTER, deviceId, timestamp, messageId);
        this.deviceType = deviceType;
        this.deviceName = deviceName;
        this.location = location;
        this.firmwareVersion = firmwareVersion;
    }
}
