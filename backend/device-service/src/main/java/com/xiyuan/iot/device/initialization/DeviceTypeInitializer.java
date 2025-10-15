package com.xiyuan.iot.device.initialization;

import com.xiyuan.iot.device.model.metamodel.DeviceEventDefinition;
import com.xiyuan.iot.device.model.metamodel.DeviceOperationDefinition;
import com.xiyuan.iot.device.model.metamodel.DevicePropertyDefinition;
import com.xiyuan.iot.device.model.metamodel.DeviceTypeModel;
import com.xiyuan.iot.device.repository.DeviceTypeModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 设备类型初始化器
 * 在应用启动时自动创建默认的设备类型元模型
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceTypeInitializer implements CommandLineRunner {
    
    private final DeviceTypeModelRepository deviceTypeModelRepository;
    
    @Override
    public void run(String... args) {
        log.info("Initializing default device type models...");
        
        initializeTemperatureSensor();
        initializeSmartLight();
        
        log.info("Device type models initialization completed");
    }
    
    /**
     * 初始化温度传感器类型
     */
    private void initializeTemperatureSensor() {
        if (deviceTypeModelRepository.existsByTypeIdentifier("temperature_sensor")) {
            log.info("Temperature sensor type already exists, skipping");
            return;
        }
        
        DeviceTypeModel model = DeviceTypeModel.builder()
                .typeIdentifier("temperature_sensor")
                .typeName("温度传感器")
                .description("环境温度监测传感器")
                .category("sensor")
                .manufacturer("XiYuan IoT")
                .model("TS-1000")
                .version("1.0")
                .protocol("MQTT")
                .enabled(true)
                .build();
        
        // 属性：温度
        DevicePropertyDefinition tempProp = DevicePropertyDefinition.builder()
                .identifier("temperature")
                .name("温度")
                .description("当前环境温度")
                .dataType("double")
                .readOnly(true)
                .unit("℃")
                .minValue(-40.0)
                .maxValue(125.0)
                .defaultValue("22.0")
                .build();
        model.addProperty(tempProp);
        
        // 属性：状态
        DevicePropertyDefinition statusProp = DevicePropertyDefinition.builder()
                .identifier("status")
                .name("状态")
                .description("传感器工作状态")
                .dataType("enum")
                .readOnly(true)
                .enumValues("[\"normal\",\"high\",\"low\",\"error\"]")
                .defaultValue("normal")
                .build();
        model.addProperty(statusProp);
        
        // 操作：校准
        DeviceOperationDefinition calibrateOp = DeviceOperationDefinition.builder()
                .identifier("calibrate")
                .name("校准")
                .description("校准传感器基准温度")
                .inputParameters("[{\"name\":\"temperature\",\"type\":\"double\",\"required\":true}]")
                .returnType("boolean")
                .async(false)
                .build();
        model.addOperation(calibrateOp);
        
        // 事件：温度告警
        DeviceEventDefinition tempAlertEvent = DeviceEventDefinition.builder()
                .identifier("temperatureAlert")
                .name("温度告警")
                .description("温度超出正常范围")
                .eventType("warning")
                .outputParameters("[{\"name\":\"temperature\",\"type\":\"double\"},{\"name\":\"threshold\",\"type\":\"double\"}]")
                .build();
        model.addEvent(tempAlertEvent);
        
        deviceTypeModelRepository.save(model);
        log.info("Created temperature_sensor type model");
    }
    
    /**
     * 初始化智能灯类型
     */
    private void initializeSmartLight() {
        if (deviceTypeModelRepository.existsByTypeIdentifier("smart_light")) {
            log.info("Smart light type already exists, skipping");
            return;
        }
        
        DeviceTypeModel model = DeviceTypeModel.builder()
                .typeIdentifier("smart_light")
                .typeName("智能灯")
                .description("可调光调色智能照明设备")
                .category("actuator")
                .manufacturer("XiYuan IoT")
                .model("SL-2000")
                .version("1.0")
                .protocol("MQTT")
                .enabled(true)
                .build();
        
        // 属性：电源状态
        DevicePropertyDefinition powerProp = DevicePropertyDefinition.builder()
                .identifier("power")
                .name("电源")
                .description("灯的开关状态")
                .dataType("boolean")
                .readOnly(false)
                .defaultValue("false")
                .build();
        model.addProperty(powerProp);
        
        // 属性：亮度
        DevicePropertyDefinition brightnessProp = DevicePropertyDefinition.builder()
                .identifier("brightness")
                .name("亮度")
                .description("灯的亮度级别")
                .dataType("int")
                .readOnly(false)
                .unit("%")
                .minValue(0.0)
                .maxValue(100.0)
                .defaultValue("100")
                .build();
        model.addProperty(brightnessProp);
        
        // 属性：颜色
        DevicePropertyDefinition colorProp = DevicePropertyDefinition.builder()
                .identifier("color")
                .name("颜色")
                .description("灯的颜色（十六进制）")
                .dataType("string")
                .readOnly(false)
                .defaultValue("#FFFFFF")
                .build();
        model.addProperty(colorProp);
        
        // 操作：打开
        DeviceOperationDefinition turnOnOp = DeviceOperationDefinition.builder()
                .identifier("turnOn")
                .name("打开")
                .description("打开智能灯")
                .returnType("boolean")
                .async(false)
                .build();
        model.addOperation(turnOnOp);
        
        // 操作：关闭
        DeviceOperationDefinition turnOffOp = DeviceOperationDefinition.builder()
                .identifier("turnOff")
                .name("关闭")
                .description("关闭智能灯")
                .returnType("boolean")
                .async(false)
                .build();
        model.addOperation(turnOffOp);
        
        // 操作：设置亮度
        DeviceOperationDefinition setBrightnessOp = DeviceOperationDefinition.builder()
                .identifier("setBrightness")
                .name("设置亮度")
                .description("调节灯的亮度")
                .inputParameters("[{\"name\":\"brightness\",\"type\":\"int\",\"required\":true,\"min\":0,\"max\":100}]")
                .returnType("boolean")
                .async(false)
                .build();
        model.addOperation(setBrightnessOp);
        
        // 操作：设置颜色
        DeviceOperationDefinition setColorOp = DeviceOperationDefinition.builder()
                .identifier("setColor")
                .name("设置颜色")
                .description("改变灯的颜色")
                .inputParameters("[{\"name\":\"color\",\"type\":\"string\",\"required\":true}]")
                .returnType("boolean")
                .async(false)
                .build();
        model.addOperation(setColorOp);
        
        deviceTypeModelRepository.save(model);
        log.info("Created smart_light type model");
    }
}
