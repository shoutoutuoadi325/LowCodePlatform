package com.xiyuan.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scene {
    private String id;
    private String name;
    private String description;
    private List<SceneAction> actions;
    private List<SceneTrigger> triggers;
    private boolean enabled;
}
