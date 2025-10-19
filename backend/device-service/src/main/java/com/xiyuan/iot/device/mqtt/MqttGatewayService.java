package com.xiyuan.iot.device.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiyuan.iot.device.mqtt.message.CommandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.messaging.MessageChannel;

import java.util.Map;
import java.util.UUID;

/**
 * MQTT网关服务
 * 提供向设备发送消息的接口
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = false)
public class MqttGatewayService {
    
    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper;
    
    /**
     * 向设备发送控制命令
     */
    public boolean sendCommand(String deviceId, String command, Map<String, Object> parameters) {
        try {
            CommandMessage commandMessage = CommandMessage.builder()
                    .deviceId(deviceId)
                    .command(command)
                    .parameters(parameters)
                    .timestamp(System.currentTimeMillis())
                    .messageId(UUID.randomUUID().toString())
                    .build();
            
            String topic = MqttConfig.Topics.getCommandTopic(deviceId);
            String payload = objectMapper.writeValueAsString(commandMessage);
            
            log.info("Sending command to device {}: {}", deviceId, command);
            
            mqttOutboundChannel.send(
                    MessageBuilder.withPayload(payload)
                            .setHeader("mqtt_topic", topic)
                            .build()
            );
            
            return true;
        } catch (Exception e) {
            log.error("Error sending command to device " + deviceId, e);
            return false;
        }
    }
    
    /**
     * 向设备发送配置更新
     */
    public boolean sendConfig(String deviceId, Map<String, Object> config) {
        try {
            String topic = MqttConfig.Topics.getConfigTopic(deviceId);
            String payload = objectMapper.writeValueAsString(config);
            
            log.info("Sending config to device {}", deviceId);
            
            mqttOutboundChannel.send(
                    MessageBuilder.withPayload(payload)
                            .setHeader("mqtt_topic", topic)
                            .build()
            );
            
            return true;
        } catch (Exception e) {
            log.error("Error sending config to device " + deviceId, e);
            return false;
        }
    }
}
