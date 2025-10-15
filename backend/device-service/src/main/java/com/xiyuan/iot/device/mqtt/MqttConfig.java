package com.xiyuan.iot.device.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * MQTT配置类
 * 配置平台与设备之间的MQTT通信
 */
@Configuration
@Slf4j
public class MqttConfig {
    
    @Value("${mqtt.broker.url:tcp://mosquitto:1883}")
    private String brokerUrl;
    
    @Value("${mqtt.client.id:iot-platform}")
    private String clientId;
    
    @Value("${mqtt.username:}")
    private String username;
    
    @Value("${mqtt.password:}")
    private String password;
    
    /**
     * MQTT主题定义
     */
    public static class Topics {
        // 设备 -> 平台
        public static final String DEVICE_TELEMETRY = "iot/devices/+/telemetry";  // 遥测数据
        public static final String DEVICE_EVENT = "iot/devices/+/event";          // 事件上报
        public static final String DEVICE_RESPONSE = "iot/devices/+/response";    // 命令响应
        public static final String DEVICE_REGISTER = "iot/devices/+/register";    // 设备注册
        public static final String DEVICE_ONLINE = "iot/devices/+/online";        // 设备上线
        public static final String DEVICE_HEARTBEAT = "iot/devices/+/heartbeat";  // 心跳
        
        // 平台 -> 设备
        public static final String DEVICE_COMMAND_PATTERN = "iot/devices/%s/command";  // 控制命令
        public static final String DEVICE_CONFIG_PATTERN = "iot/devices/%s/config";    // 配置更新
        
        public static String getCommandTopic(String deviceId) {
            return String.format(DEVICE_COMMAND_PATTERN, deviceId);
        }
        
        public static String getConfigTopic(String deviceId) {
            return String.format(DEVICE_CONFIG_PATTERN, deviceId);
        }
    }
    
    /**
     * MQTT客户端工厂
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        
        options.setServerURIs(new String[]{brokerUrl});
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        
        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }
        
        factory.setConnectionOptions(options);
        log.info("MQTT client factory configured with broker: {}", brokerUrl);
        
        return factory;
    }
    
    /**
     * 输入通道 - 接收来自设备的消息
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }
    
    /**
     * 输出通道 - 发送消息到设备
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }
    
    /**
     * MQTT消息入站适配器 - 订阅设备主题
     */
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId + "-inbound",
                        mqttClientFactory(),
                        Topics.DEVICE_TELEMETRY,
                        Topics.DEVICE_EVENT,
                        Topics.DEVICE_RESPONSE,
                        Topics.DEVICE_REGISTER,
                        Topics.DEVICE_ONLINE,
                        Topics.DEVICE_HEARTBEAT
                );
        
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        
        log.info("MQTT inbound adapter configured");
        return adapter;
    }
    
    /**
     * MQTT消息出站处理器 - 发送消息到设备
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(clientId + "-outbound", mqttClientFactory());
        
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("iot/devices/default");
        messageHandler.setDefaultQos(1);
        
        log.info("MQTT outbound handler configured");
        return messageHandler;
    }
}
