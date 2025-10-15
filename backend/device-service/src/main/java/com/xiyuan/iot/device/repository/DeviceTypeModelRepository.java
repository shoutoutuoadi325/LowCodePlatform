package com.xiyuan.iot.device.repository;

import com.xiyuan.iot.device.model.metamodel.DeviceTypeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceTypeModelRepository extends JpaRepository<DeviceTypeModel, Long> {
    
    Optional<DeviceTypeModel> findByTypeIdentifier(String typeIdentifier);
    
    boolean existsByTypeIdentifier(String typeIdentifier);
}
