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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventMenuScreen(
    bookingId: Int,
    onBack: () -> Unit= {}, //DEFAULT VALUE
    vm: EventMenuViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }

    var showSmartFilter by remember { mutableStateOf(false) }
    var smartFilterState by remember { mutableStateOf(SmartFilterState()) }
    var showReviewSheet by remember { mutableStateOf(false) }


    LaunchedEffect(bookingId) {
        vm.loadMenuWithRestore(bookingId)
    }


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
                        },
                        onSmartFilterClick = {
                            showSmartFilter = true
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

                    val menuId = item.idAsInt ?: return@items
                    val isChecked = state.selected.containsKey(menuId)
                    val quantity = state.selected[menuId] ?: 0

                    MenuItemCard(
                        item = item,
                        isChecked = isChecked,
                        quantity = quantity,
                        onCheckedChange = {
                            vm.toggleItem(menuId)
                        },
                        onAdd = {
                            vm.addItem(menuId)
                        },
                        onRemove = {
                            vm.removeItem(menuId)
                        }
                    )
                }
            }
        }

        if (state.totalAmount > 0) {
            FloatingReviewBar(
                total = state.totalAmount,
                itemCount = state.selected.values.sum(),
                onClick = {
                    vm.saveBookingMenu(bookingId)
                    showReviewSheet = true    // 👈 THIS opens review
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showSmartFilter) {
        ModalBottomSheet(
            onDismissRequest = { showSmartFilter = false },
            dragHandle = null,
            containerColor = Color.Transparent
        ) {
        SmartFilterBottomSheet(
                initialState = smartFilterState,
                onDismiss = { showSmartFilter = false },
                onApply = { state ->
                    smartFilterState = state
                    showSmartFilter = false
                    vm.applySmartFilter(state)   // 🔥 THIS LINE WAS MISSING
                }
            )
        }
    }

    if (showReviewSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReviewSheet = false }
        ) {
            ReviewSelectionBottomSheet(
                bookingId = bookingId,
                onConfirm = {
//                    vm.confirmBooking(bookingId)
                    showReviewSheet = false
                }
            )
        }
    }



}

/* ---------------- SIMPLE HEADER (OUR MENU STYLE) ---------------- */

@Composable
private fun HeaderSection(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        IconButton(
            onClick = onBack,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select Menu for Booking",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Choose dishes for your event / contract",
            fontSize = 13.sp,
            color = Color.Gray
        )
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
    val primaryGreen = Color(0xFF13EC5B)

    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(
                if (selected) primaryGreen else Color(0xFFE5E7EB),
                RoundedCornerShape(50)
            )
            .clickable { onSelect(label) }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.Black else Color.DarkGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
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
    onToggle: (String) -> Unit,
    onSmartFilterClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("AI SUGGESTED INGREDIENTS", fontSize = 12.sp, color = Color.Gray)
            Text(
                "Smart Filter",
                color = Purple,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onSmartFilterClick() }
            )
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
    val primaryGreen = Color(0xFF13EC5B)

    Box(
        modifier = Modifier
            .background(
                if (selected) primaryGreen else Color(0xFFE5E7EB),
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.Black else Color.DarkGray
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
    isChecked: Boolean,
    quantity: Int,
    onCheckedChange: () -> Unit,
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

            // 🔹 CHECKBOX
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onCheckedChange() }
            )

            Spacer(Modifier.width(8.dp))

            AsyncImage(
                model = item.image_full,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name ?: "", fontWeight = FontWeight.Bold)
                Text("₹${item.price}", color = Color(0xFF16A34A))
            }

            // 🔹 Quantity only if checkbox selected
            if (isChecked) {
                QuantityControl(quantity, onAdd, onRemove)
            }
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
