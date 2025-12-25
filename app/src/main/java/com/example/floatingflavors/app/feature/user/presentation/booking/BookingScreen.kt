package com.example.floatingflavors.app.feature.user.presentation.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.data.booking.dto.BookingRequest
import com.example.floatingflavors.app.feature.user.presentation.booking.components.CompanyContractDetails
import com.example.floatingflavors.app.feature.user.presentation.booking.components.EventBookingDetails
import com.example.floatingflavors.app.feature.user.presentation.booking.components.ToggleCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign

@Composable
fun BookingScreen(
    vm: BookingViewModel,
    userId: Int,
    onNavigateToMenu: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    // Get screen configuration for responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Check for existing booking on screen load
    LaunchedEffect(userId) {
        vm.checkUserBookingStatus(userId)
    }

    // If user has active booking, show status screen
    when (val state = vm.bookingStatus) {

        BookingViewModel.BookingState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is BookingViewModel.BookingState.Active -> {
            ResponsiveBookingStatusUI(
                bookingState = state,
                userId = userId,
                onNavigateToMenu = onNavigateToMenu,
                onBackToBooking = { vm.clearBooking() },
                onShowMessage = onShowMessage,
                vm = vm,
                screenWidth = screenWidth
            )
        }

        BookingViewModel.BookingState.None -> {
            ResponsiveBookingFormScreen(
                vm = vm,
                userId = userId,
                onShowMessage = onShowMessage,
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
fun ResponsiveBookingStatusUI(
    bookingState: BookingViewModel.BookingState.Active,
    userId: Int,
    onNavigateToMenu: (String) -> Unit,
    onBackToBooking: () -> Unit,
    onShowMessage: (String) -> Unit,
    vm: BookingViewModel,
    screenWidth: Dp
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(bookingState) {
        when (bookingState.status) {
            "CONFIRMED" -> {
                // Navigate to menu after a short delay
                coroutineScope.launch {
                    delay(2000)
                    onNavigateToMenu(bookingState.bookingId)
                }
            }
            "CANCELLED" -> {
                onShowMessage("Sorry! This booking can't be accepted for the selected time/date.")
                coroutineScope.launch {
                    delay(3000)
                    onBackToBooking()
                }
            }
            else -> {}
        }
    }

    val padding = when {
        screenWidth < 360.dp -> 12.dp  // Small phones
        screenWidth < 600.dp -> 16.dp  // Medium phones
        else -> 24.dp                   // Tablets/large screens
    }

    val iconSize = when {
        screenWidth < 360.dp -> 48.dp
        screenWidth < 600.dp -> 56.dp
        else -> 64.dp
    }

    val fontSizeTitle = when {
        screenWidth < 360.dp -> 22.sp
        screenWidth < 600.dp -> 24.sp
        else -> 28.sp
    }

    val fontSizeBody = when {
        screenWidth < 360.dp -> 12.sp
        screenWidth < 600.dp -> 14.sp
        else -> 16.sp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(padding * 1.5f)
    ) {
        val statusColor = when (bookingState.status) {
            "PENDING" -> Color(0xFFF59E0B)
            "CONFIRMED" -> Color(0xFF16A34A)
            "CANCELLED" -> Color(0xFFDC2626)
            else -> Color.Gray
        }

        val statusIcon = when (bookingState.status) {
            "PENDING" -> Icons.Filled.HourglassEmpty
            "CONFIRMED" -> Icons.Filled.CheckCircle
            "CANCELLED" -> Icons.Filled.Cancel
            else -> Icons.Filled.Help
        }

        val statusMessage = when (bookingState.status) {
            "PENDING" -> "Your booking is pending admin approval."
            "CONFIRMED" -> "Your booking has been confirmed!"
            "CANCELLED" -> "This booking was cancelled by admin."
            else -> "Unknown status"
        }

        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = "Status",
                tint = statusColor,
                modifier = Modifier.size(iconSize)
            )

            Text(
                text = bookingState.status,
                fontSize = fontSizeTitle,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = statusMessage,
                fontSize = fontSizeBody,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = fontSizeBody * 1.2f
            )
        }

        // Booking Details Card - Responsive
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (screenWidth > 600.dp) 48.dp else 0.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (screenWidth > 600.dp) 8.dp else 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(padding * 1.25f),
                verticalArrangement = Arrangement.spacedBy(padding)
            ) {
                Text(
                    text = "Booking Details",
                    fontSize = if (screenWidth < 360.dp) 16.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Divider()

                // Booking ID
                ResponsiveDetailRow(label = "Booking ID", value = bookingState.bookingId, screenWidth = screenWidth)

                // Booking Type
                ResponsiveDetailRow(label = "Type", value = bookingState.bookingType, screenWidth = screenWidth)

                // Event/Company specific details
                if (bookingState.bookingType == "EVENT") {
                    bookingState.eventName?.let {
                        ResponsiveDetailRow(label = "Event Name", value = it, screenWidth = screenWidth)
                    }
                    ResponsiveDetailRow(label = "Status", value = bookingState.status, screenWidth = screenWidth)
                    bookingState.dateTime?.let {
                        ResponsiveDetailRow(label = "Date", value = it, screenWidth = screenWidth)
                    }
                } else {
                    bookingState.companyName?.let {
                        ResponsiveDetailRow(label = "Company", value = it, screenWidth = screenWidth)
                    }
                    ResponsiveDetailRow(label = "Status", value = bookingState.status, screenWidth = screenWidth)
                }

                Divider()

                Text(
                    text = "Note: You'll be automatically redirected to menu selection once booking is confirmed.",
                    fontSize = if (screenWidth < 360.dp) 10.sp else 12.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = if (screenWidth < 360.dp) 12.sp else 14.sp
                )
            }
        }

        // Actions based on status
        when (bookingState.status) {
            "PENDING" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (screenWidth > 600.dp) 48.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(padding)
                ) {
                    OutlinedButton(
                        onClick = {
                            vm.cancelBooking(bookingState.bookingId)
                            onBackToBooking()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Filled.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Booking")
                    }

                    Button(
                        onClick = { vm.checkUserBookingStatus(userId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check Status")
                    }
                }
            }
            "CONFIRMED" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (screenWidth > 600.dp) 48.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(if (screenWidth < 360.dp) 24.dp else 32.dp),
                        color = Color.Green
                    )

                    Text(
                        text = "Redirecting to menu selection...",
                        fontSize = fontSizeBody,
                        color = Color.Gray
                    )
                }
            }
            "CANCELLED" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (screenWidth > 600.dp) 48.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sorry, this booking cannot be accepted.\nPlease try different timing or contact support.",
                        fontSize = fontSizeBody,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        lineHeight = fontSizeBody * 1.3f
                    )

                    Button(
                        onClick = onBackToBooking,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Replay, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Booking")
                    }
                }
            }
        }
    }
}

