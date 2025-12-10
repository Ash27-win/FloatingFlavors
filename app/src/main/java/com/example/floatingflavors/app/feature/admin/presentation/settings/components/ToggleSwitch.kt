package com.example.floatingflavors.app.feature.admin.presentation.settings.components

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable

@Composable
fun ToggleSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange
    )
}
