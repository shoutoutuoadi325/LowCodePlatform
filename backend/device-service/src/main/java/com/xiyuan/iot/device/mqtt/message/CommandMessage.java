package com.xiyuan.iot.device.mqtt.message;

import lombok.*;

import java.util.Map;

/**
 * 设备命令消息（平台->设备）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CommandMessage extends DeviceMessage {
    
    /**
     * 命令标识符（如turnOn, setTemperature）
     */
    private String command;
    
    /**
     * 命令参数
     */
    private Map<String, Object> parameters;
    
    @Builder
    public CommandMessage(String deviceId, long timestamp, String messageId, 
                         String command, Map<String, Object> parameters) {
        super(MessageType.COMMAND, deviceId, timestamp, messageId);
        this.command = command;
        this.parameters = parameters;
    }
}
