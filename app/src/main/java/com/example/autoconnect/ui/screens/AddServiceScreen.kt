package com.example.autoconnect.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.model.OfferedService
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.viewmodel.AuthViewModel
import com.example.autoconnect.ui.viewmodel.ServicesViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    authViewModel: AuthViewModel,
    servicesViewModel: ServicesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val allServices by servicesViewModel.allServices.collectAsState()

    val isPrestataire = currentUser != null && (currentUser?.role == "prestataire" || currentUser?.role == "admin")

    val myWorkshop = remember(allServices, currentUser) {
        if (currentUser?.role == "admin") {
            allServices.firstOrNull()
        } else {
            allServices.firstOrNull { it.id == currentUser?.id }
                ?: allServices.firstOrNull { it.isMine }
                ?: allServices.firstOrNull { it.name.contains(currentUser?.username ?: "", ignoreCase = true) }
        }
    }

    val workshopId = myWorkshop?.id ?: "g1"
    val workshopName = myWorkshop?.name ?: (currentUser?.username?.replaceFirstChar { it.uppercase() } + " Atelier Auto")

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Mécanique") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf("15000") }
    var duration by remember { mutableStateOf("45 min") }
    var description by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }

    val categories = listOf(
        "Mécanique",
        "Diagnostic valise",
        "Freinage",
        "Climatisation",
        "Électricité",
        "Pneumatique",
        "Carrosserie",
        "Pièces détachées",
        "Entretien périodique",
        "Dépannage / Remorquage"
    )

    val quickServicePresets = listOf(
        Triple("Vidange Moteur 10W40 + Filtre", 15000, "30 min"),
        Triple("Diagnostic Électronique Valise OBD", 10000, "20 min"),
        Triple("Plaquettes de frein avant avec pose", 20000, "45 min"),
        Triple("Recharge Climatisation gaz R134a", 20000, "45 min"),
        Triple("Montage + Équilibrage 4 pneus", 12000, "40 min"),
        Triple("Rénovation Alternateur / Démarreur", 25000, "1h30")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer une Prestation", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { innerPadding ->
        if (!isPrestataire) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFFF9100), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Rôle Prestataire Requis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dans AutoConnect Mali :\n• L'Administrateur crée les prestataires (garages / ateliers).\n• Les Prestataires créent leurs services et leurs tarifs.\n• Les Clients consultent les prestataires et réservent leurs prestations.",
                    color = Color(0xFF475569),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retour à l'accueil", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Nouvelle Prestation pour votre Atelier", fontWeight = FontWeight.Bold, color = BluePrimary, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Établissement rattaché : $workshopName (${myWorkshop?.city ?: "Bamako"})",
                            fontSize = 12.sp,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }

                Text("Suggestions rapides de services :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickServicePresets) { preset ->
                        Surface(
                            modifier = Modifier.clickable {
                                title = preset.first
                                priceText = preset.second.toString()
                                duration = preset.third
                                description = "Prestation professionnelle de ${preset.first} garantie par $workshopName."
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = "+ ${preset.first}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de la prestation (ex: Vidange Moteur + Filtre)") },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = BluePrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie de prestation") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = BluePrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Tarif en FCFA") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF059669)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Durée estimée") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = BluePrimary) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Pièces incluses dans la prestation") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Disponibilité immédiate pour réservation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Les clients pourront réserver ce service", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Veuillez entrer le titre de la prestation", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val price = priceText.toIntOrNull() ?: 15000
                        val newService = OfferedService(
                            id = UUID.randomUUID().toString(),
                            providerId = workshopId,
                            providerName = workshopName,
                            title = title.trim(),
                            description = if (description.isBlank()) "Prestation certifiée réalisée par $workshopName." else description.trim(),
                            priceCfa = price,
                            durationMinutes = if (duration.isBlank()) "45 min" else duration.trim(),
                            category = selectedCategory,
                            isAvailable = isAvailable
                        )

                        servicesViewModel.addOfferedService(newService) {
                            Toast.makeText(context, "Prestation '$title' ajoutée au catalogue !", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enregistrer la prestation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
