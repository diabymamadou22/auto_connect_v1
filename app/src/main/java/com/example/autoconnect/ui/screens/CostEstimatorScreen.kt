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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.ui.theme.BluePrimary

data class RepairOption(
    val id: String,
    val name: String,
    val category: String,
    val basePartsCostFcfa: Int,
    val baseLaborCostFcfa: Int,
    val description: String
)

data class DrivingTip(
    val title: String,
    val iconEmoji: String,
    val season: String,
    val advice: String,
    val importance: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostEstimatorScreen(
    onNavigateToMecaniciens: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Vehicle Category Multiplier (Citadine x1.0, SUV/4x4 x1.35, Utilitaire/Camionette x1.5)
    var vehicleCategory by remember { mutableStateOf("SUV / 4x4") }
    val categoryMultiplier = when (vehicleCategory) {
        "Citadine (Toyota Yaris, etc.)" -> 1.0
        "SUV / 4x4 (Prado, RAV4, Pick-up)" -> 1.35
        "Utilitaire / Camionette" -> 1.5
        else -> 1.15
    }

    val repairCatalog = remember {
        listOf(
            RepairOption("1", "Vidange Moteur + Filtre Huile", "Entretien", 18000, 5000, "Huile synthétique 10W40/5W30 + filtre d'origine"),
            RepairOption("2", "Changement Plaquettes de Freins Avant", "Freinage", 15000, 5000, "Jeu de plaquettes haute résistance à la chaleur"),
            RepairOption("3", "Remplacement Jeu d'Amortisseurs Avant", "Suspension", 45000, 15000, "Amortisseurs adaptés aux routes et pistes du Mali"),
            RepairOption("4", "Diagnostic Valise Électronique (OBD2)", "Électronique", 0, 10000, "Lecture et effacement des codes d'erreur calculateur"),
            RepairOption("5", "Parallélisme & Géométrie des Roues", "Pneumatique", 0, 8000, "Réglage au laser de l'alignement des trains roulants"),
            RepairOption("6", "Recharge Gaz Climatisation R134a", "Climatisation", 12000, 8000, "Tirage au vide, détection de fuite et recharge en gaz"),
            RepairOption("7", "Changement Kit Courroie de Distribution", "Moteur", 35000, 25000, "Courroie + galet tendeur + pompe à eau"),
            RepairOption("8", "Remplacement Batterie 12V 70Ah", "Électrique", 42000, 3000, "Batterie neuve garantie 12 mois")
        )
    }

    val selectedRepairIds = remember { mutableStateListOf("1", "2") }

    val totalPartsCost = selectedRepairIds.sumOf { id ->
        repairCatalog.find { it.id == id }?.let { (it.basePartsCostFcfa * categoryMultiplier).toInt() } ?: 0
    }
    val totalLaborCost = selectedRepairIds.sumOf { id ->
        repairCatalog.find { it.id == id }?.let { (it.baseLaborCostFcfa * categoryMultiplier).toInt() } ?: 0
    }
    val grandTotalFcfa = totalPartsCost + totalLaborCost

    val drivingTips = remember {
        listOf(
            DrivingTip(
                title = "Préparation à la Saison des Pluies (Hivernage)",
                iconEmoji = "🌧️",
                season = "Juin - Octobre",
                advice = "Contrôlez impérativement la profondeur des sculptures de vos pneus, l'état des balais d'essuie-glace et l'étanchéité de vos phares pour la traversée des zones inondées à Bamako.",
                importance = "Haute Priorité"
            ),
            DrivingTip(
                title = "Protection Contre la Poussière d'Harmattan",
                iconEmoji = "🌪️",
                season = "Novembre - Mars",
                advice = "La poussière fine s'infiltre rapidement. Nettoyez ou soufflez votre filtre à air tous les 2 000 km et vérifiez le liquide lave-glace.",
                importance = "Conseil Préventif"
            ),
            DrivingTip(
                title = "Contrôle Sécurité Avant Long Trajet (Bamako-Ségou-Mopti)",
                iconEmoji = "🛣️",
                season = "Toute l'année",
                advice = "Vérifiez la pression du pneu de secours, la présence du cric, de la clé de roue, du triangle de signalisation et du gilet réfléchissant.",
                importance = "Obligatoire"
            ),
            DrivingTip(
                title = "Conduite et Surchauffe par Forte Chaleur (>40°C)",
                iconEmoji = "☀️",
                season = "Avril - Mai",
                advice = "Ne rajoutez jamais d'eau du robinet froide directement dans un moteur surchauffé. Utilisez exclusivement du liquide de refroidissement adapté.",
                importance = "Urgence Moteur"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estimations & Devis Mali", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BluePrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BluePrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("ESTIMATEUR DE DEVIS", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("CONSEILS & SÉCURITÉ MALI", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Calculator Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Vehicle Category Selector
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = BluePrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("1. Type de Véhicule", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val categories = listOf("Citadine (Toyota Yaris, etc.)", "SUV / 4x4 (Prado, RAV4, Pick-up)", "Utilitaire / Camionette")
                                    categories.forEach { cat ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { vehicleCategory = cat }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            FilterChip(
                                                selected = vehicleCategory == cat,
                                                onClick = { vehicleCategory = cat },
                                                label = { Text(cat, fontSize = 12.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BluePrimary,
                                                    selectedLabelColor = Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Repair Services Selection
                        item {
                            Text("2. Sélectionnez les réparations à effectuer", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
                        }

                        items(repairCatalog) { repair ->
                            val isSelected = selectedRepairIds.contains(repair.id)
                            val partsCost = (repair.basePartsCostFcfa * categoryMultiplier).toInt()
                            val laborCost = (repair.baseLaborCostFcfa * categoryMultiplier).toInt()
                            val totalItemCost = partsCost + laborCost

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedRepairIds.remove(repair.id)
                                        else selectedRepairIds.add(repair.id)
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked == true) selectedRepairIds.add(repair.id)
                                            else selectedRepairIds.remove(repair.id)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = BluePrimary)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(repair.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(repair.description, fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Pièces: %,d FCFA • Main d'œuvre: %,d FCFA".format(partsCost, laborCost),
                                            fontSize = 11.sp,
                                            color = BluePrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = "%,d FCFA".format(totalItemCost),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }

                        // Summary & Estimated Total
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Estimation Totale (Mali)", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                        Surface(
                                            color = Color(0xFF334155),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "${selectedRepairIds.size} prestations",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "%,d FCFA".format(grandTotalFcfa),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF4ADE80),
                                        fontSize = 26.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Dont pièces: %,d FCFA | Main d'œuvre estimée: %,d FCFA".format(totalPartsCost, totalLaborCost),
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val shareText = "Devis estimatif Auto Connect Mali:\nType: $vehicleCategory\nServices: ${selectedRepairIds.size}\nMontant estimé: %,d FCFA".format(grandTotalFcfa)
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "Partager le devis"))
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Partager", color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }

                                        Button(
                                            onClick = onNavigateToMecaniciens,
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                            modifier = Modifier.weight(1.3f),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                                        ) {
                                            Text("Trouver un garage", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Mali Road Tips & Safety Guide
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Guide Pratique du Conducteur au Mali", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Recommandations adaptées au climat et au réseau routier malien.", color = Color.Gray, fontSize = 12.sp)
                        }

                        items(drivingTips) { tip ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tip.iconEmoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = tip.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1F2937),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = BluePrimary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = tip.season,
                                                color = BluePrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = Color(0xFFFFF3E0),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = tip.importance,
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(tip.advice, fontSize = 13.sp, color = Color(0xFF374151))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
