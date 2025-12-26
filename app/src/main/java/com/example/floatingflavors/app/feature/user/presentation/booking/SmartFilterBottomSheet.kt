package com.example.floatingflavors.app.feature.user.presentation.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.FlowRow

@Composable
fun SmartFilterBottomSheet(
    initialState: SmartFilterState,
    onDismiss: () -> Unit,
    onApply: (SmartFilterState) -> Unit
) {
    var dietary by remember { mutableStateOf(initialState.dietary) }
    var cuisines by remember { mutableStateOf(initialState.cuisines) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF102216), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(20.dp)
    ) {

        /* ---------- HEADER ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Reset",
                color = Color(0xFF9DB9A6),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    dietary = emptySet()
                    cuisines = emptySet()
                }
            )

            Text(
                "Smart Filter",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))

        /* ---------- DIETARY ---------- */
        Text("Dietary Preferences", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Vegetarian", "Vegan", "Gluten-Free", "Halal", "Keto", "Paleo")
                .forEach { label ->
                    FilterChipDark(
                        label = label,
                        selected = dietary.contains(label),
                        onClick = {
                            dietary =
                                if (dietary.contains(label)) dietary - label
                                else dietary + label
                        }
                    )
                }
        }

        Spacer(Modifier.height(24.dp))

        Divider(color = Color.White.copy(alpha = 0.1f))

        Spacer(Modifier.height(16.dp))

        /* ---------- CUISINE ---------- */
        Text("Cuisine Type", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        listOf("Indian", "Chinese", "Italian", "Continental", "Mexican")
            .forEach { cuisine ->
                CuisineCheckbox(
                    label = cuisine,
                    checked = cuisines.contains(cuisine),
                    onChecked = {
                        cuisines =
                            if (cuisines.contains(cuisine)) cuisines - cuisine
                            else cuisines + cuisine
                    }
                )
            }

        Spacer(Modifier.height(24.dp))

        /* ---------- APPLY ---------- */
        val totalSelected = dietary.size + cuisines.size

        Button(
            enabled = totalSelected > 0,
            onClick = {
                onApply(
                    SmartFilterState(
                        dietary = dietary,
                        cuisines = cuisines
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF13EC5B),
                disabledContainerColor = Color(0xFF7EDFA0)
            )
        ) {
            Text(
                "Apply Filters ($totalSelected)",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ---------- SMALL COMPONENTS ---------- */

@Composable
private fun FilterChipDark(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Color(0xFF13EC5B) else Color(0xFF28392E),
                RoundedCornerShape(50)
            )
            .border(
                1.dp,
                if (selected) Color(0xFF13EC5B) else Color.Transparent,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CuisineCheckbox(
    label: String,
    checked: Boolean,
    onChecked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChecked() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White)
        Checkbox(
            checked = checked,
            onCheckedChange = { onChecked() },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF13EC5B),
                uncheckedColor = Color(0xFF3B5443)
            )
        )
    }
}
