package com.xiyuan.iot.scene.controller;

import com.xiyuan.iot.scene.model.Scene;
import com.xiyuan.iot.scene.service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scenes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SceneController {
    
    private final SceneService sceneService;
    
    @PostMapping
    public ResponseEntity<Scene> createScene(@RequestBody Scene scene) {
        return ResponseEntity.ok(sceneService.createScene(scene));
    }
    
    @GetMapping
    public ResponseEntity<List<Scene>> getAllScenes() {
        return ResponseEntity.ok(sceneService.getAllScenes());
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Scene>> getActiveScenes() {
        return ResponseEntity.ok(sceneService.getActiveScenes());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Scene> getScene(@PathVariable Long id) {
        return ResponseEntity.ok(sceneService.getScene(id));
    }
    
    @GetMapping("/by-scene-id/{sceneId}")
    public ResponseEntity<Scene> getSceneBySceneId(@PathVariable String sceneId) {
        return ResponseEntity.ok(sceneService.getSceneBySceneId(sceneId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Scene> updateScene(@PathVariable Long id, @RequestBody Scene scene) {
        return ResponseEntity.ok(sceneService.updateScene(id, scene));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long id) {
        sceneService.deleteScene(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{sceneId}/execute")
    public ResponseEntity<Map<String, Object>> executeScene(@PathVariable String sceneId) {
        boolean success = sceneService.executeScene(sceneId);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "sceneId", sceneId,
                "message", success ? "Scene executed successfully" : "Failed to execute scene"
        ));
    }
}
