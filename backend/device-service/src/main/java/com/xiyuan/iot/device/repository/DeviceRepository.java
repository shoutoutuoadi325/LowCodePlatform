package com.xiyuan.iot.device.repository;

import com.xiyuan.iot.device.model.Device;
import com.xiyuan.iot.device.model.DeviceStatus;
import com.xiyuan.iot.device.model.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceId(String deviceId);
    List<Device> findByType(DeviceType type);
    List<Device> findByStatus(DeviceStatus status);
    List<Device> findByLocation(String location);
    List<Device> findByBuildingAndFloor(String building, String floor);
    List<Device> findByRoom(String room);
}
