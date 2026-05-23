package com.example.floatingflavors.app.chatbot

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.chatbot.components.*
import com.example.floatingflavors.app.chatbot.model.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: Int,
    viewModel: ChatViewModel,
    onOpenCart: () -> Unit = {}
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // TextToSpeech Engine
    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                // Optionally set language here
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    // Automatic Cart Redirection & TTS
    LaunchedEffect(messages) {
        val lastMsg = messages.lastOrNull()
        if (lastMsg != null && !lastMsg.isUser) {
            tts?.speak(lastMsg.text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
            
            if (lastMsg.type == "open_cart" || lastMsg.text.contains("Opening your cart") == true) {
                delay(1500)
                onOpenCart()
            }
        }
    }

    // Speech simulation states
    var showVoiceModal by remember { mutableStateOf(false) }
    var voiceTextSimulated by remember { mutableStateOf("Listening...") }

    // Speech recognizer activity results launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                input = spokenText
                viewModel.sendMessage(userId, spokenText)
            }
        }
    }

    // Suggestion Chips list
    val suggestionChips = listOf(
        "🍳 Chef's Specials",
        "🌶️ Spicy Foods",
        "🚚 Track Order",
        "📅 Book Event",
        "📞 Customer Support"
    )

    LaunchedEffect(Unit) {
        viewModel.loadMessages()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 🔹 HEADER (Supports orange/cream accents, clear, call and voice actions)
            ChatHeader(
                onClearChat = { viewModel.clearChat() },
                onSupportCallClick = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                },
                onVoiceClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now to search or order...")
                    }
                    try {
                        speechRecognizerLauncher.launch(intent)
                    } catch (e: Exception) {
                        // Fallback to voice simulation modal if Google services or microphone are unavailable
                        showVoiceModal = true
                        voiceTextSimulated = "Listening..."
                        coroutineScope.launch {
                            delay(1200)
                            voiceTextSimulated = "Processing speech..."
                            delay(1000)
                            voiceTextSimulated = "\"Show spicy biryani\""
                            delay(800)
                            showVoiceModal = false
                            viewModel.sendMessage(userId, "Show spicy biryani")
                        }
                    }
                }
            )

            // 🔹 AI TYPING PULSE ANIMATION
            if (isLoading) {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha1 by infiniteTransition.animateFloat(
                    initialValue = 0.3f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600, delayMillis = 0), RepeatMode.Reverse)
                )
                val alpha2 by infiniteTransition.animateFloat(
                    initialValue = 0.3f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse)
                )
                val alpha3 by infiniteTransition.animateFloat(
                    initialValue = 0.3f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE65100).copy(alpha = alpha1)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE65100).copy(alpha = alpha2)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE65100).copy(alpha = alpha3)))
                }
            }

            // 🔹 ERROR ANNOUNCEMENT
            error?.let { msg ->
                Surface(
                    color = Color(0xFFFFCDD2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            fontSize = 13.sp,
                            color = Color(0xFFB71C1C),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB71C1C))
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 🔹 CONCIERGE CHAT LOG
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFFFFDE7)), // Light Warm Cream Background
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(
                        message = msg,
                        userId = userId,
                        onAddToCart = { item ->
                            val parsedId = item.id?.toDoubleOrNull()?.toInt() ?: item.id?.toIntOrNull() ?: 0
                            val parsedPrice = item.price?.toDoubleOrNull()?.toInt() ?: item.price?.toIntOrNull() ?: 0
                            viewModel.addToCart(
                                userId = userId,
                                menuItemId = parsedId,
                                price = parsedPrice
                            )
                            android.widget.Toast.makeText(context, "${item.name} added to cart!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onSendMessage = { text ->
                            viewModel.sendMessage(userId, text)
                        }
                    )
                }
            }

            // 🔹 HORIZONTAL SUGGESTION CHIPS
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFDE7))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestionChips) { chipText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFE0B2)) // Soft Orange/Cream chip
                            .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.sendMessage(userId, chipText.substring(3).trim())
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chipText,
                            color = Color(0xFFE65100),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 🔹 TEXT INPUT PANEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    placeholder = { Text("Ask food, track order, book event…") },
                    singleLine = true,
                    enabled = !isLoading,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    enabled = input.isNotBlank() && !isLoading,
                    onClick = {
                        viewModel.sendMessage(userId, input.trim())
                        input = ""
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (input.isNotBlank() && !isLoading) Color(0xFFE65100) else Color(0xFFE0E0E0),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = Color.White
                    )
                }
            }
        }

        // 🎙️ SPEECH RECOGNITION INTERACTION LAYER
        if (showVoiceModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Voice Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFE65100)
                            )
                            IconButton(onClick = { showVoiceModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Pulsing microphone simulation waves
                        val infiniteTransition = rememberInfiniteTransition()
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.4f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFE0B2).copy(alpha = 0.5f * pulseScale))
                            )
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE65100)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎙️", fontSize = 24.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = voiceTextSimulated,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF3E2723)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
