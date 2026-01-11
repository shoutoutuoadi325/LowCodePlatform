package com.xiyuan.iot.scene.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "triggers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trigger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerType type;
    
    private String deviceId;
    
    private String condition;
    
    @ElementCollection
    @CollectionTable(name = "trigger_params", 
                     joinColumns = @JoinColumn(name = "trigger_id"))
    @MapKeyColumn(name = "param_key")
    @Column(name = "param_value")
    @Builder.Default
    private Map<String, String> parameters = new HashMap<>();
}