@Composable
fun ResponsiveDetailRow(label: String, value: String, screenWidth: Dp) {
    val fontSize = when {
        screenWidth < 360.dp -> 12.sp
        screenWidth < 600.dp -> 13.sp
        else -> 14.sp
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ResponsiveBookingFormScreen(
    vm: BookingViewModel,
    userId: Int,
    onShowMessage: (String) -> Unit,
    screenWidth: Dp
) {
    var selectedTab by remember { mutableStateOf("EVENT") }

    // Event state
    var selectedEventType by remember { mutableStateOf("Birthday") }
    var eventName by remember { mutableStateOf("") }
    var peopleCount by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventTime by remember { mutableStateOf("") }

    // Company state
    var companyName by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var employeeCount by remember { mutableStateOf("") }
    var contractDuration by remember { mutableStateOf("") }
    var serviceFrequency by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Responsive values
    val horizontalPadding = when {
        screenWidth < 360.dp -> 12.dp
        screenWidth < 600.dp -> 16.dp
        else -> 20.dp
    }

    val verticalPadding = when {
        screenWidth < 360.dp -> 16.dp
        screenWidth < 600.dp -> 20.dp
        else -> 24.dp
    }

    val headerFontSize = when {
        screenWidth < 360.dp -> 20.sp
        screenWidth < 600.dp -> 22.sp
        else -> 24.sp
    }

    val bodyFontSize = when {
        screenWidth < 360.dp -> 12.sp
        screenWidth < 600.dp -> 13.sp
        else -> 14.sp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
    ) {

        /* ---------------- RESPONSIVE HEADER ---------------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        bottomStart = if (screenWidth > 600.dp) 32.dp else 24.dp,
                        bottomEnd = if (screenWidth > 600.dp) 32.dp else 24.dp
                    )
                )
                .background(Color.Transparent)
                .padding(verticalPadding)
                .padding(horizontal = horizontalPadding)
        ) {
            Column {
                Text(
                    text = "Event Booking",
                    fontSize = headerFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Plan your perfect celebration",
                    fontSize = bodyFontSize,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        /* ---------------- RESPONSIVE TOGGLE CARD SECTION ---------------- */
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            shape = RoundedCornerShape(if (screenWidth > 600.dp) 20.dp else 16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier.padding(horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToggleCard(
                    title = "Event Booking",
                    subtitle = "Parties & Celebrations",
                    icon = Icons.Filled.Celebration,
                    selected = selectedTab == "EVENT",
                    onClick = { selectedTab = "EVENT" }
                )

                ToggleCard(
                    title = "Company Contract",
                    subtitle = "Corporate Catering",
                    icon = Icons.Filled.Apartment,
                    selected = selectedTab == "COMPANY",
                    onClick = { selectedTab = "COMPANY" }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        /* ---------------- RESPONSIVE DETAILS CARD ---------------- */
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            shape = RoundedCornerShape(if (screenWidth > 600.dp) 24.dp else 20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(verticalPadding)) {

                // Show error if any
                vm.bookingError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = bodyFontSize,
                        modifier = Modifier.padding(horizontal = verticalPadding, vertical = 8.dp)
                    )
                }

                if (selectedTab == "EVENT") {
                    Column(modifier = Modifier.padding(horizontal = verticalPadding)) {
                        EventBookingDetails(
                            selectedEventType = selectedEventType,
                            onEventTypeChange = { selectedEventType = it },
                            eventName = eventName,
                            onEventNameChange = { eventName = it },
                            peopleCount = peopleCount,
                            onPeopleCountChange = { peopleCount = it },
                            eventDate = eventDate,
                            onEventDateChange = { eventDate = it },
                            eventTime = eventTime,
                            onEventTimeChange = { eventTime = it },
                            onSubmit = {
                                // Validation
                                if (eventName.isBlank()) {
                                    onShowMessage("Please enter event name")
                                    return@EventBookingDetails
                                }
                                if (peopleCount.isBlank() || peopleCount.toIntOrNull() == null) {
                                    onShowMessage("Please enter valid number of people")
                                    return@EventBookingDetails
                                }
                                if (eventDate.isBlank()) {
                                    onShowMessage("Please select event date")
                                    return@EventBookingDetails
                                }
                                if (eventTime.isBlank()) {
                                    onShowMessage("Please select event time")
                                    return@EventBookingDetails
                                }

                                vm.submitBooking(
                                    BookingRequest(
                                        userId = userId,
                                        bookingType = "EVENT",
                                        eventType = selectedEventType,
                                        eventName = eventName,
                                        peopleCount = peopleCount.toInt(),
                                        eventDate = eventDate,
                                        eventTime = eventTime
                                    ),
                                    userId = userId
                                )
                                onShowMessage("Booking submitted! Waiting for admin approval.")
                            },
                            isLoading = vm.isLoading
                        )
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = verticalPadding)) {
                        CompanyContractDetails(
                            companyName = companyName,
                            onCompanyNameChange = { companyName = it },
                            contactPerson = contactPerson,
                            onContactPersonChange = { contactPerson = it },
                            employeeCount = employeeCount,
                            onEmployeeCountChange = { employeeCount = it },
                            contractDuration = contractDuration,
                            onContractDurationChange = { contractDuration = it },
                            serviceFrequency = serviceFrequency,
                            onServiceFrequencyChange = { serviceFrequency = it },
                            notes = notes,
                            onNotesChange = { notes = it },
                            onSubmit = {
                                // Validation
                                if (companyName.isBlank()) {
                                    onShowMessage("Please enter company name")
                                    return@CompanyContractDetails
                                }
                                if (contactPerson.isBlank()) {
                                    onShowMessage("Please enter contact person")
                                    return@CompanyContractDetails
                                }
                                if (employeeCount.isBlank() || employeeCount.toIntOrNull() == null) {
                                    onShowMessage("Please enter valid employee count")
                                    return@CompanyContractDetails
                                }

                                vm.submitBooking(
                                    BookingRequest(
                                        userId = userId,
                                        bookingType = "COMPANY",
                                        companyName = companyName,
                                        contactPerson = contactPerson,
                                        employeeCount = employeeCount.toInt(),
                                        contractDuration = contractDuration,
                                        serviceFrequency = serviceFrequency,
                                        notes = notes
                                    ),
                                    userId = userId
                                )
                                onShowMessage("Contract request submitted! Waiting for admin approval.")
                            },
                            isLoading = vm.isLoading
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}




//package com.example.floatingflavors.app.feature.user.presentation.booking
//
//import androidx.compose.foundation.*
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.outlined.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun BookingScreen() {
//
//    var selectedTab by remember { mutableStateOf("EVENT") }
//    var selectedEventType by remember { mutableStateOf("Birthday") }
//    var eventName by remember { mutableStateOf("") }
//    var peopleCount by remember { mutableStateOf("50") }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF9FAFB))
//            .verticalScroll(rememberScrollState())
//            .padding(horizontal = 16.dp)
//    ) {
//
//        Spacer(Modifier.height(12.dp))
//
//        /* ---------------- HEADER (HOME STYLE – NO BG) ---------------- */
//
//        Text(
//            text = "Event Booking",
//            fontSize = 22.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.Black
//        )
//
//        Text(
//            text = "Plan your perfect celebration",
//            fontSize = 13.sp,
//            color = Color.Gray
//        )
//
//        Spacer(Modifier.height(20.dp))
//
//        /* ---------------- EVENT / COMPANY TOGGLE ---------------- */
//
//        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//
//            ToggleCard(
//                title = "Event Booking",
//                subtitle = "Parties & Celebrations",
//                icon = Icons.Outlined.Celebration,
//                selected = selectedTab == "EVENT"
//            ) { selectedTab = "EVENT" }
//
//            ToggleCard(
//                title = "Company Contract",
//                subtitle = "Corporate Catering",
//                icon = Icons.Outlined.Apartment,
//                selected = selectedTab == "COMPANY"
//            ) { selectedTab = "COMPANY" }
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        /* ---------------- EVENT DETAILS ---------------- */
//
//        Text(
//            text = "Event Details",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold
//        )
//
//        Text(
//            text = "Tell us about your event",
//            fontSize = 12.sp,
//            color = Color.Gray
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        Text("Event Type", fontWeight = FontWeight.SemiBold)
//
//        Spacer(Modifier.height(12.dp))
//
//        EventTypeGrid(
//            selected = selectedEventType,
//            onSelect = { selectedEventType = it }
//        )
//
//        Spacer(Modifier.height(20.dp))
//
//        OutlinedTextField(
//            value = eventName,
//            onValueChange = { eventName = it },
//            placeholder = { Text("e.g., John's Birthday Party") },
//            label = { Text("Event Name") },
//            shape = RoundedCornerShape(14.dp),
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(Modifier.height(14.dp))
//
//        OutlinedTextField(
//            value = peopleCount,
//            onValueChange = { peopleCount = it },
//            leadingIcon = { Icon(Icons.Outlined.Group, null) },
//            label = { Text("Number of People") },
//            shape = RoundedCornerShape(14.dp),
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(Modifier.height(14.dp))
//
//        ReadOnlyInput(
//            label = "Event Date",
//            icon = Icons.Outlined.CalendarToday,
//            value = "Select date"
//        )
//
//        Spacer(Modifier.height(14.dp))
//
//        ReadOnlyInput(
//            label = "Event Time",
//            icon = Icons.Outlined.Schedule,
//            value = "--:--"
//        )
//
//        Spacer(Modifier.height(26.dp))
//
//        Button(
//            onClick = { /* later */ },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            shape = RoundedCornerShape(16.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFF9333EA)
//            )
//        ) {
//            Text("Select Menu Items", fontSize = 16.sp)
//            Spacer(Modifier.width(8.dp))
//            Icon(Icons.Outlined.ArrowForward, null)
//        }
//
//        Spacer(Modifier.height(24.dp))
//    }
//}
//
///* ---------------- COMPONENTS ---------------- */
//
//@Composable
//private fun ToggleCard(
//    title: String,
//    subtitle: String,
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    selected: Boolean,
//    onClick: () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .clip(RoundedCornerShape(18.dp))
//            .border(
//                if (selected) 2.dp else 1.dp,
//                if (selected) Color(0xFF9333EA) else Color(0xFFE5E7EB),
//                RoundedCornerShape(18.dp)
//            )
//            .background(if (selected) Color(0xFFF3E8FF) else Color.White)
//            .clickable { onClick() }
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Icon(
//            icon,
//            null,
//            tint = if (selected) Color(0xFF9333EA) else Color.Gray,
//            modifier = Modifier.size(32.dp)
//        )
//        Spacer(Modifier.height(8.dp))
//        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
//        Text(subtitle, fontSize = 10.sp, color = Color.Gray)
//    }
//}
//
//@Composable
//private fun EventTypeGrid(
//    selected: String,
//    onSelect: (String) -> Unit
//) {
//    val items = listOf(
//        "Birthday" to "🎂",
//        "Wedding" to "💒",
//        "Corporate" to "🏢",
//        "Anniversary" to "💖",
//        "Other" to "🎉"
//    )
//
//    Column {
//        items.chunked(3).forEach { row ->
//            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                row.forEach { (title, emoji) ->
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(92.dp)
//                            .clip(RoundedCornerShape(16.dp))
//                            .border(
//                                1.dp,
//                                if (selected == title) Color(0xFF9333EA)
//                                else Color(0xFFE5E7EB),
//                                RoundedCornerShape(16.dp)
//                            )
//                            .clickable { onSelect(title) }
//                            .background(Color.White),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Text(emoji, fontSize = 22.sp)
//                            Spacer(Modifier.height(4.dp))
//                            Text(title, fontSize = 10.sp)
//                        }
//                    }
//                }
//                if (row.size < 3) Spacer(Modifier.weight(1f))
//            }
//            Spacer(Modifier.height(12.dp))
//        }
//    }
//}
//
//@Composable
//private fun ReadOnlyInput(
//    label: String,
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    value: String
//) {
//    OutlinedTextField(
//        value = value,
//        onValueChange = {},
//        readOnly = true,
//        leadingIcon = { Icon(icon, null) },
//        label = { Text(label) },
//        shape = RoundedCornerShape(14.dp),
//        modifier = Modifier.fillMaxWidth()
//    )
//}
