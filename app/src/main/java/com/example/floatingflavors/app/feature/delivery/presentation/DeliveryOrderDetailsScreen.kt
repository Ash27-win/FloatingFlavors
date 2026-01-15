package com.example.floatingflavors.app.feature.delivery.presentation

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    LaunchedEffect(Unit) { viewModel.loadOrderDetails() }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val data = order ?: return

    val canAccept =
        data.deliveryPartnerId == null &&
                (data.status == "CONFIRMED" || data.status == "PREPARING")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F6))
            .verticalScroll(rememberScrollState())
    ) {

        /* ---------------- TOP BAR ---------------- */
        TopAppBar(
            title = { Text("Order Details", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.HelpOutline, null)
                }
            }
        )

        /* ---------------- MAP PREVIEW (STATIC) ---------------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFFE5E5E5))
        )

        Column(Modifier.padding(16.dp)) {

            /* ---------------- ORDER HEADER ---------------- */
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Order #FF-${data.id}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(12.dp))
                Surface(
                    color = Color(0xFFE6F7EC),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "NEW REQUEST",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(0xFF16A34A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row {
                InfoChip(Icons.Default.Payments, "Online Payment")
                Spacer(Modifier.width(8.dp))
                InfoChip(Icons.Default.Schedule, "Est. 25 mins")
            }

            Spacer(Modifier.height(16.dp))

            /* ---------------- PICKUP / DROP CARD ---------------- */
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {

                    LocationItem(
                        iconBg = Color(0xFFFFEDD5),
                        icon = Icons.Default.Store,
                        title = "PICKUP",
                        name = "Floating Flavors Hub A",
                        address = pickupAddress,
                        extra = "1.2 mi away"
                    )

                    Spacer(Modifier.height(16.dp))

                    LocationItem(
                        iconBg = Color(0xFFFEE2E2),
                        icon = Icons.Default.LocationOn,
                        title = "DROP-OFF",
                        name = "Home",
                        address = data.deliveryAddress ?: "Address unavailable",
                        extra = "Leave at door"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            /* ---------------- CUSTOMER CARD ---------------- */
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFE5E5E5), CircleShape)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(data.customerName, fontWeight = FontWeight.Bold)
                        Text(
                            "Customer since 2021",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, Color(0xFFEA580C), CircleShape),
                        onClick = {
                            data.customerPhone?.let {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it"))
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Call, null, tint = Color(0xFFEA580C))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---------------- ACTION BUTTONS ---------------- */
            if (canAccept) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.rejectOrder() }
                    ) {
                        Text("Reject")
                    }

                    Button(
                        modifier = Modifier.weight(2f),
                        colors = ButtonDefaults.buttonColors(Color(0xFFEA580C)),
                        onClick = {

                            if (!DeliveryPermissionHandler.hasLocationPermission(activity)) {
                                DeliveryPermissionHandler.requestLocationPermission(activity)
                                Toast.makeText(
                                    context,
                                    "Allow location permission",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            val lm =
                                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                context.startActivity(
                                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                )
                                Toast.makeText(
                                    context,
                                    "Turn ON GPS to accept order",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            viewModel.acceptOrderAndStartTracking(context)
                        }
                    ) {
                        Text("Accept Order →")
                    }
                }
            }
        }
    }
}

/* ---------------- COMPONENTS ---------------- */

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F1F1)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(16.dp), tint = Color.Gray)
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun LocationItem(
    iconBg: Color,
    icon: ImageVector,
    title: String,
    name: String,
    address: String,
    extra: String
) {
    Row(verticalAlignment = Alignment.Top) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color(0xFFEA580C))
            }
            Spacer(
                Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(Color(0xFFE5E7EB))
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(name, fontWeight = FontWeight.Bold)
            Text(address, fontSize = 13.sp, color = Color.Gray)
            Text(extra, fontSize = 12.sp, color = Color(0xFFEA580C))
        }

        Icon(
            Icons.Default.Navigation,
            null,
            tint = Color(0xFFEA580C),
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFFFEDD5), CircleShape)
                .padding(8.dp)
        )
    }
}
