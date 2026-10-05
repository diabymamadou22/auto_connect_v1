package com.example.autoconnect.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.viewmodel.ServicesViewModel
import java.util.Locale
import java.util.UUID

enum class DiscoveryFilterTab(val title: String, val category: ServiceCategory?, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ALL("Tous", null, Icons.Default.DirectionsCar),
    MECHANICS("Mécaniciens", ServiceCategory.MECANICIEN, Icons.Default.Handyman),
    PARTS("Pièces Auto", ServiceCategory.PIECES, Icons.Default.Storefront),
    TIRES("Pneus", ServiceCategory.PNEUMATIQUE, Icons.Default.TireRepair),
    FAVORITES("Favoris", null, Icons.Default.Favorite)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProviderDiscoveryScreen(
    servicesViewModel: ServicesViewModel,
    onNavigateToDetail: (ServiceProvider) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allServices by servicesViewModel.allServices.collectAsState()

    var selectedTab by remember { mutableStateOf(DiscoveryFilterTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("Toutes") }
    var onlyOpenNow by remember { mutableStateOf(false) }
    var minRating by remember { mutableDoubleStateOf(0.0) }
    var sortBy by remember { mutableStateOf("rating") } // "rating", "distance", "name"

    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Distinct list of cities found in Room
    val availableCities = remember(allServices) {
        listOf("Toutes") + allServices.map { it.city }.filter { it.isNotBlank() }.distinct().sorted()
    }

    // Counts from Room database
    val mechanicsCount = remember(allServices) {
        allServices.count { it.category == ServiceCategory.MECANICIEN }
    }
    val partsShopsCount = remember(allServices) {
        allServices.count { it.category == ServiceCategory.PIECES }
    }
    val tiresCount = remember(allServices) {
        allServices.count { it.category == ServiceCategory.PNEUMATIQUE }
    }
    val favoritesCount = remember(allServices) {
        allServices.count { it.isFavorite }
    }

    // Filtered providers
    val filteredList = remember(allServices, selectedTab, searchQuery, selectedCity, onlyOpenNow, minRating, sortBy) {
        var list = allServices.filter { provider ->
            // Tab filter
            val matchesTab = when (selectedTab) {
                DiscoveryFilterTab.ALL -> true
                DiscoveryFilterTab.MECHANICS -> provider.category == ServiceCategory.MECANICIEN
                DiscoveryFilterTab.PARTS -> provider.category == ServiceCategory.PIECES
                DiscoveryFilterTab.TIRES -> provider.category == ServiceCategory.PNEUMATIQUE
                DiscoveryFilterTab.FAVORITES -> provider.isFavorite
            }

            // Search query filter (matches name, description, city, or services/parts offered)
            val query = searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                    provider.name.lowercase().contains(query) ||
                    provider.description.lowercase().contains(query) ||
                    provider.city.lowercase().contains(query) ||
                    (provider.servicesOffered?.lowercase()?.contains(query) == true)

            // City filter
            val matchesCity = selectedCity == "Toutes" || provider.city.equals(selectedCity, ignoreCase = true)

            // Open status
            val matchesOpen = !onlyOpenNow || provider.isOpen

            // Rating
            val matchesRating = provider.rating >= minRating

            matchesTab && matchesSearch && matchesCity && matchesOpen && matchesRating
        }

        // Sorting
        when (sortBy) {
            "distance" -> list.sortedBy { it.getDistance(12.6392, -8.0029) }
            "name" -> list.sortedBy { it.name.lowercase() }
            else -> list.sortedByDescending { it.rating }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Découverte Prestataires",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Mécaniciens & Boutiques de Pièces (Base Room)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("discovery_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier.testTag("discovery_map_button")
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Voir sur la Carte",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.testTag("discovery_filter_button")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Filtres avancés",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("discovery_add_provider_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter un prestataire")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nouveau", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Search Input Header
            item {
                Surface(
                    color = BluePrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "Rechercher mécanicien, pièce, quartier...",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    },
                                    singleLine = true,
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Effacer", tint = Color.Gray)
                                            }
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("discovery_search_input")
                                )
                            }
                        }
                    }
                }
            }

            // Room Database Stats Pill Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = BluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Base Room SQLite locale",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${allServices.size} prestataires enregistrés hors-ligne",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "En direct",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Primary Discovery Filter Tabs (Mechanics vs Parts Shops vs Tires vs All)
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    contentColor = BluePrimary,
                    divider = {},
                    indicator = {}
                ) {
                    DiscoveryFilterTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        val badgeCount = when (tab) {
                            DiscoveryFilterTab.ALL -> allServices.size
                            DiscoveryFilterTab.MECHANICS -> mechanicsCount
                            DiscoveryFilterTab.PARTS -> partsShopsCount
                            DiscoveryFilterTab.TIRES -> tiresCount
                            DiscoveryFilterTab.FAVORITES -> favoritesCount
                        }

                        Surface(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) BluePrimary else Color.White,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
                            modifier = Modifier
                                .padding(end = 8.dp, top = 4.dp, bottom = 8.dp)
                                .testTag("discovery_tab_${tab.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFF475569),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = "$badgeCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF64748B),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Filter Chips Row (City, Open now, Rating, Sorting)
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = onlyOpenNow,
                            onClick = { onlyOpenNow = !onlyOpenNow },
                            label = { Text("Ouvert maintenant", fontSize = 12.sp) },
                            leadingIcon = {
                                if (onlyOpenNow) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDCFCE7),
                                selectedLabelColor = Color(0xFF166534)
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = minRating >= 4.5,
                            onClick = { minRating = if (minRating >= 4.5) 0.0 else 4.5 },
                            label = { Text("⭐ 4.5+ étoiles", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = Color(0xFF92400E)
                            )
                        )
                    }

                    if (selectedCity != "Toutes") {
                        item {
                            AssistChip(
                                onClick = { selectedCity = "Toutes" },
                                label = { Text("Ville : $selectedCity", fontSize = 12.sp) },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = "Effacer ville", modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }

                    item {
                        AssistChip(
                            onClick = {
                                sortBy = when (sortBy) {
                                    "rating" -> "distance"
                                    "distance" -> "name"
                                    else -> "rating"
                                }
                            },
                            label = {
                                Text(
                                    when (sortBy) {
                                        "distance" -> "📍 Tri : Proximité"
                                        "name" -> "🔤 Tri : Nom"
                                        else -> "⭐ Tri : Meilleures Notes"
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }

            // Results count and active filter summary
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredList.size} résultats trouvés",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )

                    if (selectedTab != DiscoveryFilterTab.ALL || searchQuery.isNotEmpty() || selectedCity != "Toutes" || onlyOpenNow || minRating > 0.0) {
                        TextButton(
                            onClick = {
                                selectedTab = DiscoveryFilterTab.ALL
                                searchQuery = ""
                                selectedCity = "Toutes"
                                onlyOpenNow = false
                                minRating = 0.0
                                sortBy = "rating"
                            }
                        ) {
                            Text("Réinitialiser", fontSize = 12.sp, color = BluePrimary)
                        }
                    }
                }
            }

            // Empty state if no providers match
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Aucun prestataire trouvé",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Essayez d'élargir votre recherche, de changer de quartier ou d'ajouter un nouveau garage à la base.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedTab = DiscoveryFilterTab.ALL
                                    selectedCity = "Toutes"
                                    onlyOpenNow = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Text("Voir tous les prestataires")
                            }
                        }
                    }
                }
            } else {
                // List of Mechanics and Parts Shops
                items(filteredList, key = { it.id }) { provider ->
                    DiscoveryProviderCard(
                        provider = provider,
                        onToggleFavorite = { servicesViewModel.toggleFavorite(provider.id) },
                        onCardClick = { onNavigateToDetail(provider) },
                        onCallClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                            context.startActivity(intent)
                        },
                        onChatClick = {
                            onNavigateToChat(provider.id)
                        },
                        onItineraryClick = {
                            val uri = Uri.parse("geo:${provider.latitude},${provider.longitude}?q=${provider.latitude},${provider.longitude}(${Uri.encode(provider.name)})")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtres de Découverte",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                    TextButton(
                        onClick = {
                            selectedCity = "Toutes"
                            onlyOpenNow = false
                            minRating = 0.0
                            sortBy = "rating"
                        }
                    ) {
                        Text("Effacer tout", color = BluePrimary)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text("Ville / Région", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                var cityDropdownOpen by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = cityDropdownOpen,
                    onExpandedChange = { cityDropdownOpen = !cityDropdownOpen },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityDropdownOpen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = cityDropdownOpen,
                        onDismissRequest = { cityDropdownOpen = false }
                    ) {
                        availableCities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    selectedCity = city
                                    cityDropdownOpen = false
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
                    Column {
                        Text("Ouvert actuellement", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Afficher uniquement les ateliers ouverts", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = onlyOpenNow,
                        onCheckedChange = { onlyOpenNow = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Trier par", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sortBy == "rating",
                        onClick = { sortBy = "rating" },
                        label = { Text("Note ⭐") }
                    )
                    FilterChip(
                        selected = sortBy == "distance",
                        onClick = { sortBy = "distance" },
                        label = { Text("Distance 📍") }
                    )
                    FilterChip(
                        selected = sortBy == "name",
                        onClick = { sortBy = "name" },
                        label = { Text("Nom (A-Z)") }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Appliquer les filtres", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add Local Provider Dialog (Persisted in Room Database)
    if (showAddDialog) {
        AddProviderToRoomDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newProvider ->
                servicesViewModel.addService(newProvider)
                Toast.makeText(context, "« ${newProvider.name} » enregistré avec succès dans la base Room !", Toast.LENGTH_LONG).show()
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DiscoveryProviderCard(
    provider: ServiceProvider,
    onToggleFavorite: () -> Unit,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onChatClick: () -> Unit,
    onItineraryClick: () -> Unit
) {
    val isMechanic = provider.category == ServiceCategory.MECANICIEN
    val isPartsShop = provider.category == ServiceCategory.PIECES

    // Badge configuration
    val categoryBadgeText = when (provider.category) {
        ServiceCategory.MECANICIEN -> "MÉCANICIEN & GARAGE"
        ServiceCategory.PIECES -> "BOUTIQUE DE PIÈCES"
        ServiceCategory.PNEUMATIQUE -> "PNEUMATIQUE"
        ServiceCategory.AUTRE -> "SERVICE AUTO"
    }

    val categoryColor = provider.category.color
    val distanceKm = remember(provider) {
        String.format(Locale.US, "%.1f km", provider.getDistance(12.6392, -8.0029))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onCardClick() }
            .testTag("provider_card_${provider.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Category Chip + Proximity + Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = categoryColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = provider.category.icon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = categoryBadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = distanceKm,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (provider.isFavorite) Color(0xFFEF4444) else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Name and Open Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = provider.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${provider.city} • ${provider.phone}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Surface(
                    color = if (provider.isOpen) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (provider.isOpen) Color(0xFF16A34A) else Color(0xFFDC2626))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (provider.isOpen) "Ouvert" else "Fermé",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (provider.isOpen) Color(0xFF166534) else Color(0xFF991B1B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description / Specialties
            Text(
                text = provider.description,
                fontSize = 13.sp,
                color = Color(0xFF334155),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Services & Parts Offered Chips
            if (!provider.servicesOffered.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val tags = remember(provider.servicesOffered) {
                    provider.servicesOffered.split(",", ";").map { it.trim() }.filter { it.isNotBlank() }.take(4)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions: Rating + Call + Chat + Itinerary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating Score
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = provider.rating.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                // Quick Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Call Button
                    Surface(
                        onClick = onCallClick,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.testTag("call_${provider.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Appeler", tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Appeler", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                    }

                    // Chat / Quote Button
                    Surface(
                        onClick = onChatClick,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.testTag("chat_${provider.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Devis", tint = BluePrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Devis", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                        }
                    }

                    // Itinerary Button
                    Surface(
                        onClick = onItineraryClick,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3E8FF),
                        modifier = Modifier.testTag("nav_${provider.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = "Itinéraire", tint = Color(0xFF7E22CE), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7E22CE))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddProviderToRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (ServiceProvider) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ServiceCategory.MECANICIEN) }
    var city by remember { mutableStateOf("Bamako") }
    var phone by remember { mutableStateOf("+223 ") }
    var description by remember { mutableStateOf("") }
    var servicesOffered by remember { mutableStateOf("") }
    var latitudeStr by remember { mutableStateOf("12.6392") }
    var longitudeStr by remember { mutableStateOf("-8.0029") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enregistrer dans la Base Room",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF0F172A)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Ajoutez un atelier de mécanique ou un magasin de pièces qui sera sauvegardé dans la base SQLite locale Room.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Category Selection
                item {
                    Text("Type d'établissement :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == ServiceCategory.MECANICIEN,
                            onClick = { selectedCategory = ServiceCategory.MECANICIEN },
                            label = { Text("🔧 Mécanicien") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedCategory == ServiceCategory.PIECES,
                            onClick = { selectedCategory = ServiceCategory.PIECES },
                            label = { Text("🔩 Pièces auto") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom du garage ou magasin *") },
                        placeholder = { Text("Ex: Garage Moderne Lafiabougou") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Ville / Quartier *") },
                        placeholder = { Text("Ex: Bamako (Hamdallaye)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Téléphone *") },
                        placeholder = { Text("+223 76 00 00 00") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Spécialités") },
                        placeholder = { Text("Ex: Spécialiste freinage, vidange, pièces Toyota...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                item {
                    OutlinedTextField(
                        value = servicesOffered,
                        onValueChange = { servicesOffered = it },
                        label = { Text("Pièces ou services clés (séparés par des virgules)") },
                        placeholder = { Text("Ex: Batteries, Plaquettes, Filtres, Valise OBD") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text("Coordonnées GPS :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latitudeStr,
                            onValueChange = { latitudeStr = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = longitudeStr,
                            onValueChange = { longitudeStr = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || city.isBlank() || phone.isBlank()) {
                        return@Button
                    }
                    val lat = latitudeStr.toDoubleOrNull() ?: 12.6392
                    val lng = longitudeStr.toDoubleOrNull() ?: -8.0029

                    val newProvider = ServiceProvider(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        category = selectedCategory,
                        description = if (description.isNotBlank()) description.trim() else "Atelier et service professionnel enregistré.",
                        city = city.trim(),
                        phone = phone.trim(),
                        rating = 5.0,
                        latitude = lat,
                        longitude = lng,
                        isOpen = true,
                        isFavorite = false,
                        isMine = true,
                        hours = "08:00 - 18:30",
                        servicesOffered = if (servicesOffered.isNotBlank()) servicesOffered.trim() else null
                    )
                    onConfirm(newProvider)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Enregistrer dans Room")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = Color.Gray)
            }
        }
    )
}
