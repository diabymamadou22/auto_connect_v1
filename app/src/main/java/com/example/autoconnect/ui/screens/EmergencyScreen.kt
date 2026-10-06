package com.example.autoconnect.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.ui.theme.EmergencyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedBreakdownType by remember { mutableStateOf("Moteur") }
    var locationNote by remember { mutableStateOf("Bamako, Pont Fahd (GPS: 12.6392, -8.0029)") }
    var showSOSDialog by remember { mutableStateOf(false) }
    var sosSent by remember { mutableStateOf(false) }

    val emergencyContacts = listOf(
        EmergencyContact("Sapeurs Pompiers Mali", "18", EmergencyRed),
        EmergencyContact("Police Secours", "17", Color(0xFF1976D2)),
        EmergencyContact("SAMU / Urgences", "15", Color(0xFF388E3C)),
        EmergencyContact("Dépannage Auto Bamako 24/7", "+223 76 00 11 22", Color(0xFFFF8F00))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("URGENCE SOS DÉPANNAGE", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmergencyRed)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // High Priority Emergency Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(EmergencyRed, Color(0xFFB71C1C))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ASSISTANCE IMMÉDIATE",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "En cas de panne ou d'accident, votre position GPS est partagée avec les dépanneurs les plus proches.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Breakdown Type Selector
            Text("1. Type de panne", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BreakdownOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "Moteur",
                    icon = Icons.Default.Engineering,
                    isSelected = selectedBreakdownType == "Moteur",
                    onSelect = { selectedBreakdownType = "Moteur" }
                )
                BreakdownOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "Pneu",
                    icon = Icons.Default.TireRepair,
                    isSelected = selectedBreakdownType == "Pneu",
                    onSelect = { selectedBreakdownType = "Pneu" }
                )
                BreakdownOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "Batterie",
                    icon = Icons.Default.BatteryAlert,
                    isSelected = selectedBreakdownType == "Batterie",
                    onSelect = { selectedBreakdownType = "Batterie" }
                )
                BreakdownOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "Accident",
                    icon = Icons.Default.CarCrash,
                    isSelected = selectedBreakdownType == "Accident",
                    onSelect = { selectedBreakdownType = "Accident" }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Location Input
            Text("2. Votre position actuelle", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = locationNote,
                onValueChange = { locationNote = it },
                label = { Text("Lieu / Repère GPS") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main SOS Signal Transmit Button
            Button(
                onClick = { showSOSDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRANSMETTRE SIGNAL SOS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            AnimatedVisibility(visible = sosSent) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alerte SOS transmise !", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Text("3 dépanneurs à proximité ont reçu votre position GPS.", fontSize = 12.sp, color = Color(0xFF1B5E20))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Emergency Call Directory Section
            Text("Numéros d'urgence directe", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            emergencyContacts.forEach { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                            context.startActivity(intent)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = contact.color.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = contact.color)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(contact.phone, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = contact.color
                        ) {
                            Text(
                                text = "APPELER",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Alert Dialog
    if (showSOSDialog) {
        AlertDialog(
            onDismissRequest = { showSOSDialog = false },
            title = { Text("Confirmer le signal SOS", fontWeight = FontWeight.Bold) },
            text = {
                Text("Voulez-vous envoyer une alerte d'urgence pour une panne de type '$selectedBreakdownType' à la position '$locationNote' ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSOSDialog = false
                        sosSent = true
                        Toast.makeText(context, "Signal SOS envoyé avec succès !", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Oui, Envoyer SOS", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSOSDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun BreakdownOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 2.dp,
                color = if (isSelected) EmergencyRed else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EmergencyRed.copy(alpha = 0.1f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) EmergencyRed else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) EmergencyRed else Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class EmergencyContact(
    val title: String,
    val phone: String,
    val color: Color
)
