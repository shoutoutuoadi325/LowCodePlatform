package com.xiyuan.iot.simulator.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 虚拟设备基类
 * 所有模拟设备的基础框架
 */
@Slf4j
public abstract class VirtualDevice {
    
    protected final String deviceId;
    protected final String deviceType;
    protected final String deviceName;
    protected final MqttClient mqttClient;
    protected final ObjectMapper objectMapper;
    protected final ScheduledExecutorService scheduler;
    protected final Map<String, Object> properties;
    
    protected boolean running = false;
    
    public VirtualDevice(String deviceId, String deviceType, String deviceName, String brokerUrl) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.deviceName = deviceName;
        this.properties = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        try {
            this.mqttClient = new MqttClient(brokerUrl, deviceId, null);
            setupMqttCallbacks();
        } catch (MqttException e) {
            log.error("Failed to create MQTT client for device: {}", deviceId, e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 设置MQTT回调
     */
    private void setupMqttCallbacks() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.warn("Device {} connection lost", deviceId);
                reconnect();
            }
            
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                handleCommand(topic, new String(message.getPayload()));
            }
            
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Not used for subscriber
            }
        });
    }
    
    /**
     * 连接到MQTT Broker
     */
    public void connect() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);
            
            mqttClient.connect(options);
            log.info("Device {} connected to MQTT broker", deviceId);
            
            // 订阅命令主题
            String commandTopic = String.format("iot/devices/%s/command", deviceId);
            mqttClient.subscribe(commandTopic, 1);
            log.info("Device {} subscribed to {}", deviceId, commandTopic);
            
            // 发送上线消息
            sendOnlineMessage();
            
        } catch (MqttException e) {
            log.error("Failed to connect device: {}", deviceId, e);
        }
    }
    
    /**
     * 重连
     */
    private void reconnect() {
        scheduler.schedule(() -> {
            if (!mqttClient.isConnected()) {
                log.info("Attempting to reconnect device: {}", deviceId);
                connect();
            }
        }, 5, TimeUnit.SECONDS);
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        running = false;
        scheduler.shutdownNow();
        
        try {
            if (mqttClient.isConnected()) {
                sendOfflineMessage();
                mqttClient.disconnect();
                mqttClient.close();
                log.info("Device {} disconnected", deviceId);
            }
        } catch (MqttException e) {
            log.error("Error disconnecting device: {}", deviceId, e);
        }
    }
    
    /**
     * 启动设备模拟
     */
    public void start() {
        running = true;
        connect();
        
        // 启动定时上报遥测数据
        scheduler.scheduleAtFixedRate(this::reportTelemetry, 5, 10, TimeUnit.SECONDS);
        
        // 启动定时心跳
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 0, 30, TimeUnit.SECONDS);
        
        // 调用设备特定的初始化
        onStart();
        
        log.info("Device {} started simulation", deviceId);
    }
    
    /**
     * 发送遥测数据
     */
    protected void reportTelemetry() {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("messageType", "TELEMETRY");
            message.put("deviceId", deviceId);
            message.put("timestamp", System.currentTimeMillis());
            message.put("messageId", UUID.randomUUID().toString());
            message.put("properties", getCurrentProperties());
            
            String topic = String.format("iot/devices/%s/telemetry", deviceId);
            String payload = objectMapper.writeValueAsString(message);
            
            publishMessage(topic, payload);
            log.debug("Device {} reported telemetry", deviceId);
            
        } catch (Exception e) {
            log.error("Error reporting telemetry for device: {}", deviceId, e);
        }
    }
    
    /**
     * 发送事件
     */
    protected void reportEvent(String eventIdentifier, String eventType, Map<String, Object> eventData) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("messageType", "EVENT");
            message.put("deviceId", deviceId);
            message.put("timestamp", System.currentTimeMillis());
            message.put("messageId", UUID.randomUUID().toString());
            message.put("eventIdentifier", eventIdentifier);
            message.put("eventType", eventType);
            message.put("eventData", eventData);
            
            String topic = String.format("iot/devices/%s/event", deviceId);
            String payload = objectMapper.writeValueAsString(message);
            
            publishMessage(topic, payload);
            log.info("Device {} reported event: {}", deviceId, eventIdentifier);
            
        } catch (Exception e) {
            log.error("Error reporting event for device: {}", deviceId, e);
        }
    }
    
    /**
     * 发送心跳
     */
    private void sendHeartbeat() {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("messageType", "HEARTBEAT");
            message.put("deviceId", deviceId);
            message.put("timestamp", System.currentTimeMillis());
            message.put("messageId", UUID.randomUUID().toString());
            
            String topic = String.format("iot/devices/%s/heartbeat", deviceId);
            String payload = objectMapper.writeValueAsString(message);
            
            publishMessage(topic, payload);
            log.debug("Device {} sent heartbeat", deviceId);
            
        } catch (Exception e) {
            log.error("Error sending heartbeat for device: {}", deviceId, e);
        }
    }
    
    /**
     * 发送上线消息
     */
    private void sendOnlineMessage() {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("messageType", "ONLINE");
            message.put("deviceId", deviceId);
            message.put("timestamp", System.currentTimeMillis());
            message.put("messageId", UUID.randomUUID().toString());
            
            String topic = String.format("iot/devices/%s/online", deviceId);
            String payload = objectMapper.writeValueAsString(message);
            
            publishMessage(topic, payload);
            log.info("Device {} sent online message", deviceId);
            
        } catch (Exception e) {
            log.error("Error sending online message for device: {}", deviceId, e);
        }
    }
    
    /**
     * 发送离线消息
     */
    private void sendOfflineMessage() {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("messageType", "OFFLINE");
            message.put("deviceId", deviceId);
            message.put("timestamp", System.currentTimeMillis());
            
            String topic = String.format("iot/devices/%s/offline", deviceId);
            String payload = objectMapper.writeValueAsString(message);
            
            publishMessage(topic, payload);
            log.info("Device {} sent offline message", deviceId);
            
        } catch (Exception e) {
            log.error("Error sending offline message for device: {}", deviceId, e);
        }
    }
    
    /**
     * 发布MQTT消息
     */
    protected void publishMessage(String topic, String payload) {
        try {
            if (mqttClient.isConnected()) {
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);
                mqttClient.publish(topic, message);
            } else {
                log.warn("Cannot publish message, device {} not connected", deviceId);
            }
        } catch (MqttException e) {
            log.error("Error publishing message for device: {}", deviceId, e);
        }
    }
    
    /**
     * 处理来自平台的命令 - 需要子类实现
     */
    protected abstract void handleCommand(String topic, String payload);
    
    /**
     * 获取当前属性 - 需要子类实现
     */
    protected abstract Map<String, Object> getCurrentProperties();
    
    /**
     * 设备特定的启动逻辑 - 子类可选实现
     */
    protected void onStart() {
        // Default: do nothing
    }
    
    /**
     * 更新属性
     */
    protected void updateProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    /**
     * 获取属性
     */
    protected Object getProperty(String key) {
        return properties.get(key);
    }
}
