package com.labresource.platform.equipment;

/**
 * Operational and physical status of laboratory equipment.
 * Note: BOOKED is deliberately omitted; booking availability is evaluated dynamically over time windows.
 */
public enum EquipmentStatus {
    AVAILABLE,
    IN_USE,
    UNDER_MAINTENANCE,
    OUT_OF_SERVICE,
    RETIRED
}
