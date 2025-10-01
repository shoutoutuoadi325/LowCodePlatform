package com.xiyuan.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneAction {
    private String deviceId;
    private String action;
    private Map<String, Object> parameters;
    private Integer delaySeconds;
}
