package com.example.autoconnect.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.theme.EmergencyRed
import com.example.autoconnect.ui.viewmodel.ServicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    servicesViewModel: ServicesViewModel,
    onNavigateToDetail: (ServiceProvider) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allServices by servicesViewModel.allServices.collectAsState()

    // Default reference position: Bamako, Mali (12.6392, -8.0029)
    var userLat by remember { mutableDoubleStateOf(12.6392) }
    var userLng by remember { mutableDoubleStateOf(-8.0029) }
    var isLocating by remember { mutableStateOf(false) }

    // Category Filter (Default to MECANICIEN for proximity map request)
    var selectedCategoryFilter by remember { mutableStateOf<ServiceCategory?>(ServiceCategory.MECANICIEN) }

    var selectedProvider by remember { mutableStateOf<ServiceProvider?>(null) }

    // Geolocation permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNetwork = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val loc = lastGps ?: lastNetwork
                if (loc != null) {
                    userLat = loc.latitude
                    userLng = loc.longitude
                    Toast.makeText(context, "Position GPS actualisée !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Position actuelle : Bamako, Mali", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Position : Bamako (Par défaut)", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permission refusée. Position par défaut : Bamako", Toast.LENGTH_SHORT).show()
        }
        isLocating = false
    }

    // Function to update location
    fun refreshLocation() {
        isLocating = true
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Filter services based on category and sort by distance to user
    val displayedServices = remember(allServices, selectedCategoryFilter, userLat, userLng) {
        val filtered = if (selectedCategoryFilter != null) {
            allServices.filter { it.category == selectedCategoryFilter }
        } else {
            allServices
        }
        filtered.sortedBy { it.getDistance(userLat, userLng) }
    }

    // Map bounding box for Mali coordinates:
    val minLat = 10.0
    val maxLat = 20.0
    val minLng = -12.0
    val maxLng = 4.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carte des Mécaniciens GPS", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { refreshLocation() }) {
                        if (isLocating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = "Ma Position", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { refreshLocation() },
                containerColor = BluePrimary,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Géolocaliser", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE8ECEF))
        ) {
            // Interactive Map Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(displayedServices, userLat, userLng) {
                        detectTapGestures { tapOffset ->
                            val width = size.width
                            val height = size.height

                            // Check tap on providers
                            var tapped: ServiceProvider? = null
                            for (provider in displayedServices) {
                                val x = ((provider.longitude - minLng) / (maxLng - minLng) * width).toFloat()
                                val y = ((maxLat - provider.latitude) / (maxLat - minLat) * height).toFloat()

                                val dx = tapOffset.x - x
                                val dy = tapOffset.y - y
                                if (dx * dx + dy * dy <= 45 * 45) {
                                    tapped = provider
                                    break
                                }
                            }
                            selectedProvider = tapped
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                // Draw decorative Mali region background shape
                val maliPath = Path().apply {
                    moveTo(width * 0.1f, height * 0.8f)
                    lineTo(width * 0.2f, height * 0.65f)
                    lineTo(width * 0.4f, height * 0.5f)
                    lineTo(width * 0.7f, height * 0.2f)
                    lineTo(width * 0.95f, height * 0.15f)
                    lineTo(width * 0.85f, height * 0.45f)
                    lineTo(width * 0.6f, height * 0.7f)
                    lineTo(width * 0.4f, height * 0.9f)
                    close()
                }
                drawPath(maliPath, color = Color(0xFFC8D6E5))
                drawPath(maliPath, color = Color(0xFF8395A7), style = Stroke(width = 3f))

                // Draw Niger river curve
                val riverPath = Path().apply {
                    moveTo(width * 0.15f, height * 0.82f)
                    cubicTo(
                        width * 0.35f, height * 0.75f,
                        width * 0.5f, height * 0.55f,
                        width * 0.85f, height * 0.42f
                    )
                }
                drawPath(riverPath, color = Color(0xFF54A0FF), style = Stroke(width = 6f))

                // Draw USER POSITION Marker
                val userX = ((userLng - minLng) / (maxLng - minLng) * width).toFloat()
                val userY = ((maxLat - userLat) / (maxLat - minLat) * height).toFloat()

                // Pulsing user aura
                drawCircle(color = BluePrimary.copy(alpha = 0.25f), radius = 42f, center = Offset(userX, userY))
                drawCircle(color = BluePrimary.copy(alpha = 0.5f), radius = 28f, center = Offset(userX, userY))
                drawCircle(color = Color.White, radius = 16f, center = Offset(userX, userY))
                drawCircle(color = BluePrimary, radius = 10f, center = Offset(userX, userY))

                // Draw markers for filtered providers
                for (provider in displayedServices) {
                    val x = ((provider.longitude - minLng) / (maxLng - minLng) * width).toFloat()
                    val y = ((maxLat - provider.latitude) / (maxLat - minLat) * height).toFloat()

                    val isSelected = selectedProvider?.id == provider.id
                    val pinColor = if (provider.category == ServiceCategory.MECANICIEN) Color(0xFFFF9800) else provider.category.color

                    // Outer halo
                    drawCircle(
                        color = if (isSelected) EmergencyRed else pinColor.copy(alpha = 0.35f),
                        radius = if (isSelected) 34f else 22f,
                        center = Offset(x, y)
                    )
                    // Inner pin
                    drawCircle(
                        color = if (isSelected) EmergencyRed else pinColor,
                        radius = if (isSelected) 20f else 14f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 8f else 5f,
                        center = Offset(x, y)
                    )
                }
            }

            // Top Control Bar (Category Filter Chips)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Position : Bamako • ${displayedServices.size} mécaniciens/services",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
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
                                    selected = selectedCategoryFilter == null,
                                    onClick = { selectedCategoryFilter = null },
                                    label = { Text("Tous les services") }
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

                Spacer(modifier = Modifier.height(8.dp))

                // Nearby Proximity Cards Carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(displayedServices, key = { it.id }) { provider ->
                        val distanceKm = provider.getDistance(userLat, userLng)
                        val isSelected = selectedProvider?.id == provider.id

                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { selectedProvider = provider },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFFF3E0) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = provider.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = BluePrimary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = String.format("%.1f km", distanceKm),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BluePrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${provider.category.title} • ${provider.city}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                    Text(provider.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Selected Provider Info Panel
            selectedProvider?.let { provider ->
                val distanceKm = provider.getDistance(userLat, userLng)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = provider.category.color.copy(alpha = 0.15f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(provider.category.icon, contentDescription = null, tint = provider.category.color)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(provider.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${provider.category.title} • ${provider.city}", color = Color.Gray, fontSize = 12.sp)
                            }
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.1f km", distanceKm),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val geoUri = Uri.parse("geo:${provider.latitude},${provider.longitude}?q=${provider.latitude},${provider.longitude}(${Uri.encode(provider.name)})")
                                    val intent = Intent(Intent.ACTION_VIEW, geoUri)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Itinéraire", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Appeler", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onNavigateToDetail(provider) },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Fiche", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
