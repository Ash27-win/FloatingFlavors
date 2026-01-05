package com.example.floatingflavors.app.feature.user.presentation.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.floatingflavors.app.feature.user.data.tracking.dto.StatusItem

@Composable
fun DeliveryTimeline(
    timeline: List<StatusItem>,
    currentStatus: String
) {
    Column(modifier = Modifier.padding(16.dp)) {
        timeline.forEach { item ->
            val isActive = item.status == currentStatus

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = when (item.status) {
                        "CONFIRMED" -> Icons.Default.Check
                        "PREPARING" -> Icons.Default.Restaurant
                        "OUT_FOR_DELIVERY" -> Icons.Default.DeliveryDining
                        "DELIVERED" -> Icons.Default.Home
                        else -> Icons.Default.Check
                    },
                    contentDescription = null,
                    tint = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(item.status.replace("_", " "))
                    item.time?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
