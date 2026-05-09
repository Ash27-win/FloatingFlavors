package com.example.floatingflavors.app.feature.delivery.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.floatingflavors.app.core.util.TestTags
import com.example.floatingflavors.app.feature.delivery.presentation.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryHelpSupportScreen(
    onBack: () -> Unit,
    onChatWithSupport: () -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    
    val categories = listOf(
        Pair("Order Issues", Icons.Default.Receipt),
        Pair("Payment Queries", Icons.Default.Payments),
        Pair("App Technical Support", Icons.Default.Build),
        Pair("Safety & Emergency", Icons.Default.MedicalServices)
    )

    val faqs = listOf(
        "How do I change my payout schedule?" to "Payouts are scheduled weekly by default. You can change this to daily in your Earnings settings.",
        "What to do if a customer is unreachable?" to "Try calling them at least 3 times. If no response after 5 minutes at the location, contact support.",
        "How are tips calculated?" to "Tips are paid 100% to partners and are visible in your earnings immediately after delivery.",
        "What are the vehicle requirements?" to "Vehicles must be under 10 years old with valid RC, Insurance, and PUC certificates."
    )

    val expandedIndex = remember { mutableStateOf(-1) }

    Scaffold(
        modifier = Modifier.testTag(TestTags.DELIVERY_HELP_SUPPORT_SCREEN),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "HELP & SUPPORT",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Back", modifier = Modifier.padding(8.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFBF7))
            )
        },
        containerColor = Color(0xFFFFFBF7)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search Bar
            TextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    placeholder = { Text("Search for issues", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
            )
            
            Spacer(Modifier.height(24.dp))
 
             // Chat with Support Card
             Card(
                    modifier = Modifier.fillMaxWidth().clickable { onChatWithSupport() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).background(Color(0xFFFFF3E0), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFFFF6D00), modifier = Modifier.size(28.dp))
                            // Small green dot for online
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = -6.dp)
                                    .size(14.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Chat with Support", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Instant resolution with our\nAI assistant", fontSize = 12.sp, color = Color.Gray)
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFFF6D00), modifier = Modifier.padding(6.dp))
                        }
                    }
            }
 
            Spacer(Modifier.height(30.dp))
 
            SectionHeader("CATEGORIES")
 
            Spacer(Modifier.height(16.dp))
 
            // Categories Grid - Refactored for stability
            val chunkedCategories = categories.chunked(2)
            for (rowCategories in chunkedCategories) {
                CategoryRow(rowCategories)
            }
 
            SectionHeader("FREQUENTLY ASKED QUESTIONS")
 
            Spacer(Modifier.height(16.dp))
 
            // FAQs - Refactored to avoid nested measurement crashes
            faqs.forEach { faqPair: Pair<String, String> ->
                val (question, answer) = faqPair
                val index = faqs.indexOf(faqPair)
                val isExpanded = expandedIndex.value == index
                
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedIndex.value = if (isExpanded) -1 else index }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(question, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, 
                                null, 
                                tint = if (isExpanded) Color(0xFFFF6D00) else Color.Gray
                            )
                        }
                        
                        if (isExpanded) {
                            Text(
                                text = answer,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                            )
                        }
                    }
                }
            }
 
            Spacer(Modifier.height(24.dp))
 
            // Call / Email buttons
            val context = androidx.compose.ui.platform.LocalContext.current
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { 
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:+919876543210")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Phone, null, tint = Color(0xFFFF6D00))
                    Spacer(Modifier.width(8.dp))
                    Text("Call Support", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { 
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:admin@floatingflavors.com")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Support Request - Delivery Partner")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Email, null, tint = Color(0xFFFF6D00))
                    Spacer(Modifier.width(8.dp))
                    Text("Email Us", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            
            // Bottom Navigation Spacer
            Spacer(Modifier.height(84.dp))
        }
    }
}

@Composable
fun CategoryRow(rowCategories: List<Pair<String, ImageVector>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (category in rowCategories) {
            Box(modifier = Modifier.weight(1f)) {
                CategoryCard(category.first, category.second)
            }
        }
        if (rowCategories.size < 2) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun CategoryCard(title: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = if(icon == Icons.Default.MedicalServices) Color(0xFFD32F2F) else Color(0xFF1976D2), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
