package com.xiyuan.iot.device.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiyuan.iot.device.model.Device;
import com.xiyuan.iot.device.model.DeviceStatus;
import com.xiyuan.iot.device.mqtt.message.*;
import com.xiyuan.iot.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MQTT消息处理器
 * 处理来自设备的所有MQTT消息
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MqttMessageHandler {
    
    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 处理来自设备的入站消息
     */
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        try {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = new String((byte[]) message.getPayload());
            
            log.info("Received MQTT message from topic: {}, payload: {}", topic, payload);
            
            // 根据主题路由到不同的处理方法
            if (topic.contains("/telemetry")) {
                handleTelemetry(topic, payload);
            } else if (topic.contains("/event")) {
                handleEvent(topic, payload);
            } else if (topic.contains("/response")) {
                handleResponse(topic, payload);
            } else if (topic.contains("/register")) {
                handleRegister(topic, payload);
            } else if (topic.contains("/online")) {
                handleOnline(topic, payload);
            } else if (topic.contains("/heartbeat")) {
                handleHeartbeat(topic, payload);
            } else {
                log.warn("Unknown topic: {}", topic);
            }
            
        } catch (Exception e) {
            log.error("Error handling MQTT message", e);
        }
    }
    
    /**
     * 处理遥测数据（属性上报）
     */
    private void handleTelemetry(String topic, String payload) {
        try {
            TelemetryMessage telemetryMessage = objectMapper.readValue(payload, TelemetryMessage.class);
            String deviceId = telemetryMessage.getDeviceId();
            
            log.info("Processing telemetry from device {}: {}", deviceId, telemetryMessage.getProperties());
            
            // 更新设备属性
            Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                Map<String, String> properties = device.getProperties();
                if (properties == null) {
                    device.setProperties(new HashMap<>());
                    properties = device.getProperties();
                }
                
                // 将Object值转换为String存储
                final Map<String, String> finalProperties = properties;
                telemetryMessage.getProperties().forEach((key, value) -> {
                    finalProperties.put(key, String.valueOf(value));
                });
                
                device.setStatus(DeviceStatus.ONLINE);
                deviceRepository.save(device);
                
                log.debug("Updated device {} properties", deviceId);
            } else {
                log.warn("Device not found: {}", deviceId);
            }
            
        } catch (Exception e) {
            log.error("Error processing telemetry", e);
        }
    }
    
    /**
     * 处理事件上报
     */
    private void handleEvent(String topic, String payload) {
        try {
            EventMessage eventMessage = objectMapper.readValue(payload, EventMessage.class);
            String deviceId = eventMessage.getDeviceId();
            
            log.info("Event from device {}: {} ({})", 
                    deviceId, 
                    eventMessage.getEventIdentifier(), 
                    eventMessage.getEventType());
            
            // 这里可以触发场景引擎或告警系统
            // TODO: 集成场景引擎
            
        } catch (Exception e) {
            log.error("Error processing event", e);
        }
    }
    
    /**
     * 处理命令响应
     */
    private void handleResponse(String topic, String payload) {
        try {
            log.info("Command response: {}", payload);
            // TODO: 处理命令响应，更新命令执行状态
            
        } catch (Exception e) {
            log.error("Error processing response", e);
        }
    }
    
    /**
     * 处理设备注册
     */
    private void handleRegister(String topic, String payload) {
        try {
            RegisterMessage registerMessage = objectMapper.readValue(payload, RegisterMessage.class);
            String deviceId = registerMessage.getDeviceId();
            
            log.info("Device registration request: {}", deviceId);
            
            // 检查设备是否已存在
            Optional<Device> existingDevice = deviceRepository.findByDeviceId(deviceId);
            if (existingDevice.isEmpty()) {
                log.info("New device detected, auto-registration may be required: {}", deviceId);
                // TODO: 实现自动注册逻辑或发送通知给管理员
            } else {
                log.info("Device {} is already registered", deviceId);
            }
            
        } catch (Exception e) {
            log.error("Error processing registration", e);
        }
    }
    
    /**
     * 处理设备上线
     */
    private void handleOnline(String topic, String payload) {
        try {
            DeviceMessage message = objectMapper.readValue(payload, DeviceMessage.class);
            String deviceId = message.getDeviceId();
            
            log.info("Device online: {}", deviceId);
            
            deviceRepository.findByDeviceId(deviceId).ifPresent(device -> {
                device.setStatus(DeviceStatus.ONLINE);
                deviceRepository.save(device);
            });
            
        } catch (Exception e) {
            log.error("Error processing online status", e);
        }
    }
    
    /**
     * 处理心跳
     */
    private void handleHeartbeat(String topic, String payload) {
        try {
            DeviceMessage message = objectMapper.readValue(payload, DeviceMessage.class);
            String deviceId = message.getDeviceId();
            
            log.debug("Heartbeat from device: {}", deviceId);
            
            // 更新设备在线状态
            deviceRepository.findByDeviceId(deviceId).ifPresent(device -> {
                if (device.getStatus() != DeviceStatus.ONLINE) {
                    device.setStatus(DeviceStatus.ONLINE);
                    deviceRepository.save(device);
                }
            });
            
        } catch (Exception e) {
            log.error("Error processing heartbeat", e);
        }
    }
}
