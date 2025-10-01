package com.xiyuan.scene.service;

import com.xiyuan.common.model.Scene;
import com.xiyuan.common.model.SceneAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SceneService {
    private final Map<String, Scene> scenes = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${device.service.url:http://localhost:8081}")
    private String deviceServiceUrl;

    public SceneService() {
        initializeDefaultScenes();
    }

    private void initializeDefaultScenes() {
        // 上课场景
        Scene classScene = Scene.builder()
                .id("scene-class")
                .name("上课模式")
                .description("教室进入上课状态")
                .enabled(true)
                .actions(Arrays.asList(
                        SceneAction.builder()
                                .deviceId("light-101")
                                .action("turnOn")
                                .parameters(Map.of("brightness", 100))
                                .delaySeconds(0)
                                .build(),
                        SceneAction.builder()
                                .deviceId("curtain-101")
                                .action("close")
                                .parameters(Collections.emptyMap())
                                .delaySeconds(0)
                                .build(),
                        SceneAction.builder()
                                .deviceId("proj-101")
                                .action("turnOn")
                                .parameters(Collections.emptyMap())
                                .delaySeconds(2)
                                .build()
                ))
                .triggers(Collections.emptyList())
                .build();

        // 下课场景
        Scene afterClassScene = Scene.builder()
                .id("scene-after-class")
                .name("下课模式")
                .description("教室进入下课状态")
                .enabled(true)
                .actions(Arrays.asList(
                        SceneAction.builder()
                                .deviceId("proj-101")
                                .action("turnOff")
                                .parameters(Collections.emptyMap())
                                .delaySeconds(0)
                                .build(),
                        SceneAction.builder()
                                .deviceId("curtain-101")
                                .action("open")
                                .parameters(Collections.emptyMap())
                                .delaySeconds(1)
                                .build()
                ))
                .triggers(Collections.emptyList())
                .build();

        // 节能模式
        Scene energySavingScene = Scene.builder()
                .id("scene-energy-saving")
                .name("节能模式")
                .description("降低能耗")
                .enabled(true)
                .actions(Arrays.asList(
                        SceneAction.builder()
                                .deviceId("light-101")
                                .action("setBrightness")
                                .parameters(Map.of("brightness", 50))
                                .delaySeconds(0)
                                .build(),
                        SceneAction.builder()
                                .deviceId("ac-101")
                                .action("setTemperature")
                                .parameters(Map.of("temperature", 28))
                                .delaySeconds(0)
                                .build()
                ))
                .triggers(Collections.emptyList())
                .build();

        scenes.put(classScene.getId(), classScene);
        scenes.put(afterClassScene.getId(), afterClassScene);
        scenes.put(energySavingScene.getId(), energySavingScene);
    }

    public List<Scene> getAllScenes() {
        return new ArrayList<>(scenes.values());
    }

    public Optional<Scene> getScene(String sceneId) {
        return Optional.ofNullable(scenes.get(sceneId));
    }

    public Scene createScene(Scene scene) {
        if (scene.getId() == null || scene.getId().isEmpty()) {
            scene.setId("scene-" + UUID.randomUUID().toString());
        }
        scenes.put(scene.getId(), scene);
        return scene;
    }

    public Optional<Scene> updateScene(String sceneId, Scene scene) {
        if (scenes.containsKey(sceneId)) {
            scene.setId(sceneId);
            scenes.put(sceneId, scene);
            return Optional.of(scene);
        }
        return Optional.empty();
    }

    public boolean deleteScene(String sceneId) {
        return scenes.remove(sceneId) != null;
    }

    public boolean executeScene(String sceneId) {
        Scene scene = scenes.get(sceneId);
        if (scene == null || !scene.isEnabled()) {
            return false;
        }

        for (SceneAction action : scene.getActions()) {
            try {
                if (action.getDelaySeconds() != null && action.getDelaySeconds() > 0) {
                    Thread.sleep(action.getDelaySeconds() * 1000L);
                }

                String url = deviceServiceUrl + "/api/devices/" + action.getDeviceId() + "/action";
                Map<String, Object> request = new HashMap<>();
                request.put("action", action.getAction());
                request.put("parameters", action.getParameters());

                restTemplate.postForEntity(url, request, Map.class);
            } catch (Exception e) {
                System.err.println("Error executing action: " + e.getMessage());
            }
        }
        return true;
    }
}
