package com.sr.asset_service.Enums;

public enum DeviceType {
    PLC, // port 502 (Modbus)
    IOT, // port 1883 (MQTT)
    SERVER,
    WORKSTATION,
    ROUTER,
    SWITCH,
    CAMERA,
    UNKNOWN
}
