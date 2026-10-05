package com.example.autoconnect.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.theme.EmergencyRed
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TowingRequestScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val bamakoDistricts = listOf(
        "Badalabougou", "Hamdallaye ACI 2000", "Kalaban Coro", "Faladié",
        "Sébénikoro", "Lafiabougou", "Route de Ségou", "Mopti / National 6"
    )

    var selectedDistrict by remember { mutableStateOf("Badalabougou") }
    var vehicleModel by remember { mutableStateOf("Toyota Corolla 2015") }
    var issueType by remember { mutableStateOf("Panne Moteur / Surchauffe") }
    var userPhone by remember { mutableStateOf("+223 76 12 34 56") }

    var isDispatching by remember { mutableStateOf(false) }
    var dispatchStep by remember { mutableIntStateOf(0) } // 0: Idle, 1: Searching, 2: Driver Assigned, 3: En route

    LaunchedEffect(isDispatching) {
        if (isDispatching) {
            dispatchStep = 1
            delay(2000)
            dispatchStep = 2
            delay(2500)
            dispatchStep = 3
        }
    }

    val issueTypes = listOf("Panne Moteur / Surchauffe", "Accident / Impact", "Crevaison / Pas de roue de secours", "Transmission / Boîte bloquée", "Batterie HS")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dépannage & Remorquage Express 24h/7j", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmergencyRed)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emergency Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = EmergencyRed,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Service d'Urgence Remorquage Bamako", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Assistance plateau & dépanneuse géolocalisée 24h/24", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }

            if (!isDispatching) {
                // Location Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmergencyRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lieu de la panne (Quartier / Zone)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(bamakoDistricts) { district ->
                                    FilterChip(
                                        selected = selectedDistrict == district,
                                        onClick = { selectedDistrict = district },
                                        label = { Text(district, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmergencyRed,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Vehicle & Problem Details
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Détails du véhicule & Problème", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = vehicleModel,
                                onValueChange = { vehicleModel = it },
                                label = { Text("Marque & Modèle du véhicule") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Type d'incident :", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                issueTypes.forEach { type ->
                                    Surface(
                                        color = if (issueType == type) EmergencyRed.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { issueType = type }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (issueType == type) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (issueType == type) EmergencyRed else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = type,
                                                fontSize = 12.sp,
                                                fontWeight = if (issueType == type) FontWeight.Bold else FontWeight.Normal,
                                                color = if (issueType == type) EmergencyRed else Color(0xFF334155)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = userPhone,
                                onValueChange = { userPhone = it },
                                label = { Text("Votre numéro pour le chauffeur") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // Dispatch Action Button
                item {
                    Button(
                        onClick = { isDispatching = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DEMANDER UNE DÉPANNEUSE EN URGENCE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } else {
                // Live Dispatch Tracker Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (dispatchStep < 3) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = EmergencyRed
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            when (dispatchStep) {
                                1 -> {
                                    Text("Localisation de la dépanneuse la plus proche...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Zone recherchée : $selectedDistrict", fontSize = 12.sp, color = Color.Gray)
                                }
                                2 -> {
                                    Text("Chauffeur trouvé ! Attribution en cours...", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Chauffeur : Ousmane Traoré (Plateau Remorquage Mali)", fontSize = 12.sp)
                                }
                                3 -> {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("DÉPANNEUSE EN ROUTE EN EXCLUSIVITÉ", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Arrivée estimée : 12 - 15 minutes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmergencyRed)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Fiche du Chauffeur Dépanneur :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("• Nom : Ousmane Traoré", fontSize = 12.sp)
                                            Text("• Camion Plateau : Mercedes Atego (+223 76 99 88 11)", fontSize = 12.sp)
                                            Text("• Lieu de départ : Badalabougou Pont", fontSize = 12.sp)
                                            Text("• Destination : $selectedDistrict", fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+22376998811"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Appeler Chauffeur", fontSize = 11.sp, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                val msg = "Bonjour, je suis arrêté à $selectedDistrict avec mon véhicule $vehicleModel ($issueType). Pouvez-vous me rejoindre ?"
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/22376998811?text=${Uri.encode(msg)}"))
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "WhatsApp non installé", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("WhatsApp Position", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Central Hotline Call Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Standard Téléphonique Dépannage Mali", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("+223 80 00 22 44 (Appel Direct Gratuit)", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+22380002244"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.background(EmergencyRed, CircleShape)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
