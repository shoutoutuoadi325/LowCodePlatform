package com.xiyuan.iot.scene.service;

import com.xiyuan.iot.scene.engine.SceneExecutor;
import com.xiyuan.iot.scene.model.Scene;
import com.xiyuan.iot.scene.model.SceneStatus;
import com.xiyuan.iot.scene.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SceneService {
    
    private final SceneRepository sceneRepository;
    private final SceneExecutor sceneExecutor;
    
    @Transactional
    public Scene createScene(Scene scene) {
        if (scene.getSceneId() == null || scene.getSceneId().isEmpty()) {
            scene.setSceneId(UUID.randomUUID().toString());
        }
        
        if (scene.getStatus() == null) {
            scene.setStatus(SceneStatus.INACTIVE);
        }
        
        Scene savedScene = sceneRepository.save(scene);
        log.info("Created scene: {}", savedScene.getSceneId());
        
        return savedScene;
    }
    
    public List<Scene> getAllScenes() {
        return sceneRepository.findAll();
    }
    
    public Scene getScene(Long id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scene not found: " + id));
    }
    
    public Scene getSceneBySceneId(String sceneId) {
        return sceneRepository.findBySceneId(sceneId)
                .orElseThrow(() -> new RuntimeException("Scene not found: " + sceneId));
    }
    
    @Transactional
    public Scene updateScene(Long id, Scene scene) {
        Scene existingScene = getScene(id);
        existingScene.setName(scene.getName());
        existingScene.setDescription(scene.getDescription());
        existingScene.setStatus(scene.getStatus());
        existingScene.setTriggers(scene.getTriggers());
        existingScene.setActions(scene.getActions());
        return sceneRepository.save(existingScene);
    }
    
    @Transactional
    public void deleteScene(Long id) {
        sceneRepository.deleteById(id);
    }
    
    public boolean executeScene(String sceneId) {
        Scene scene = getSceneBySceneId(sceneId);
        
        if (scene.getStatus() != SceneStatus.ACTIVE) {
            log.warn("Cannot execute scene {} - status: {}", sceneId, scene.getStatus());
            return false;
        }
        
        return sceneExecutor.executeScene(scene);
    }
    
    public List<Scene> getActiveScenes() {
        return sceneRepository.findByStatus(SceneStatus.ACTIVE);
    }
}
