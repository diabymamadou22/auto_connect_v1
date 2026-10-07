package com.example.autoconnect.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.local.ChatMessageEntity
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    providerId: String,
    servicesViewModel: ServicesViewModel,
    onNavigateToBookings: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val allProviders by servicesViewModel.allServices.collectAsState()
    val allMessages by servicesViewModel.allChatMessages.collectAsState()

    val provider = allProviders.find { it.id == providerId }
    val providerName = provider?.name ?: "Garage Mécanique"
    val providerPhone = provider?.phone ?: "+223 76 12 34 56"

    val providerMessages = allMessages
        .filter { it.providerId == providerId }
        .sortedBy { it.timestamp }

    var inputText by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Demande de devis estimatif",
        "Bruit suspect au freinage",
        "Voyant moteur orange allumé",
        "Disponibilité pour révision aujourd'hui ?"
    )

    // Auto scroll to bottom when messages update
    LaunchedEffect(providerMessages.size) {
        if (providerMessages.isNotEmpty()) {
            listState.animateScrollToItem(providerMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = providerName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = providerName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF4ADE80),
                                    shape = CircleShape,
                                    modifier = Modifier.size(6.dp)
                                ) { }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "En ligne • Réponse rapide",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$providerPhone"))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Call, contentDescription = "Appeler", tint = Color.White)
                    }
                    IconButton(onClick = {
                        servicesViewModel.clearChatHistory(providerId)
                        Toast.makeText(context, "Discussion réinitialisée", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Effacer", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
        ) {
            val isWide = maxWidth >= 600.dp
            val contentModifier = if (isWide) {
                Modifier
                    .widthIn(max = 760.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(
                modifier = contentModifier.fillMaxSize()
            ) {
            // Quick Questions Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickQuestions) { question ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            servicesViewModel.sendChatMessage(
                                providerId = providerId,
                                providerName = providerName,
                                userMessageText = question,
                                isQuoteRequest = question.contains("devis", ignoreCase = true)
                            )
                        },
                        label = { Text(question, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = BluePrimary
                        )
                    )
                }
            }

            // Messages Feed
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(providerMessages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        onAcceptQuote = {
                            onNavigateToBookings()
                            Toast.makeText(context, "Devis accepté ! Complétez l'heure du rendez-vous.", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }

            // Bottom Input Bar
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            servicesViewModel.sendChatMessage(
                                providerId = providerId,
                                providerName = providerName,
                                userMessageText = "📷 [Photo Diagnostic envoyée] Pouvez-vous évaluer ce bruit / cette fuite ?",
                                isQuoteRequest = true
                            )
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Joindre photo", tint = BluePrimary)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Posez votre question ou devis...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                servicesViewModel.sendChatMessage(
                                    providerId = providerId,
                                    providerName = providerName,
                                    userMessageText = inputText,
                                    isQuoteRequest = inputText.contains("devis", ignoreCase = true)
                                )
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .background(BluePrimary, CircleShape)
                            .size(42.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
}

@Composable
fun ChatBubbleItem(
    message: ChatMessageEntity,
    onAcceptQuote: () -> Unit
) {
    val isUser = message.sender == "user"
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) BluePrimary else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            shadowElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier.width(280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Text(
                        text = message.providerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = BluePrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.message,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else Color(0xFF1E293B)
                )

                // Quote Card Embed
                if (message.isQuote && message.quoteAmount.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RequestQuote, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ESTIMATION DEVIS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF16A34A))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.quoteAmount,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF15803D)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAcceptQuote,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Réserver ce Tarif en RDV", fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 9.sp,
                        color = if (isUser) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
