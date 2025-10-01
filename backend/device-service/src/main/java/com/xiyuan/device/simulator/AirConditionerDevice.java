package com.xiyuan.device.simulator;

import com.xiyuan.common.enums.DeviceType;

import java.util.Map;

public class AirConditionerDevice extends SimulatedDevice {

    public AirConditionerDevice(String id, String name, String location, String floor, String room) {
        super(id, name, DeviceType.AIR_CONDITIONER, location, floor, room);
    }

    @Override
    protected void initializeState() {
        updateState("power", false);
        updateState("temperature", 26);
        updateState("mode", "cool");
        updateState("fanSpeed", "auto");
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
            case "setTemperature":
                if (parameters != null && parameters.containsKey("temperature")) {
                    updateState("temperature", parameters.get("temperature"));
                }
                break;
            case "setMode":
                if (parameters != null && parameters.containsKey("mode")) {
                    updateState("mode", parameters.get("mode"));
                }
                break;
            case "setFanSpeed":
                if (parameters != null && parameters.containsKey("fanSpeed")) {
                    updateState("fanSpeed", parameters.get("fanSpeed"));
                }
                break;
        }
    }
}
