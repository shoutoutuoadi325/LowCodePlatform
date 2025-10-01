package com.xiyuan.iot.scene.engine;

import com.xiyuan.iot.scene.model.Action;
import com.xiyuan.iot.scene.model.Scene;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SceneExecutor {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${device.service.url:http://localhost:8081}")
    private String deviceServiceUrl;
    
    public boolean executeScene(Scene scene) {
        log.info("Executing scene: {}", scene.getName());
        
        List<Action> actions = scene.getActions();
        if (actions == null || actions.isEmpty()) {
            log.warn("No actions defined for scene: {}", scene.getSceneId());
            return false;
        }
        
        actions.sort((a1, a2) -> {
            Integer order1 = a1.getOrder() != null ? a1.getOrder() : 0;
            Integer order2 = a2.getOrder() != null ? a2.getOrder() : 0;
            return order1.compareTo(order2);
        });
        
        for (Action action : actions) {
            try {
                executeAction(action);
                
                if (action.getDelaySeconds() != null && action.getDelaySeconds() > 0) {
                    Thread.sleep(action.getDelaySeconds() * 1000L);
                }
            } catch (Exception e) {
                log.error("Error executing action for device {}: {}", 
                         action.getDeviceId(), e.getMessage());
            }
        }
        
        log.info("Scene execution completed: {}", scene.getName());
        return true;
    }
    
    private void executeAction(Action action) {
        String url = String.format("%s/api/devices/%s/control", 
                                   deviceServiceUrl, action.getDeviceId());
        
        Map<String, Object> request = new HashMap<>();
        request.put("action", action.getAction());
        
        Map<String, Object> parameters = new HashMap<>();
        if (action.getParameters() != null) {
            action.getParameters().forEach((key, value) -> {
                try {
                    if (value.matches("-?\\d+(\\.\\d+)?")) {
                        parameters.put(key, Double.parseDouble(value));
                    } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                        parameters.put(key, Boolean.parseBoolean(value));
                    } else {
                        parameters.put(key, value);
                    }
                } catch (NumberFormatException e) {
                    parameters.put(key, value);
                }
            });
        }
        request.put("parameters", parameters);
        
        log.info("Sending control request to device {}: action={}, params={}", 
                action.getDeviceId(), action.getAction(), parameters);
        
        restTemplate.postForObject(url, request, Map.class);
    }
}
