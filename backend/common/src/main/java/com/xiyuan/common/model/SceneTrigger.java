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
public class SceneTrigger {
    private String type;
    private String deviceId;
    private String condition;
    private Map<String, Object> parameters;
}
