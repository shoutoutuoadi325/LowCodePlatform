package com.xiyuan.device.simulator;

import com.xiyuan.common.enums.DeviceType;

import java.util.Map;

public class LightDevice extends SimulatedDevice {

    public LightDevice(String id, String name, String location, String floor, String room) {
        super(id, name, DeviceType.LIGHT, location, floor, room);
    }

    @Override
    protected void initializeState() {
        updateState("power", false);
        updateState("brightness", 0);
        updateState("color", "#FFFFFF");
    }

    @Override
    public void executeAction(String action, Map<String, Object> parameters) {
        switch (action) {
            case "turnOn":
                updateState("power", true);
                if (parameters != null && parameters.containsKey("brightness")) {
                    updateState("brightness", parameters.get("brightness"));
                } else {
                    updateState("brightness", 100);
                }
                break;
            case "turnOff":
                updateState("power", false);
                updateState("brightness", 0);
                break;
            case "setBrightness":
                if (parameters != null && parameters.containsKey("brightness")) {
                    updateState("brightness", parameters.get("brightness"));
                    if ((Integer) parameters.get("brightness") > 0) {
                        updateState("power", true);
                    }
                }
                break;
            case "setColor":
                if (parameters != null && parameters.containsKey("color")) {
                    updateState("color", parameters.get("color"));
                }
                break;
        }
    }
}
