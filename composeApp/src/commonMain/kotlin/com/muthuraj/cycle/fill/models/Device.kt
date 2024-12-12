package com.muthuraj.cycle.fill.models

data class Device(
    val name: String,
    val imageUrl: String,
    val documentPath: String,
    val type: DeviceType
)

enum class DeviceType {
    DISHWASHER,
    WATER_PURIFIER,
    DRYER,
    TOOTHBRUSH,
    FLOSS,
    COFFEE_MAKER,
    OTHER
}

data class Consumable(
    val name: String,
    val type: ConsumableType,
    val lastRefillDate: String? = null
)

enum class ConsumableType {
    RINSE_AID,
    SALT,
    DETERGENT,
    FILTER,
    MEMBRANE,
    DESCALER,
    COFFEE_FILTER,
    BRUSH_HEAD,
    FLOSS_HEAD,
    OTHER
}

// Predefined consumables for each device type
val deviceConsumables = mapOf(
    DeviceType.DISHWASHER to listOf(
        Consumable("Rinse Aid", ConsumableType.RINSE_AID),
        Consumable("Salt", ConsumableType.SALT),
        Consumable("Detergent", ConsumableType.DETERGENT)
    ),
    DeviceType.WATER_PURIFIER to listOf(
        Consumable("Filter", ConsumableType.FILTER),
        Consumable("Membrane", ConsumableType.MEMBRANE)
    ),
    DeviceType.COFFEE_MAKER to listOf(
        Consumable("Descaler", ConsumableType.DESCALER),
        Consumable("Filter", ConsumableType.COFFEE_FILTER)
    ),
    DeviceType.TOOTHBRUSH to listOf(
        Consumable("Brush Head", ConsumableType.BRUSH_HEAD)
    ),
    DeviceType.FLOSS to listOf(
        Consumable("Floss Head", ConsumableType.FLOSS_HEAD)
    )
) 