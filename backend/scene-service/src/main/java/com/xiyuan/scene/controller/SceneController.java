package com.xiyuan.scene.controller;

import com.xiyuan.common.model.Scene;
import com.xiyuan.scene.service.SceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scenes")
@CrossOrigin(origins = "*")
public class SceneController {

    @Autowired
    private SceneService sceneService;

    @GetMapping
    public ResponseEntity<List<Scene>> getAllScenes() {
        return ResponseEntity.ok(sceneService.getAllScenes());
    }

    @GetMapping("/{sceneId}")
    public ResponseEntity<Scene> getScene(@PathVariable String sceneId) {
        return sceneService.getScene(sceneId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Scene> createScene(@RequestBody Scene scene) {
        Scene created = sceneService.createScene(scene);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{sceneId}")
    public ResponseEntity<Scene> updateScene(@PathVariable String sceneId, @RequestBody Scene scene) {
        return sceneService.updateScene(sceneId, scene)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{sceneId}")
    public ResponseEntity<Void> deleteScene(@PathVariable String sceneId) {
        boolean deleted = sceneService.deleteScene(sceneId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{sceneId}/execute")
    public ResponseEntity<Map<String, Object>> executeScene(@PathVariable String sceneId) {
        boolean success = sceneService.executeScene(sceneId);
        return ResponseEntity.ok(Map.of("success", success));
    }
}
