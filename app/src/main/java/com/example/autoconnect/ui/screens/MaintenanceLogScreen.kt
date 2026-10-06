package com.example.autoconnect.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.ui.theme.BluePrimary

data class MaintenanceItem(
    val title: String,
    val intervalKm: Int,
    val lastDoneKm: Int,
    val recommendedProduct: String,
    val estimatedCostFcfa: String
)

data class ServiceRecord(
    val date: String,
    val serviceName: String,
    val garageName: String,
    val mileageKm: Int,
    val costFcfa: String
)

data class FaultSymptom(
    val symptomName: String,
    val probableCause: String,
    val severity: String,
    val actionAdvice: String,
    val recommendedCategory: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceLogScreen(
    onNavigateToCategoryList: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Vehicle details state
    var vehicleName by remember { mutableStateOf("Toyota Corolla 2018") }
    var licensePlate by remember { mutableStateOf("AB-8942-MD") }
    var currentMileage by remember { mutableIntStateOf(128500) }
    var isEditingVehicle by remember { mutableStateOf(false) }

    // Maintenance tasks list
    val maintenanceTasks = remember {
        mutableStateListOf(
            MaintenanceItem("Vidange Moteur (Huile + Filtre)", 5000, 125000, "5W30 / 10W40 Synthétique", "25.000 FCFA"),
            MaintenanceItem("Contrôle & Remplacement Freins", 15000, 115000, "Plaquettes d'origine Toyota", "18.000 FCFA"),
            MaintenanceItem("Filtre à Air & Habitacle", 10000, 120000, "Filtre certifié dépoussiérage", "8.000 FCFA"),
            MaintenanceItem("Courroie de Distribution", 60000, 80000, "Kit courroie + galet tendeur", "65.000 FCFA"),
            MaintenanceItem("Contrôle Pression & Pneumatique", 3000, 127000, "Vérification usure & gonflage", "2.000 FCFA")
        )
    }

    // Historical service logs
    val serviceHistory = remember {
        mutableStateListOf(
            ServiceRecord("12 Mai 2026", "Vidange Huile Moteur 5W30", "Garage Pro Mali - Bamako", 125000, "25.000 FCFA"),
            ServiceRecord("10 Fév 2026", "Changement Filtre à Air", "Auto Pièces Coulibaly", 120000, "8.000 FCFA"),
            ServiceRecord("18 Nov 2025", "Changement Plaquettes de Frein", "Mali Mécanique Express", 115000, "18.000 FCFA")
        )
    }

    // Standard symptoms database for Mali drivers
    val faultSymptoms = listOf(
        FaultSymptom(
            symptomName = "Voyant Moteur Allumé (Check Engine)",
            probableCause = "Capteur défaillant, carburant de mauvaise qualité ou problème d'injection",
            severity = "Moyenne - À vérifier rapidement",
            actionAdvice = "Passez chez un garage équipé d'une valise de diagnostic OBD2.",
            recommendedCategory = "MECANICIEN"
        ),
        FaultSymptom(
            symptomName = "Bruit de grincement métallique au freinage",
            probableCause = "Plaquettes de frein complètement usées frottant contre les disques",
            severity = "Haute Urgence",
            actionAdvice = "Ne pas différer. Remplacez immédiatement les plaquettes pour éviter d'endommager les disques.",
            recommendedCategory = "MECANICIEN"
        ),
        FaultSymptom(
            symptomName = "Moteur chauffe / Aiguille de température dans le rouge",
            probableCause = "Fuite du liquide de refroidissement, ventilateur ou thermostat bloqué",
            severity = "Critique - Risque de joint de culasse",
            actionAdvice = "Arrêtez le véhicule immédiatement et coupez le moteur. Laissez refroidir avant d'ouvrir le bouchon.",
            recommendedCategory = "AUTRE"
        ),
        FaultSymptom(
            symptomName = "Climatisation souffle de l'air chaud",
            probableCause = "Manque de gaz réfrigérant R134a ou fuite dans le condenseur",
            severity = "Confort",
            actionAdvice = "Faites effectuer une recharge de gaz et une détection de fuite chez un spécialiste clim auto.",
            recommendedCategory = "MECANICIEN"
        ),
        FaultSymptom(
            symptomName = "Direction lourde ou tirage d'un côté",
            probableCause = "Pression des pneus asymétrique, parallélisme déréglé ou usure de crémaillère",
            severity = "Moyenne",
            actionAdvice = "Vérifiez la pression des pneus puis réalisez un parallélisme / géométrie des trains.",
            recommendedCategory = "PNEUMATIQUE"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carnet d'Entretien Auto", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Vehicle Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BluePrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(vehicleName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                                Text("Matricule: $licensePlate", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                            }
                        }

                        IconButton(onClick = { isEditingVehicle = !isEditingVehicle }) {
                            Icon(Icons.Default.Build, contentDescription = "Modifier", tint = Color.White)
                        }
                    }

                    if (isEditingVehicle) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = vehicleName,
                            onValueChange = { vehicleName = it },
                            label = { Text("Modèle du véhicule", color = Color.White) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = licensePlate,
                                onValueChange = { licensePlate = it },
                                label = { Text("Immatriculation", color = Color.White) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = currentMileage.toString(),
                                onValueChange = { it.toIntOrNull()?.let { km -> currentMileage = km } },
                                label = { Text("Kilométrage (km)", color = Color.White) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { isEditingVehicle = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Enregistrer", color = BluePrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Compteur actuel:", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                                }
                                Text(
                                    text = "$currentMileage km",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = BluePrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = BluePrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("ÉCHÉANCES & RAPPELS", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("HISTORIQUE", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("DIAGNOSTIC PANNE", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Reminders & Scheduled Tasks
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(maintenanceTasks) { task ->
                            val nextDueKm = task.lastDoneKm + task.intervalKm
                            val remainingKm = nextDueKm - currentMileage
                            val progress = ((currentMileage - task.lastDoneKm).toFloat() / task.intervalKm.toFloat()).coerceIn(0f, 1f)

                            val statusColor = when {
                                remainingKm <= 0 -> Color(0xFFE53935) // Urgent / Overdue
                                remainingKm <= 1000 -> Color(0xFFFF9800) // Warning
                                else -> Color(0xFF4CAF50) // OK
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = task.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1F2937),
                                            modifier = Modifier.weight(1f, fill = false),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = statusColor.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = if (remainingKm <= 0) "Urgent !" else if (remainingKm <= 1000) "Bientôt" else "En ordre",
                                                color = statusColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape),
                                        color = statusColor,
                                        trackColor = Color(0xFFE0E0E0),
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Prochaine à : $nextDueKm km",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = if (remainingKm >= 0) "Reste $remainingKm km" else "Dépassé de ${-remainingKm} km",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Estimation: ${task.estimatedCostFcfa}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BluePrimary,
                                            modifier = Modifier.weight(1f, fill = false),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = {
                                                // Log service completed
                                                serviceHistory.add(
                                                    0,
                                                    ServiceRecord("Aujourd'hui", task.title, "Garage Partenaire Auto Connect", currentMileage, task.estimatedCostFcfa)
                                                )
                                                Toast.makeText(context, "Entretien enregistré dans l'historique !", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text("Valider révision", fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Service History Log
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Historique des Réparations", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Surface(
                                    color = BluePrimary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${serviceHistory.size} révisions",
                                        color = BluePrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        items(serviceHistory) { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE8F5E9),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(record.serviceName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${record.garageName} • ${record.date}", color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Kilométrage: ${record.mileageKm} km", color = BluePrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(record.costFcfa, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF2E7D32), maxLines = 1)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Symptom Fault Diagnostic Guide
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Guide de Diagnostic des Pannes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Sélectionnez le comportement anormal de votre véhicule pour obtenir une analyse instantanée.", color = Color.Gray, fontSize = 12.sp)
                        }

                        items(faultSymptoms) { symptom ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(symptom.symptomName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Cause probable:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                                    Text(symptom.probableCause, fontSize = 13.sp, color = Color(0xFF374151))

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text("Conseil Auto Connect:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BluePrimary)
                                    Text(symptom.actionAdvice, fontSize = 13.sp, color = Color(0xFF1F2937))

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color(0xFFFFF3E0),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = symptom.severity,
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        Button(
                                            onClick = { onNavigateToCategoryList(symptom.recommendedCategory) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text("Trouver un spécialiste", fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
