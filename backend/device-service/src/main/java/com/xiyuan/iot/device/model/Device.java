package com.xiyuan.iot.device.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String deviceId;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType type;
    
    @Column(nullable = false)
    private String location;
    
    private String building;
    
    private String floor;
    
    private String room;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status;
    
    @ElementCollection
    @CollectionTable(name = "device_properties", 
                     joinColumns = @JoinColumn(name = "device_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value")
    @Builder.Default
    private Map<String, String> properties = new HashMap<>();
    
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
