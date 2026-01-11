package com.xiyuan.iot.scene.repository;

import com.xiyuan.iot.scene.model.Scene;
import com.xiyuan.iot.scene.model.SceneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    Optional<Scene> findBySceneId(String sceneId);
    List<Scene> findByStatus(SceneStatus status);
}
