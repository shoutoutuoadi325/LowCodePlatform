package com.xiyuan.device.simulator;

import com.xiyuan.common.enums.DeviceType;

import java.util.Map;

public class ProjectorDevice extends SimulatedDevice {

    public ProjectorDevice(String id, String name, String location, String floor, String room) {
        super(id, name, DeviceType.PROJECTOR, location, floor, room);
    }

    @Override
    protected void initializeState() {
        updateState("power", false);
        updateState("input", "HDMI1");
        updateState("brightness", 100);
    }

    @Override
    public void executeAction(String action, Map<String, Object> parameters) {
        switch (action) {
            case "turnOn":
                updateState("power", true);
                break;
            case "turnOff":
                updateState("power", false);
                break;
            case "setInput":
                if (parameters != null && parameters.containsKey("input")) {
                    updateState("input", parameters.get("input"));
                }
                break;
            case "setBrightness":
                if (parameters != null && parameters.containsKey("brightness")) {
                    updateState("brightness", parameters.get("brightness"));
                }
                break;
        }
    }
}
