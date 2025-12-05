package com.aicode.reviewer.enums;

public enum FindingSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public double toScore() {
        return switch (this) {
            case LOW -> 10;
            case MEDIUM -> 30;
            case HIGH -> 60;
            case CRITICAL -> 90;
        };
    }
}
