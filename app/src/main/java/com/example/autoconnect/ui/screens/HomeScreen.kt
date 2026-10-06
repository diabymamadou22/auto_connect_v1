package com.example.autoconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.data.sync.SyncStatus
import com.example.autoconnect.ui.components.NearbyMapComponent
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.theme.EmergencyRed
import com.example.autoconnect.ui.viewmodel.AuthViewModel
import com.example.autoconnect.ui.viewmodel.ServicesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    servicesViewModel: ServicesViewModel,
    onNavigateToDetail: (ServiceProvider) -> Unit,
    onNavigateToCategoryList: (ServiceCategory) -> Unit,
    onNavigateToAddService: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToNearby: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToProviderPortal: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onNavigateToMaintenanceLog: () -> Unit,
    onNavigateToCostEstimator: () -> Unit,
    onNavigateToTutorials: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToTowingRequest: () -> Unit,
    onNavigateToVehicleHealthReport: () -> Unit,
    onNavigateToChatList: () -> Unit,
    onNavigateToDiscovery: () -> Unit = {},
    onNavigateToMechanicSearch: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val allServices by servicesViewModel.allServices.collectAsState()
    val services by servicesViewModel.filteredServices.collectAsState()
    val syncStatus by servicesViewModel.syncStatus.collectAsState()
    val isOnline by servicesViewModel.isOnline.collectAsState()

    val mechanicsCount = remember(allServices) {
        allServices.count { it.category == ServiceCategory.MECANICIEN }
    }
    val partsShopsCount = remember(allServices) {
        allServices.count { it.category == ServiceCategory.PIECES }
    }

    val searchQuery by servicesViewModel.searchQuery.collectAsState()
    val selectedCity by servicesViewModel.selectedCity.collectAsState()
    val onlyOpen by servicesViewModel.onlyOpen.collectAsState()
    val sortBy by servicesViewModel.sortBy.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val cities = remember(allServices) {
        listOf("Toutes") + allServices.map { it.city }.distinct()
    }

    val topRated = remember(allServices) {
        allServices.sortedByDescending { it.rating }.take(5)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BluePrimary)
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (currentUser?.username?.take(1) ?: "A").uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = BluePrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = currentUser?.username?.uppercase() ?: "UTILISATEUR",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = when (currentUser?.role) {
                                    "admin" -> "Administrateur"
                                    "prestataire" -> "Prestataire"
                                    else -> "Client / Utilisateur"
                                },
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = BluePrimary) },
                    label = { Text("Accueil", fontWeight = FontWeight.Bold) },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = BluePrimary) },
                    label = { Text("Tous les Prestataires & Garages", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToDiscovery()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = null, tint = BluePrimary) },
                    label = { Text("Recherche Avancée", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToMechanicSearch()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Map, contentDescription = null, tint = BluePrimary) },
                    label = { Text("Carte des Garages GPS") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = BluePrimary) },
                    label = { Text("Mon Véhicule (Carnet & Diagnostic)") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Bolt, contentDescription = null, tint = EmergencyRed) },
                    label = { Text("Urgence SOS 24/7", fontWeight = FontWeight.Bold, color = EmergencyRed) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToEmergency()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.HelpOutline, contentDescription = null, tint = BluePrimary) },
                    label = { Text("Comment ça marche ? (Aide)") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showHelpDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                if (currentUser?.role == "admin") {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF9C27B0)) },
                        label = { Text("Administration") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToAdminDashboard()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                if (currentUser?.role == "prestataire" || currentUser?.role == "admin") {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFFFF9800)) },
                        label = { Text("Portail Prestataire") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToProviderPortal()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = EmergencyRed) },
                    label = { Text("Déconnexion", color = EmergencyRed) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when(selectedTab) {
                                1 -> "Carte & Garages Proches"
                                2 -> "Mon Véhicule"
                                else -> "AutoConnect Mali"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 19.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showHelpDialog = true },
                            modifier = Modifier.testTag("home_guide_top_button")
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Guide & Aide", tint = Color.White)
                        }
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Actualisation des données...", Toast.LENGTH_SHORT).show()
                                servicesViewModel.syncDataNow { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("home_sync_top_button")
                        ) {
                            if (syncStatus is SyncStatus.Syncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudSync else Icons.Default.CloudOff,
                                    contentDescription = "Actualiser les données",
                                    tint = if (isOnline) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToEmergency) {
                            Icon(Icons.Default.Bolt, contentDescription = "Urgence", tint = Color(0xFFFFD700))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BluePrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
                        label = { Text("Accueil", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            indicatorColor = BluePrimary.copy(alpha = 0.12f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Carte") },
                        label = { Text("Carte", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            indicatorColor = BluePrimary.copy(alpha = 0.12f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Mon Auto") },
                        label = { Text("Mon Auto", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            indicatorColor = BluePrimary.copy(alpha = 0.12f)
                        )
                    )
                }
            },
            floatingActionButton = {
                if (currentUser?.role == "prestataire") {
                    ExtendedFloatingActionButton(
                        onClick = onNavigateToAddService,
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                        text = { Text("Créer une Prestation", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        containerColor = BluePrimary,
                        contentColor = Color.White
                    )
                } else if (currentUser?.role == "admin") {
                    ExtendedFloatingActionButton(
                        onClick = onNavigateToAdminDashboard,
                        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                        text = { Text("Créer un Prestataire", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        containerColor = Color(0xFF6A1B9A),
                        contentColor = Color.White
                    )
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> {
                    // TAB 0: ACCUEIL & RECHERCHE
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        // Search Header Section
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BluePrimary)
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Bonjour${if (!currentUser?.username.isNullOrBlank()) " " + currentUser!!.username.replaceFirstChar { it.uppercase() } else ""} 👋",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Trouvez rapidement un mécanicien ou des pièces au Mali",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = searchQuery,
                                                onValueChange = { servicesViewModel.setSearchQuery(it) },
                                                placeholder = { Text("Rechercher garage, mécanicien, pièce...", color = Color.Gray, fontSize = 14.sp) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color.Transparent,
                                                    unfocusedBorderColor = Color.Transparent
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = { showFilterSheet = true }) {
                                                Icon(Icons.Default.Tune, contentDescription = "Filtres", tint = BluePrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section d'Accueil : Statut discret, 4 Grandes Actions & Guide
                        item {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                                // Petite ligne statut & accès guide
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (isOnline) Color(0xFFECFDF5) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, if (isOnline) Color(0xFFA7F3D0) else Color(0xFFFDE68A)),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isOnline) "🟢 En direct • ${allServices.size} adresses" else "🟠 Mode hors-ligne actif",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isOnline) Color(0xFF065F46) else Color(0xFF92400E),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    TextButton(
                                        onClick = { showHelpDialog = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.HelpOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = BluePrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Guide d'utilisation",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BluePrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Que souhaitez-vous faire ?",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // 4 Grandes Actions Principales (2x2)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // 1. Trouver un Garage
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 115.dp)
                                            .clickable { onNavigateToDiscovery() }
                                            .testTag("home_main_garage_card"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E40AF)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(13.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color.White.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Column {
                                                Text(
                                                    "Trouver un Garage",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 13.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "Mécanique & révision",
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // 2. Trouver des Pièces
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 115.dp)
                                            .clickable { onNavigateToCategoryList(ServiceCategory.PIECES) }
                                            .testTag("home_main_parts_card"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(13.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color.White.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Column {
                                                Text(
                                                    "Pièces Auto",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 13.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "Boutiques certifiées",
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // 3. Carte Interactive
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 115.dp)
                                            .clickable { onNavigateToMap() }
                                            .testTag("home_main_map_card"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF059669)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(13.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color.White.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Column {
                                                Text(
                                                    "Carte des Garages",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 13.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "GPS & Ateliers proches",
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // 4. Urgence Dépannage
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 115.dp)
                                            .clickable { onNavigateToEmergency() }
                                            .testTag("home_main_emergency_card"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = EmergencyRed),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(13.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color.White.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Column {
                                                Text(
                                                    "🚨 Urgence SOS",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 13.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "Remorquage 24/7",
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Bannière d'Aide & Guide Express
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showHelpDialog = true }
                                        .testTag("home_guide_banner"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = BluePrimary.copy(alpha = 0.12f),
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Comment ça marche ?",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                "1. Choisissez un pro • 2. Appelez • 3. Suivez l'entretien",
                                                fontSize = 11.sp,
                                                color = Color(0xFF475569),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = BluePrimary.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                "Guide",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BluePrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Raccourcis pratiques : Mes Rendez-vous & Autour de moi
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onNavigateToBookings,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp), tint = BluePrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Mes Rendez-vous",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = onNavigateToNearby,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF059669))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Autour de moi",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Categories Grid Section
                        item {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Catégories de services",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    TextButton(onClick = onNavigateToDiscovery) {
                                        Text("DÉCOUVRIR TOUT", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CategoryCard(
                                        modifier = Modifier.weight(1f),
                                        category = ServiceCategory.MECANICIEN,
                                        onClick = { onNavigateToCategoryList(ServiceCategory.MECANICIEN) }
                                    )
                                    CategoryCard(
                                        modifier = Modifier.weight(1f),
                                        category = ServiceCategory.PIECES,
                                        onClick = { onNavigateToCategoryList(ServiceCategory.PIECES) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CategoryCard(
                                        modifier = Modifier.weight(1f),
                                        category = ServiceCategory.PNEUMATIQUE,
                                        onClick = { onNavigateToCategoryList(ServiceCategory.PNEUMATIQUE) }
                                    )
                                    CategoryCard(
                                        modifier = Modifier.weight(1f),
                                        category = ServiceCategory.AUTRE,
                                        onClick = { onNavigateToCategoryList(ServiceCategory.AUTRE) }
                                    )
                                }
                            }
                        }

                        // Top Prestataires Carousel
                        item {
                            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Top Garages & Mécaniciens",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    TextButton(onClick = { servicesViewModel.resetFilters() }) {
                                        Text("TOUT VOIR", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(topRated) { provider ->
                                        FeaturedProviderCard(
                                            provider = provider,
                                            onClick = { onNavigateToDetail(provider) }
                                        )
                                    }
                                }
                            }
                        }

                        // Services List Header & Chips
                        item {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tous les prestataires",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    IconButton(onClick = { showFilterSheet = true }) {
                                        Icon(Icons.Default.FilterList, contentDescription = "Filtres", tint = BluePrimary)
                                    }
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (selectedCity != "Toutes") {
                                        AssistChip(
                                            onClick = { servicesViewModel.setSelectedCity("Toutes") },
                                            label = { Text(selectedCity) },
                                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                    if (onlyOpen) {
                                        AssistChip(
                                            onClick = { servicesViewModel.setOnlyOpen(false) },
                                            label = { Text("Ouvert") },
                                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                }
                            }
                        }

                        // Services List Items
                        if (services.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aucun prestataire trouvé",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(services, key = { it.id }) { provider ->
                                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                                    ListProviderCard(
                                        provider = provider,
                                        onClick = { onNavigateToDetail(provider) },
                                        onNavigateToMap = onNavigateToMap
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: CARTE & PROXIMITE - Interactive Nearby Map Component
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NearbyMapComponent(
                            modifier = Modifier.fillMaxSize(),
                            services = services,
                            onNavigateToDetail = onNavigateToDetail,
                            onExpandFullScreen = onNavigateToMap,
                            isEmbedded = false
                        )
                    }
                }

                2 -> {
                    // TAB 2: MON VEHICULE (Carnet, Diagnostic, RDV, Devis, Tutoriels)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Gestion de mon Véhicule", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Text("Espace entretien, diagnostic et suivi mécanique", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Carnet d'entretien
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToMaintenanceLog() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF00897B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Carnet d'Entretien",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Rappels de vidange, filtres & suivi révision",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                }
                            }
                        }

                        // Mes Rendez-vous
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToBookings() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = BluePrimary)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Mes Rendez-vous Garage",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Gérer mes réservations en atelier",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                }
                            }
                        }

                        // Remorquage Express
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToTowingRequest() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = EmergencyRed)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Demande de Remorquage 24/7",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Dépannage d'urgence sur route",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                }
                            }
                        }

                        // Estimations & Devis FCFA
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToCostEstimator() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Estimations Devis FCFA",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Calculateur de prix moyen des pièces & réparations",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                }
                            }
                        }

                        // Tutoriels Hors Ligne
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToTutorials() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF15803D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.OfflinePin, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Tutoriels & Guides Hors-Ligne",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Auto-dépannage pas-à-pas accessible sans réseau",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                }
                            }
                        }

                        // Bilan de Santé PDF
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToVehicleHealthReport() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Bilan de Santé Auto (PDF & WhatsApp)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Générer un rapport complet partageable",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter Sheet Modal
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filtres", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    TextButton(onClick = { servicesViewModel.resetFilters() }) {
                        Text("Réinitialiser", color = BluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Ville", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                var cityExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { cityExpanded = !cityExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    servicesViewModel.setSelectedCity(city)
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ouvert actuellement", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Switch(
                        checked = onlyOpen,
                        onCheckedChange = { servicesViewModel.setOnlyOpen(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Trier par", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sortBy == "rating",
                        onClick = { servicesViewModel.setSortBy("rating") },
                        label = { Text("Note") }
                    )
                    FilterChip(
                        selected = sortBy == "distance",
                        onClick = { servicesViewModel.setSortBy("distance") },
                        label = { Text("Distance") }
                    )
                    FilterChip(
                        selected = sortBy == "name",
                        onClick = { servicesViewModel.setSortBy("name") },
                        label = { Text("Nom") }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Appliquer", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // Guide d'utilisation facile
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Guide AutoConnect Mali",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "AutoConnect vous permet de trouver et contacter facilement les meilleurs mécaniciens et vendeurs de pièces à Bamako.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )

                    HelpStepItem(
                        number = "1",
                        title = "Trouver un garage ou une pièce",
                        description = "Tapez votre besoin ou quartier dans la recherche, ou cliquez sur « Trouver un Garage » ou « Pièces Auto »."
                    )

                    HelpStepItem(
                        number = "2",
                        title = "Appeler ou s'y rendre",
                        description = "Sur la fiche d'un garage, touchez « Appeler » pour contacter l'artisan directement ou « Itinéraire » pour lancer le guidage GPS."
                    )

                    HelpStepItem(
                        number = "3",
                        title = "Carte interactive des garages",
                        description = "L'onglet « Carte » en bas affiche tous les garages et ateliers autour de vous sur un plan Google Maps."
                    )

                    HelpStepItem(
                        number = "4",
                        title = "Urgence & Remorquage 24/7",
                        description = "En cas de panne sur la route, le bouton rouge « Urgence SOS » vous permet d'appeler immédiatement un dépanneur."
                    )

                    HelpStepItem(
                        number = "5",
                        title = "Suivi de votre véhicule",
                        description = "Dans l'onglet « Mon Véhicule », notez vos vidanges dans le Carnet d'entretien et estimez le coût de vos pièces en FCFA."
                    )

                    HelpStepItem(
                        number = "6",
                        title = "Mode Hors-ligne garanti",
                        description = "Même sans connexion Internet, toutes les adresses et numéros enregistrés restent consultables !"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("J'ai compris", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun HelpStepItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = BluePrimary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF64748B), lineHeight = 16.sp)
        }
    }
}

@Composable
fun CategoryCard(
    modifier: Modifier = Modifier,
    category: ServiceCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = category.color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(category.icon, contentDescription = null, tint = Color.White)
                }
            }
            Text(
                text = category.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeaturedProviderCard(
    provider: ServiceProvider,
    onClick: () -> Unit
) {
    val imageUrl = when (provider.category) {
        ServiceCategory.PIECES -> "https://images.unsplash.com/photo-1507136566006-cfc505b114fc?auto=format&fit=crop&q=80&w=600"
        ServiceCategory.MECANICIEN -> "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&q=80&w=600"
        ServiceCategory.PNEUMATIQUE -> "https://images.unsplash.com/photo-1578844251758-2f71da64c96f?auto=format&fit=crop&q=80&w=600"
        ServiceCategory.AUTRE -> "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&q=80&w=600"
    }

    Card(
        modifier = Modifier
            .width(240.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = provider.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = provider.category.color,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = provider.category.title.uppercase(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = provider.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = provider.city,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = BluePrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(provider.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BluePrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListProviderCard(
    provider: ServiceProvider,
    onClick: () -> Unit,
    onNavigateToMap: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = provider.category.color.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = provider.category.icon,
                        contentDescription = null,
                        tint = provider.category.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = provider.city,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(provider.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                }
            }

            if (onNavigateToMap != null) {
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clickable { onNavigateToMap() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Carte",
                        tint = Color(0xFF059669),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
