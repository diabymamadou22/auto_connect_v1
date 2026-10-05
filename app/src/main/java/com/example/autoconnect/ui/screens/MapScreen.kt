package com.example.autoconnect.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.theme.EmergencyRed
import com.example.autoconnect.ui.viewmodel.ServicesViewModel
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    servicesViewModel: ServicesViewModel,
    onNavigateToDetail: (ServiceProvider) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allServices by servicesViewModel.allServices.collectAsState()

    // Reference position: Bamako, Mali (12.6392, -8.0029)
    var userLat by remember { mutableDoubleStateOf(12.6392) }
    var userLng by remember { mutableDoubleStateOf(-8.0029) }
    var isLocating by remember { mutableStateOf(false) }

    // Category Filter (Default to null = All discovered services)
    var selectedCategoryFilter by remember { mutableStateOf<ServiceCategory?>(null) }
    var isSatelliteMode by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<ServiceProvider?>(null) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        if (selectedProvider != null) {
            selectedProvider = null
        } else {
            onBack()
        }
    }

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
                    Toast.makeText(context, "Position GPS actualisée : Bamako !", Toast.LENGTH_SHORT).show()
                    webViewInstance?.evaluateJavascript("setUserLocation(${loc.latitude}, ${loc.longitude});", null)
                } else {
                    Toast.makeText(context, "Position actuelle : Bamako, Mali", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Position par défaut : Bamako", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permission refusée. Position : Bamako", Toast.LENGTH_SHORT).show()
        }
        isLocating = false
    }

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

    // Mechanics & parts shops counts from Room
    val mechanicsCount = remember(allServices) {
        allServices.count { it.category == ServiceCategory.MECANICIEN }
    }
    val partsShopsCount = remember(allServices) {
        allServices.count { it.category == ServiceCategory.PIECES }
    }

    // Updates markers in webview when filtered services or selection changes
    LaunchedEffect(displayedServices) {
        webViewInstance?.let { webView ->
            val jsonArray = buildServicesJson(displayedServices)
            webView.evaluateJavascript("updateMarkersData($jsonArray);", null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Carte Google Maps",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${allServices.size} adresses en base Room ($mechanicsCount mécaniciens, $partsShopsCount boutiques)",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("map_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Toggle Map Type (Roadmap / Satellite)
                    IconButton(
                        onClick = {
                            isSatelliteMode = !isSatelliteMode
                            webViewInstance?.evaluateJavascript("setMapType($isSatelliteMode);", null)
                            Toast.makeText(
                                context,
                                if (isSatelliteMode) "Vue Google Maps Satellite activée" else "Vue Google Maps Plan activée",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("map_layer_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSatelliteMode) Icons.Default.Map else Icons.Default.Layers,
                            contentDescription = "Changer le type de carte",
                            tint = Color.White
                        )
                    }

                    // GPS Re-center button
                    IconButton(
                        onClick = {
                            refreshLocation()
                            webViewInstance?.evaluateJavascript("centerOnUser();", null)
                        },
                        modifier = Modifier.testTag("map_locate_button")
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Ma Position",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    refreshLocation()
                    webViewInstance?.evaluateJavascript("centerOnUser();", null)
                },
                containerColor = BluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("map_fab_recenter")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ma Position", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Google Maps WebView
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        settings.cacheMode = WebSettings.LOAD_DEFAULT

                        val bridge = object {
                            @JavascriptInterface
                            fun onMarkerClicked(providerId: String) {
                                Handler(Looper.getMainLooper()).post {
                                    val found = allServices.find { it.id == providerId }
                                    selectedProvider = found
                                }
                            }

                            @JavascriptInterface
                            fun openDirections(lat: Double, lng: Double, name: String) {
                                Handler(Looper.getMainLooper()).post {
                                    val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    try {
                                        ctx.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(ctx, "Impossible d'ouvrir Google Maps", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }

                        addJavascriptInterface(bridge, "AndroidBridge")

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                val json = buildServicesJson(displayedServices)
                                view?.evaluateJavascript("updateMarkersData($json);", null)
                            }
                        }

                        val html = buildGoogleMapsHtml(displayedServices, userLat, userLng, isSatelliteMode)
                        loadDataWithBaseURL("https://www.google.com", html, "text/html", "UTF-8", null)
                        webViewInstance = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_maps_webview")
            )

            // Top Overlay: Category Filter Chips & Info Pill
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Coordonnées GPS Room : ${displayedServices.size} repères affichés",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == null,
                                    onClick = {
                                        selectedCategoryFilter = null
                                    },
                                    label = { Text("Tous (${allServices.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BluePrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_all")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == ServiceCategory.MECANICIEN,
                                    onClick = {
                                        selectedCategoryFilter = ServiceCategory.MECANICIEN
                                    },
                                    label = { Text("🛠️ Mécaniciens ($mechanicsCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1E3A8A),
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_mechanics")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == ServiceCategory.PIECES,
                                    onClick = {
                                        selectedCategoryFilter = ServiceCategory.PIECES
                                    },
                                    label = { Text("🏬 Pièces Auto ($partsShopsCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF059669),
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_parts")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == ServiceCategory.PNEUMATIQUE,
                                    onClick = {
                                        selectedCategoryFilter = ServiceCategory.PNEUMATIQUE
                                    },
                                    label = { Text("🛞 Pneumatique") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFD97706),
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_tires")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Carousel of Nearby Discovered Shops (Room DB)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(displayedServices, key = { it.id }) { provider ->
                        val distanceKm = provider.getDistance(userLat, userLng)
                        val isSelected = selectedProvider?.id == provider.id

                        Card(
                            modifier = Modifier
                                .width(230.dp)
                                .clickable {
                                    selectedProvider = provider
                                    webViewInstance?.evaluateJavascript(
                                        "focusMarker('${provider.id}', ${provider.latitude}, ${provider.longitude});",
                                        null
                                    )
                                }
                                .testTag("map_card_${provider.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
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
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = provider.rating.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Selected Provider Info Panel
            AnimatedVisibility(
                visible = selectedProvider != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                selectedProvider?.let { provider ->
                    val distanceKm = provider.getDistance(userLat, userLng)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("selected_provider_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = provider.category.color.copy(alpha = 0.15f),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            provider.category.icon,
                                            contentDescription = null,
                                            tint = provider.category.color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = provider.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (provider.isOpen) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (provider.isOpen) "Ouvert" else "Fermé",
                                                color = if (provider.isOpen) Color(0xFF15803D) else EmergencyRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = " ${provider.rating}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = " • ${String.format("%.1f km", distanceKm)}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { selectedProvider = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Fermer",
                                        tint = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Coordinates and address detail
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GPS: ${String.format("%.4f", provider.latitude)}°N, ${String.format("%.4f", provider.longitude)}°W • ${provider.city}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (!provider.servicesOffered.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = provider.servicesOffered,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Google Maps Navigation intent
                                Button(
                                    onClick = {
                                        val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${provider.latitude},${provider.longitude}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        try {
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Ouverture de Google Maps...", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("provider_directions_button")
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Itinéraire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Call phone
                                if (provider.phone.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Numéro : ${provider.phone}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("provider_call_button")
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Appeler", modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Full details button
                                Button(
                                    onClick = { onNavigateToDetail(provider) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("provider_details_button")
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Détails", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Builds JSON payload of all Room database providers to send to the Google Maps WebView.
 */
private fun buildServicesJson(services: List<ServiceProvider>): String {
    val array = JSONArray()
    for (s in services) {
        val obj = JSONObject().apply {
            put("id", s.id)
            put("name", s.name)
            put("category", s.category.name)
            put("categoryTitle", s.category.title)
            put("lat", s.latitude)
            put("lng", s.longitude)
            put("rating", s.rating)
            put("city", s.city)
            put("phone", s.phone)
            put("isOpen", s.isOpen)
            put("servicesOffered", s.servicesOffered ?: "")
        }
        array.put(obj)
    }
    return array.toString()
}

/**
 * Generates the Google Maps Leaflet-powered Web View HTML with custom mechanic/parts pins.
 */
private fun buildGoogleMapsHtml(
    services: List<ServiceProvider>,
    userLat: Double,
    userLng: Double,
    isSatellite: Boolean
): String {
    val servicesJson = buildServicesJson(services)

    return """
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <title>AutoConnect Mali Google Maps</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
        html, body {
            height: 100%;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: #E8ECEF;
            overflow: hidden;
        }
        #map {
            width: 100%;
            height: 100%;
        }
        /* Custom Google Maps style marker pin */
        .gmap-pin {
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50% 50% 50% 0;
            transform: rotate(-45deg);
            width: 38px;
            height: 38px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.35);
            border: 2px solid #ffffff;
            cursor: pointer;
            transition: transform 0.2s ease;
        }
        .gmap-pin-inner {
            transform: rotate(45deg);
            font-size: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            font-weight: bold;
        }
        .pin-mechanic { background: linear-gradient(135deg, #1E3A8A, #3B82F6); }
        .pin-parts { background: linear-gradient(135deg, #065F46, #10B981); }
        .pin-tires { background: linear-gradient(135deg, #B45309, #F59E0B); }
        .pin-other { background: linear-gradient(135deg, #581C87, #8B5CF6); }

        /* User Location Pulsing Beacon */
        .user-beacon {
            position: relative;
            width: 22px;
            height: 22px;
        }
        .user-beacon-center {
            width: 14px;
            height: 14px;
            background: #2563EB;
            border: 3px solid #ffffff;
            border-radius: 50%;
            box-shadow: 0 2px 6px rgba(0,0,0,0.4);
            position: absolute;
            top: 4px;
            left: 4px;
            z-index: 2;
        }
        .user-beacon-pulse {
            width: 28px;
            height: 28px;
            background: rgba(37, 99, 235, 0.4);
            border-radius: 50%;
            position: absolute;
            top: -3px;
            left: -3px;
            animation: pulse 1.8s infinite ease-out;
            z-index: 1;
        }
        @keyframes pulse {
            0% { transform: scale(0.6); opacity: 1; }
            100% { transform: scale(2.2); opacity: 0; }
        }

        /* Popup styling */
        .leaflet-popup-content-wrapper {
            border-radius: 14px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.2);
            padding: 2px;
        }
        .popup-card {
            font-size: 13px;
            line-height: 1.4;
        }
        .popup-title {
            font-weight: 700;
            font-size: 14px;
            color: #0F172A;
            margin-bottom: 2px;
        }
        .popup-cat {
            display: inline-block;
            font-size: 10px;
            font-weight: 700;
            padding: 2px 6px;
            border-radius: 6px;
            margin-bottom: 4px;
        }
        .popup-rating {
            color: #D97706;
            font-weight: 700;
            font-size: 12px;
        }
        .popup-btn {
            display: block;
            width: 100%;
            background: #1E3A8A;
            color: #ffffff;
            text-align: center;
            padding: 6px 0;
            margin-top: 8px;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 700;
            font-size: 11px;
        }
    </style>
</head>
<body>
    <div id="map"></div>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
        var map;
        var roadLayer;
        var satLayer;
        var markersGroup;
        var userMarker;
        var currentServices = $servicesJson;
        var userCoords = [$userLat, $userLng];

        // Initialize Google Maps style layers
        // Google Maps Standard Roadmap Tiles
        roadLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
            maxZoom: 20,
            subdomains: ['mt0', 'mt1', 'mt2', 'mt3'],
            attribution: '© Google Maps'
        });

        // Google Maps Hybrid Satellite Tiles
        satLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}', {
            maxZoom: 20,
            subdomains: ['mt0', 'mt1', 'mt2', 'mt3'],
            attribution: '© Google Maps Satellite'
        });

        // Initialize Leaflet Map
        map = L.map('map', {
            center: userCoords,
            zoom: 13,
            layers: [${if (isSatellite) "satLayer" else "roadLayer"}],
            zoomControl: true
        });

        markersGroup = L.layerGroup().addTo(map);

        // Add User Location Beacon
        var userIcon = L.divIcon({
            className: 'custom-user-marker',
            html: '<div class="user-beacon"><div class="user-beacon-center"></div><div class="user-beacon-pulse"></div></div>',
            iconSize: [22, 22],
            iconAnchor: [11, 11]
        });

        userMarker = L.marker(userCoords, { icon: userIcon, zIndexOffset: 1000 }).addTo(map);
        userMarker.bindPopup("<b>📍 Votre position actuelle</b><br>Bamako, Mali");

        // Function to create marker icon based on category
        function getPinIcon(category) {
            var pinClass = 'pin-other';
            var iconChar = '🚗';
            if (category === 'MECANICIEN') {
                pinClass = 'pin-mechanic';
                iconChar = '🛠️';
            } else if (category === 'PIECES') {
                pinClass = 'pin-parts';
                iconChar = '🏬';
            } else if (category === 'PNEUMATIQUE') {
                pinClass = 'pin-tires';
                iconChar = '🛞';
            }

            return L.divIcon({
                className: 'custom-gmap-marker',
                html: '<div class="gmap-pin ' + pinClass + '"><div class="gmap-pin-inner">' + iconChar + '</div></div>',
                iconSize: [38, 38],
                iconAnchor: [19, 38],
                popupAnchor: [0, -38]
            });
        }

        // Render markers from services list
        function renderMarkers(services) {
            markersGroup.clearLayers();
            services.forEach(function(item) {
                if (item.lat && item.lng) {
                    var marker = L.marker([item.lat, item.lng], {
                        icon: getPinIcon(item.category),
                        title: item.name
                    });

                    var popupHtml = '<div class="popup-card">' +
                        '<div class="popup-title">' + item.name + '</div>' +
                        '<div class="popup-cat" style="background:#EFF6FF; color:#1E3A8A;">' + item.categoryTitle + '</div>' +
                        '<div>⭐ <span class="popup-rating">' + item.rating + '</span> • ' + item.city + '</div>' +
                        (item.servicesOffered ? '<div style="font-size:11px;color:#64748B;margin-top:2px;">' + item.servicesOffered + '</div>' : '') +
                        '<a href="#" class="popup-btn" onclick="onPopupAction(\'' + item.id + '\', ' + item.lat + ', ' + item.lng + ', \'' + item.name.replace(/'/g, "\\'") + '\'); return false;">🧭 Lancer Itinéraire GPS</a>' +
                        '</div>';

                    marker.bindPopup(popupHtml);

                    marker.on('click', function() {
                        if (window.AndroidBridge && window.AndroidBridge.onMarkerClicked) {
                            window.AndroidBridge.onMarkerClicked(item.id);
                        }
                    });

                    marker._providerId = item.id;
                    markersGroup.addLayer(marker);
                }
            });
        }

        function onPopupAction(id, lat, lng, name) {
            if (window.AndroidBridge && window.AndroidBridge.openDirections) {
                window.AndroidBridge.openDirections(lat, lng, name);
            }
        }

        // Exposed functions called by Android Jetpack Compose
        window.updateMarkersData = function(services) {
            currentServices = services;
            renderMarkers(services);
        };

        window.focusMarker = function(id, lat, lng) {
            map.flyTo([lat, lng], 16, { duration: 0.8 });
            markersGroup.eachLayer(function(layer) {
                if (layer._providerId === id) {
                    layer.openPopup();
                }
            });
        };

        window.setMapType = function(isSatellite) {
            if (isSatellite) {
                map.removeLayer(roadLayer);
                map.addLayer(satLayer);
            } else {
                map.removeLayer(satLayer);
                map.addLayer(roadLayer);
            }
        };

        window.setUserLocation = function(lat, lng) {
            userCoords = [lat, lng];
            if (userMarker) {
                userMarker.setLatLng(userCoords);
            }
        };

        window.centerOnUser = function() {
            map.flyTo(userCoords, 14, { duration: 0.8 });
            if (userMarker) {
                userMarker.openPopup();
            }
        };

        // Initial render
        renderMarkers(currentServices);
    </script>
</body>
</html>
    """.trimIndent()
}
