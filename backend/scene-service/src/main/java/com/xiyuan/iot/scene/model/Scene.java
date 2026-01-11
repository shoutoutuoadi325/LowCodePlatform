package com.xiyuan.iot.scene.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String sceneId;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SceneStatus status;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "scene_fk", nullable = true)
    @Builder.Default
    private List<Trigger> triggers = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "scene_fk", nullable = true)
    @Builder.Default
    private List<Action> actions = new ArrayList<>();
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
