package com.xiyuan.iot.simulator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 设备模拟器应用
 * 独立运行的设备模拟器，通过MQTT与IoT平台通信
 */
@SpringBootApplication
@Slf4j
public class DeviceSimulatorApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DeviceSimulatorApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner run(DeviceSimulatorManager manager) {
        return args -> {
            log.info("==================================================");
            log.info("  IoT Device Simulator Started");
            log.info("==================================================");
            
            // 启动设备模拟器
            manager.startSimulation();
        };
    }
}
