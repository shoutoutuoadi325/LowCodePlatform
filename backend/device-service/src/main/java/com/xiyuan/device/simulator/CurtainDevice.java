package com.xiyuan.device.simulator;

import com.xiyuan.common.enums.DeviceType;

import java.util.Map;

public class CurtainDevice extends SimulatedDevice {

    public CurtainDevice(String id, String name, String location, String floor, String room) {
        super(id, name, DeviceType.CURTAIN, location, floor, room);
    }

    @Override
    protected void initializeState() {
        updateState("position", 0);
    }

    @Override
    public void executeAction(String action, Map<String, Object> parameters) {
        switch (action) {
            case "open":
                updateState("position", 100);
                break;
            case "close":
                updateState("position", 0);
                break;
            case "setPosition":
                if (parameters != null && parameters.containsKey("position")) {
                    updateState("position", parameters.get("position"));
                }
                break;
        }
    }
}
