package com.example.autoconnect.ui.screens

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.local.BookingEntity
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.viewmodel.AuthViewModel
import com.example.autoconnect.ui.viewmodel.ServicesViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPortalScreen(
    authViewModel: AuthViewModel,
    servicesViewModel: ServicesViewModel,
    onNavigateToAddService: () -> Unit,
    onNavigateToDetail: (ServiceProvider) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val allServices by servicesViewModel.allServices.collectAsState()
    val allBookings by servicesViewModel.allBookings.collectAsState()

    val isProUser = currentUser != null && (currentUser?.role == "prestataire" || currentUser?.role == "admin")

    // Show services marked as mine or matching provider username
    val myServices = remember(allServices, currentUser) {
        if (currentUser?.role == "admin") {
            allServices
        } else {
            allServices.filter { it.isMine || it.name.contains(currentUser?.username ?: "", ignoreCase = true) }
        }
    }

    val myBookings = remember(allBookings, myServices) {
        val myServiceIds = myServices.map { it.id }.toSet()
        val myServiceNames = myServices.map { it.name.lowercase() }.toSet()
        allBookings.filter { booking ->
            myServiceIds.contains(booking.providerId) || myServiceNames.any { booking.providerName.lowercase().contains(it) }
        }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Services, 1: RDV Clients
    var showFormSheet by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceProvider?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Espace Professionnel AutoConnect", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            if (!isProUser) {
                // Non-Authenticated Pro Warning Screen
                ProAccessRestrictedCard(
                    onLoginClick = onBack
                )
            } else {
                // Authenticated Pro Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Admin Authentication Badge Header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = null,
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = currentUser?.username?.replaceFirstChar { it.uppercase() } ?: "Pro",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 18.sp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = Color(0xFF10B981),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = "AUTHENTIFIÉ ADMIN",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Compte Professionnel Validé & Sécurisé",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Quick Metrics Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PortalStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "Établissements",
                                        value = myServices.size.toString()
                                    )
                                    PortalStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "RDV Reçus",
                                        value = myBookings.size.toString()
                                    )
                                    PortalStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "Statut",
                                        value = "Actif"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Navigation Tabs (Services vs RDV Clients)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.White,
                                contentColor = BluePrimary
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = {
                                        Text(
                                            "Mes Services (${myServices.size})",
                                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    icon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = {
                                        Text(
                                            "RDV Clients (${myBookings.size})",
                                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // TAB 0: Mes Établissements / Services
                    if (selectedTab == 0) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Services & Établissements", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1E293B))
                                Button(
                                    onClick = {
                                        editingService = null
                                        showFormSheet = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Nouveau Service", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (myServices.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Business, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Aucun service configuré pour le moment.", fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        Text("Créez votre première fiche d'établissement pour recevoir des clients.", color = Color.Gray, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                editingService = null
                                                showFormSheet = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                                        ) {
                                            Text("Créer un service maintenant", color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else {
                            items(myServices, key = { it.id }) { provider ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(provider.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("${provider.category.title} • ${provider.city}", color = BluePrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                if (!provider.phone.isBlank()) {
                                                    Text("Tél : ${provider.phone}", color = Color.Gray, fontSize = 12.sp)
                                                }
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        editingService = provider
                                                        showFormSheet = true
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = BluePrimary)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        servicesViewModel.deleteService(provider.id)
                                                        Toast.makeText(context, "Service supprimé", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                                                }
                                            }
                                        }

                                        if (provider.description.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = provider.description,
                                                color = Color(0xFF475569),
                                                fontSize = 12.sp,
                                                maxLines = 2
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(if (provider.isOpen) Color(0xFF10B981) else Color(0xFFEF4444))
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (provider.isOpen) "Ouvert aux clients" else "Fermé actuellement",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (provider.isOpen) Color(0xFF047857) else Color(0xFFB91C1C)
                                                )
                                            }

                                            Switch(
                                                checked = provider.isOpen,
                                                onCheckedChange = { isOpen ->
                                                    servicesViewModel.toggleServiceStatus(provider.id, isOpen)
                                                    Toast.makeText(context, if (isOpen) "Statut: Ouvert" else "Statut: Fermé", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: Rendez-vous Clients Reçus
                    if (selectedTab == 1) {
                        item {
                            Text("Demandes de Rendez-vous Clients", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (myBookings.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(44.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Aucun rendez-vous client pour le moment.", fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        Text("Les réservations effectuées par vos clients s'afficheront ici.", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            items(myBookings, key = { it.id }) { booking ->
                                ProBookingCard(
                                    booking = booking,
                                    onUpdateStatus = { newStatus ->
                                        servicesViewModel.updateBookingStatus(booking.id, newStatus)
                                        Toast.makeText(context, "Statut mis à jour : $newStatus", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Form Sheet to Add or Edit Service
    if (showFormSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFormSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            ServiceFormContent(
                editingService = editingService,
                onSave = { service ->
                    if (editingService != null) {
                        servicesViewModel.updateService(service)
                        Toast.makeText(context, "Service mis à jour !", Toast.LENGTH_SHORT).show()
                    } else {
                        servicesViewModel.addService(service)
                        Toast.makeText(context, "Nouveau service ajouté !", Toast.LENGTH_SHORT).show()
                    }
                    showFormSheet = false
                },
                onCancel = { showFormSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceFormContent(
    editingService: ServiceProvider?,
    onSave: (ServiceProvider) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(editingService?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(editingService?.category ?: ServiceCategory.MECANICIEN) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var city by remember { mutableStateOf(editingService?.city ?: "Bamako") }
    var phone by remember { mutableStateOf(editingService?.phone ?: "+223 ") }
    var description by remember { mutableStateOf(editingService?.description ?: "") }
    var hours by remember { mutableStateOf(editingService?.hours ?: "08:00 - 18:00") }
    var servicesOffered by remember { mutableStateOf(editingService?.servicesOffered ?: "Entretien, Réparation, Diagnostic") }
    var latText by remember { mutableStateOf(editingService?.latitude?.toString() ?: "12.6392") }
    var lngText by remember { mutableStateOf(editingService?.longitude?.toString() ?: "-8.0029") }

    val context = LocalContext.current
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNetwork = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val loc = lastGps ?: lastNetwork
                if (loc != null) {
                    latText = String.format(java.util.Locale.US, "%.5f", loc.latitude)
                    lngText = String.format(java.util.Locale.US, "%.5f", loc.longitude)
                    Toast.makeText(context, "Position GPS capturée avec succès !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Mise à jour GPS en cours...", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Erreur lors de la capture GPS", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permission GPS refusée", Toast.LENGTH_SHORT).show()
        }
    }

    val districtPresets = listOf(
        Triple("Badalabougou", 12.6212, -7.9895),
        Triple("ACI 2000", 12.6285, -8.0210),
        Triple("Hamdallaye", 12.6410, -8.0120),
        Triple("Faladié", 12.5920, -7.9530),
        Triple("Bacodjicoroni", 12.6015, -7.9950),
        Triple("Lafiabougou", 12.6480, -8.0350),
        Triple("Sogoniko", 12.6050, -7.9620),
        Triple("Titibougou", 12.6820, -7.9150),
        Triple("Kalaban Coro", 12.5680, -7.9810)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (editingService != null) "Modifier le Service" else "Ajouter un Service",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF0F172A)
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Fermer")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom de l'établissement / Atelier") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Selection Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory.title,
                onValueChange = {},
                readOnly = true,
                label = { Text("Catégorie de service") },
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
                ServiceCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.title) },
                        onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Ville") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Téléphone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = hours,
            onValueChange = { hours = it },
            label = { Text("Horaires d'ouverture") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = servicesOffered,
            onValueChange = { servicesOffered = it },
            label = { Text("Prestations offertes (séparées par des virgules)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description & Adresse détaillée") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Géolocalisation GPS du Garage", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                    }

                    OutlinedButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Capter GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Sélection rapide par quartier (Bamako) :", fontSize = 11.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(districtPresets) { district ->
                        Surface(
                            modifier = Modifier.clickable {
                                latText = district.second.toString()
                                lngText = district.third.toString()
                                Toast.makeText(context, "GPS réglé sur ${district.first}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = "📍 ${district.first}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Longitude", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Annuler")
            }

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val lat = latText.toDoubleOrNull() ?: 12.6392
                    val lng = lngText.toDoubleOrNull() ?: -8.0029

                    val service = ServiceProvider(
                        id = editingService?.id ?: UUID.randomUUID().toString(),
                        name = name,
                        category = selectedCategory,
                        description = description,
                        city = city,
                        phone = phone,
                        rating = editingService?.rating ?: 5.0,
                        latitude = lat,
                        longitude = lng,
                        isOpen = editingService?.isOpen ?: true,
                        isFavorite = editingService?.isFavorite ?: false,
                        isMine = true,
                        hours = hours,
                        servicesOffered = servicesOffered
                    )
                    onSave(service)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enregistrer", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProBookingCard(
    booking: BookingEntity,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
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
                Column {
                    Text(booking.clientName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    Text("Tél: ${booking.clientPhone}", color = BluePrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Surface(
                    color = when (booking.status) {
                        "CONFIRME" -> Color(0xFF10B981)
                        "TERMINE" -> Color(0xFF3B82F6)
                        "ANNULE" -> Color(0xFFEF4444)
                        else -> Color(0xFFF59E0B)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = booking.status,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Service : ${booking.serviceType}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text("Date : ${booking.date} à ${booking.timeSlot}", color = Color.Gray, fontSize = 12.sp)

            if (booking.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Notes : ${booking.notes}", color = Color(0xFF475569), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdateStatus("CONFIRME") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmer", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { onUpdateStatus("TERMINE") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Terminé", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { onUpdateStatus("ANNULE") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Annuler", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ProAccessRestrictedCard(onLoginClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFFFF9100),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Accès Espace Pro Restreint",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Seuls les professionnels dont le compte a été créé et authentifié par l'Administrateur peuvent accéder à la gestion des services.",
                color = Color(0xFF475569),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Se Connecter avec un Compte Pro", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PortalStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

