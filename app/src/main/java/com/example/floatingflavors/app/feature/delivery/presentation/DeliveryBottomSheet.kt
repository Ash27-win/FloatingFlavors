package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectHorizontalDragGestures

@Composable
fun DeliveryBottomSheet(
    etaMin: Int,
    enabled: Boolean,
    onArrived: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estimated Arrival",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$etaMin mins",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.Black
            )

            Spacer(Modifier.height(24.dp))

            SlideToArriveButton(enabled = enabled, onArrived = onArrived)
        }
    }
}

@Composable
fun SlideToArriveButton(
    enabled: Boolean,
    onArrived: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isConfirmed by remember { mutableStateOf(false) }
    
    // Hardcoded max swipe distance for typical phone widths,
    // though ideally we'd use onGloballyPositioned.
    val maxDragX = 600f 

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFE5E5E5), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        // Background Text
        Text(
            text = if (isConfirmed) "Arrived" else "Slide to Arrive",
            color = Color.Gray,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // Draggable Thumb
        if (enabled && !isConfirmed) {
            Box(
                modifier = Modifier
                    .offset { androidx.compose.ui.unit.IntOffset(offsetX.toInt(), 0) }
                    .size(56.dp)
                    .background(Color.White, CircleShape)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > maxDragX * 0.7f) {
                                    isConfirmed = true
                                    onArrived()
                                } else {
                                    offsetX = 0f
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount).coerceIn(0f, maxDragX)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowForward, "Slide", tint = Color.Black)
            }
        }
    }
}
