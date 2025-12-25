package com.example.floatingflavors.app.feature.user.presentation.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.example.floatingflavors.app.feature.menu.data.remote.dto.idAsInt
import androidx.compose.foundation.layout.FlowRow

/* ---------------- COLORS (FIGMA TOKENS) ---------------- */

private val Purple = Color(0xFF9333EA)
private val Bg = Color(0xFFF3F4F6)
private val CardBg = Color.White
private val ChipBorderColor = Color(0xFFE5E7EB)

/* ---------------- MAIN SCREEN ---------------- */

@Composable
fun EventMenuScreen(
    bookingId: Int,
    onBack: () -> Unit= {}, //DEFAULT VALUE
    vm: EventMenuViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) { vm.loadMenu() }

    // 🔹 CATEGORY FILTER
    val filteredMenu = remember(state.menu, selectedCategory) {
        if (selectedCategory == "All") state.menu
        else state.menu.filter {
            it.category.equals(selectedCategory, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {

        Column {
            HeaderSection(
                onBack = onBack,
//                onNotification = { /* future */ }
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {

                item {
                    CategorySection(
                        selectedCategory = selectedCategory,
                        onSelect = { selectedCategory = it }
                    )
                }

                item { SearchBar() }

                item {
                    AiSuggestionSection(
                        selected = selectedIngredients,
                        onToggle = {
                            selectedIngredients =
                                if (selectedIngredients.contains(it))
                                    selectedIngredients - it
                                else
                                    selectedIngredients + it
                        }
                    )
                }

                item {
                    SectionTitle(
                        title = "Recommended Platters",
                        subtitle = "Bulk Pricing Applied"
                    )
                }

                items(filteredMenu) { item ->
                    MenuItemCard(
                        item = item,
                        quantity = state.selected[item.idAsInt ?: -1] ?: 0,
                        onAdd = { item.idAsInt?.let(vm::addItem) },
                        onRemove = { item.idAsInt?.let(vm::removeItem) }
                    )
                }
            }
        }

        if (state.totalAmount > 0) {
            FloatingReviewBar(
                total = state.totalAmount,
                itemCount = state.selected.values.sum(),
                onClick = {
                    // 🔥 NEXT STEP: open bottom sheet review
                    vm.saveBookingMenu(bookingId)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/* ---------------- HEADER ---------------- */

@Composable
private fun HeaderSection(
    onBack: () -> Unit,
//    onNotification: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(
                Purple,
                RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
//                IconButton(onClick = onNotification) {
//                    Icon(Icons.Default.Notifications, null, tint = Color.White)
//                }
            }

            Column {
                Text(
                    "Select Menu for Booking",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Choose dishes for your event / contract",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/* ---------------- CATEGORY ---------------- */

@Composable
private fun CategorySection(
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.padding(start = 20.dp, top = 12.dp)) {
        Text("Categories", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            CategoryChip("All", selectedCategory, onSelect)
            CategoryChip("Starters", selectedCategory, onSelect)
            CategoryChip("Main Course", selectedCategory, onSelect)
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    val selected = selectedCategory == label

    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(
                if (selected) Purple else CardBg,
                RoundedCornerShape(50)
            )
            .border(1.dp, ChipBorderColor, RoundedCornerShape(50))
            .clickable { onSelect(label) }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else Color.Black,
            fontSize = 13.sp
        )
    }
}

/* ---------------- SEARCH ---------------- */

@Composable
private fun SearchBar() {
    Box(
        modifier = Modifier
            .padding(20.dp)
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            "✨ Search dishes or ingredients (AI Essential)",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

/* ---------------- AI SUGGESTIONS ---------------- */

@Composable
private fun AiSuggestionSection(
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("AI SUGGESTED INGREDIENTS", fontSize = 12.sp, color = Color.Gray)
            Text("Smart Filter", color = Purple, fontSize = 12.sp)
        }

        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Spicy", "Chicken", "Vegetarian",
                "Basil", "Coconut Milk", "Gluten Free"
            ).forEach { label ->
                FilterChip(
                    label = label,
                    selected = selected.contains(label),
                    onClick = { onToggle(label) }
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Purple else CardBg,
                RoundedCornerShape(50)
            )
            .border(1.dp, ChipBorderColor, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = if (selected) Color.White else Color.Black
        )
    }
}

/* ---------------- SECTION TITLE ---------------- */

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Purple, fontSize = 12.sp)
    }
}

/* ---------------- MENU CARD ---------------- */

@Composable
private fun MenuItemCard(
    item: MenuItemDto,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = item.image_full,
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name ?: "", fontWeight = FontWeight.Bold)
                Text("₹${item.price}", color = Color(0xFF16A34A))
            }

            QuantityControl(quantity, onAdd, onRemove)
        }
    }
}

/* ---------------- QUANTITY ---------------- */

@Composable
private fun QuantityControl(
    qty: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onRemove, enabled = qty > 0) {
            Icon(Icons.Default.Remove, null)
        }
        Text(qty.toString(), fontWeight = FontWeight.Bold)
        IconButton(onClick = onAdd) {
            Icon(Icons.Default.Add, null)
        }
    }
}

/* ---------------- FLOATING BAR ---------------- */

@Composable
private fun FloatingReviewBar(
    total: Double,
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("$itemCount Platter Items", color = Color.LightGray, fontSize = 12.sp)
                Text(
                    "₹${"%.2f".format(total)}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Review Selection →", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
