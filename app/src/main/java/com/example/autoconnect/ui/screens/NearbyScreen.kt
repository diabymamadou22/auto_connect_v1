package com.example.autoconnect.ui.screens

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.components.NearbyMapComponent
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.viewmodel.ServicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    servicesViewModel: ServicesViewModel,
    onNavigateToDetail: (ServiceProvider) -> Unit,
    onBack: () -> Unit
) {
    val allServices by servicesViewModel.allServices.collectAsState()

    // Default reference position: Bamako (12.6392, -8.0029)
    val userLat = 12.6392
    val userLng = -8.0029

    var maxDistanceKm by remember { mutableFloatStateOf(50f) }
    var selectedCategoryFilter by remember { mutableStateOf<ServiceCategory?>(null) }

    val nearbyProviders = remember(allServices, maxDistanceKm, selectedCategoryFilter) {
        allServices
            .filter { selectedCategoryFilter == null || it.category == selectedCategoryFilter }
            .map { Pair(it, it.getDistance(userLat, userLng)) }
            .filter { it.second <= maxDistanceKm }
            .sortedBy { it.second }
    }

    var isMapView by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prestataires à proximité", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { isMapView = true }) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Vue Carte",
                            tint = if (isMapView) Color.White else Color.White.copy(alpha = 0.55f)
                        )
                    }
                    IconButton(onClick = { isMapView = false }) {
                        Icon(
                            Icons.Default.FormatListBulleted,
                            contentDescription = "Vue Liste",
                            tint = if (!isMapView) Color.White else Color.White.copy(alpha = 0.55f)
                        )
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
            // Mode Selector Tab Row
            TabRow(
                selectedTabIndex = if (isMapView) 0 else 1,
                containerColor = Color.White,
                contentColor = BluePrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (isMapView) 0 else 1]),
                        color = BluePrimary
                    )
                }
            ) {
                Tab(
                    selected = isMapView,
                    onClick = { isMapView = true },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Carte Interactive", fontWeight = if (isMapView) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = !isMapView,
                    onClick = { isMapView = false },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Liste par Distance", fontWeight = if (!isMapView) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }

            if (isMapView) {
                NearbyMapComponent(
                    modifier = Modifier.fillMaxSize(),
                    services = allServices,
                    onNavigateToDetail = onNavigateToDetail,
                    isEmbedded = false
                )
            } else {
                // Distance & Category Filter Box
                Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = BluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rayon de recherche: ${maxDistanceKm.toInt()} km",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1F2937)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = maxDistanceKm,
                        onValueChange = { maxDistanceKm = it },
                        valueRange = 5f..200f,
                        steps = 38,
                        colors = SliderDefaults.colors(
                            thumbColor = BluePrimary,
                            activeTrackColor = BluePrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("Tous les services") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == ServiceCategory.MECANICIEN,
                                onClick = { selectedCategoryFilter = ServiceCategory.MECANICIEN },
                                label = { Text("🛠️ Mécaniciens") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFF9800), selectedLabelColor = Color.White)
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == ServiceCategory.PNEUMATIQUE,
                                onClick = { selectedCategoryFilter = ServiceCategory.PNEUMATIQUE },
                                label = { Text("🛞 Pneumatique") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == ServiceCategory.PIECES,
                                onClick = { selectedCategoryFilter = ServiceCategory.PIECES },
                                label = { Text("🚗 Pièces Auto") }
                            )
                        }
                    }
                }
            }

            if (nearbyProviders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun prestataire trouvé dans ce rayon.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(nearbyProviders, key = { it.first.id }) { (provider, distanceKm) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clickable { onNavigateToDetail(provider) },
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
                                    color = provider.category.color.copy(alpha = 0.15f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(provider.category.icon, contentDescription = null, tint = provider.category.color, modifier = Modifier.size(22.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = provider.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${provider.category.title} • ${provider.city}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                        Text(provider.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .background(BluePrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1f km", distanceKm),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = BluePrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
}
