package com.example.floatingflavors.app.feature.user.presentation.booking

data class SmartFilterState(
    val dietary: Set<String> = emptySet(),
    val cuisines: Set<String> = emptySet()
)
