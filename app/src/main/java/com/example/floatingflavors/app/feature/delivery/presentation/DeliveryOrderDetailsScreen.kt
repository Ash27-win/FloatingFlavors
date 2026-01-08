package com.example.floatingflavors.app.feature.delivery.presentation

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryPermissionHandler

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryOrderDetailsScreen(
    orderId: Int,
    deliveryPartnerId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val viewModel: DeliveryOrderDetailsViewModel = viewModel(
        factory = DeliveryOrderDetailsViewModelFactory(orderId, deliveryPartnerId)
    )

    val order by viewModel.order.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pickupAddress by viewModel.pickupAddress.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrderDetails()
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val data = order ?: return

    // 🔥 CORE LOGIC (DO NOT CHANGE)
    val canAccept =
        data.deliveryPartnerId == null &&
                (data.status == "CONFIRMED" || data.status == "PREPARING")

    val isAccepted =
        data.deliveryPartnerId == deliveryPartnerId.toString() &&
                data.status == "OUT_FOR_DELIVERY"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .verticalScroll(rememberScrollState())
    ) {

        TopAppBar(
            title = { Text("Order Details", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFFE5E5E5))
        )

        Column(Modifier.padding(16.dp)) {

            Text(
                text = "Order #FF-${data.id}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    LocationRow(
                        "PICKUP",
                        "Your Live Location",
                        pickupAddress,
                        Icons.Default.Navigation
                    )
                    Spacer(Modifier.height(12.dp))
                    LocationRow(
                        "DROP-OFF",
                        "Customer Address",
                        data.deliveryAddress ?: "Address unavailable",
                        Icons.Default.Navigation
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ✅ ACCEPT / REJECT
            if (canAccept) {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {

                        if (!DeliveryPermissionHandler.hasLocationPermission(activity)) {
                            DeliveryPermissionHandler.requestLocationPermission(activity)
                            Toast.makeText(context, "Allow location permission", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            Toast.makeText(context, "Turn ON GPS to accept order", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        viewModel.acceptOrderAndStartTracking(context)
                    }
                ) {
                    Text("Accept Order")
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.rejectOrder() }
                ) {
                    Text("Reject")
                }
            }

            // ✅ MARK AS DELIVERED — ONLY AFTER ACCEPT
            if (isAccepted) {
                Spacer(Modifier.height(20.dp))
                Button(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFF2563EB)),
                    onClick = { viewModel.markAsDelivered(context) }
                ) {
                    Text("Mark as Delivered")
                }
            }
        }
    }
}

@Composable
private fun LocationRow(
    title: String,
    name: String,
    address: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(name, fontWeight = FontWeight.Bold)
            Text(address, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(icon, contentDescription = null, tint = Color(0xFFFF7A00))
    }
}
