package com.example.floatingflavors.app.feature.menu.data.remote.dto

// BOOKING MENU SCREEN USAGE OF THIS CLASS HELPER CLASS FOR MENUITEMDTO

// Helper extension properties/functions for MenuItemDto
val MenuItemDto.idAsInt: Int?
    get() = id?.toIntOrNull()

val MenuItemDto.priceAsDouble: Double?
    get() = price?.toDoubleOrNull()

val MenuItemDto.ratingAsFloat: Float?
    get() = rating ?: null

val MenuItemDto.isAvailableAsBoolean: Boolean?
    get() = is_available?.toBooleanStrictOrNull()