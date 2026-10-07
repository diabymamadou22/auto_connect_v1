package com.example.autoconnect.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.theme.EmergencyRed
import org.json.JSONArray
import org.json.JSONObject

/**
 * Filter mode for the Nearby Map Component
 */
enum class MapTargetFilter(val label: String, val category: ServiceCategory?) {
    ALL("Tous", null),
    MECHANICS("🔧 Mécaniciens", ServiceCategory.MECANICIEN),
    PARTS("🔩 Boutiques Pièces", ServiceCategory.PIECES),
    TIRES("🛞 Pneus / Vulca", ServiceCategory.PNEUMATIQUE),
    OTHER("✨ Autres services", ServiceCategory.AUTRE)
}

/**
 * A reusable, interactive Map Component for visualizing nearby mechanics
 * and spare parts shops on Google Maps tiles with custom markers, search,
 * GPS location, and a detailed bottom card.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NearbyMapComponent(
    modifier: Modifier = Modifier,
    services: List<ServiceProvider>,
    onNavigateToDetail: (ServiceProvider) -> Unit,
    onExpandFullScreen: (() -> Unit)? = null,
    isEmbedded: Boolean = false,
    initialTargetFilter: MapTargetFilter = MapTargetFilter.ALL
) {
    val context = LocalContext.current

    // Reference position: Bamako, Mali (12.6392, -8.0029)
    var userLat by remember { mutableDoubleStateOf(12.6392) }
    var userLng by remember { mutableDoubleStateOf(-8.0029) }
    var isLocating by remember { mutableStateOf(false) }

    var selectedFilter by remember { mutableStateOf(initialTargetFilter) }
    var searchQuery by remember { mutableStateOf("") }
    var isSatelliteMode by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<ServiceProvider?>(null) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

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
                    webViewInstance?.evaluateJavascript("setUserLocation(${loc.latitude}, ${loc.longitude});", null)
                } else {
                    Toast.makeText(context, "Position actuelle : Bamako, Mali", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Position par défaut : Bamako", Toast.LENGTH_SHORT).show()
            }
        }
        isLocating = false
    }

    fun requestGpsCenter() {
        isLocating = true
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Filtered services
    val displayedServices = remember(services, selectedFilter, searchQuery, userLat, userLng) {
        services.filter { item ->
            val matchesCategory = when (selectedFilter) {
                MapTargetFilter.ALL -> true
                else -> item.category == selectedFilter.category
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                item.name.contains(searchQuery, ignoreCase = true) ||
                        item.city.contains(searchQuery, ignoreCase = true) ||
                        item.description.contains(searchQuery, ignoreCase = true) ||
                        (item.servicesOffered?.contains(searchQuery, ignoreCase = true) == true)
            }
            matchesCategory && matchesSearch
        }.sortedBy { it.getDistance(userLat, userLng) }
    }

    val mechanicsCount = remember(services) {
        services.count { it.category == ServiceCategory.MECANICIEN }
    }
    val partsCount = remember(services) {
        services.count { it.category == ServiceCategory.PIECES }
    }

    // Push updated markers whenever filtered list changes
    LaunchedEffect(displayedServices) {
        webViewInstance?.let { webView ->
            val jsonArray = buildServicesJson(displayedServices)
            webView.evaluateJavascript("updateMarkersData($jsonArray);", null)
        }
    }

    Surface(
        modifier = modifier.testTag("nearby_map_component"),
        shape = if (isEmbedded) RoundedCornerShape(20.dp) else RoundedCornerShape(0.dp),
        color = Color(0xFFF1F5F9),
        shadowElevation = if (isEmbedded) 4.dp else 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Leaflet HTML WebView
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        // Prevent Mesa driver from failing to query GPU rendernodes in containerized / headless emulators
                        try {
                            setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                        } catch (_: Throwable) {}
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                val jsonArray = buildServicesJson(displayedServices)
                                view?.evaluateJavascript("updateMarkersData($jsonArray);", null)
                            }
                        }

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onMarkerClick(providerId: String) {
                                post {
                                    val found = services.find { it.id == providerId }
                                    if (found != null) {
                                        selectedProvider = found
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun onMapClicked() {
                                post {
                                    selectedProvider = null
                                }
                            }
                        }, "AndroidBridge")

                        val html = buildNearbyMapHtml(displayedServices, userLat, userLng, isSatelliteMode)
                        loadDataWithBaseURL("https://google.com/maps", html, "text/html", "UTF-8", null)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_webview_container"),
                onRelease = { webView ->
                    try {
                        webView.stopLoading()
                        webView.destroy()
                    } catch (_: Throwable) {}
                }
            )

            // Top Overlay: Filter Chips & Search Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Search bar and quick counters
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Recherche",
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text("Rechercher garage, pièces, quartier...", fontSize = 12.sp, color = Color.Gray)
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Fullscreen expand button if embedded
                        if (isEmbedded && onExpandFullScreen != null) {
                            IconButton(
                                onClick = onExpandFullScreen,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Plein écran", tint = BluePrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips Row (Focus on Mechanics & Parts)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MapTargetFilter.values()) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) BluePrimary else Color.White.copy(alpha = 0.92f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) BluePrimary else Color(0xFFCBD5E1)
                            ),
                            shadowElevation = 2.dp,
                            modifier = Modifier.clickable {
                                selectedFilter = filter
                                selectedProvider = null
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = filter.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF1E293B)
                                )
                                val badgeCount = when (filter) {
                                    MapTargetFilter.ALL -> services.size
                                    MapTargetFilter.MECHANICS -> mechanicsCount
                                    MapTargetFilter.PARTS -> partsCount
                                    else -> services.count { it.category == filter.category }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = "$badgeCount",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Map Control Floating Buttons (Right Side: Satellite & GPS re-center)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Layer Switch (Plan / Satellite)
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable {
                            isSatelliteMode = !isSatelliteMode
                            webViewInstance?.evaluateJavascript("setMapType($isSatelliteMode);", null)
                            Toast.makeText(
                                context,
                                if (isSatelliteMode) "Vue Satellite" else "Vue Plan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSatelliteMode) Icons.Default.Map else Icons.Default.Layers,
                            contentDescription = "Type de carte",
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // GPS Re-center
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable {
                            requestGpsCenter()
                            webViewInstance?.evaluateJavascript("centerOnUser();", null)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Centrer GPS",
                            tint = if (isLocating) EmergencyRed else Color(0xFF059669),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom Selected Establishment Card (Slide-up)
            AnimatedVisibility(
                visible = selectedProvider != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 540.dp)
                    .padding(12.dp)
            ) {
                val provider = selectedProvider
                if (provider != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetail(provider) }
                            .testTag("selected_provider_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Name, Category, Close
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (provider.category == ServiceCategory.PIECES) Color(0xFFDCFCE7) else Color(0xFFDBEAFE),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (provider.category == ServiceCategory.PIECES) Icons.Default.Storefront else Icons.Default.Build,
                                                contentDescription = null,
                                                tint = if (provider.category == ServiceCategory.PIECES) Color(0xFF16A34A) else BluePrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = provider.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (provider.category == ServiceCategory.PIECES) Color(0xFFECFDF5) else Color(0xFFEFF6FF),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = provider.category.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (provider.category == ServiceCategory.PIECES) Color(0xFF047857) else BluePrimary,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${provider.getDistance(userLat, userLng)} km",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { selectedProvider = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Rating and City
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${provider.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(provider.city, fontSize = 11.5.sp, color = Color(0xFF475569))

                                if (provider.isOpen) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                                        Text("Ouvert", color = Color(0xFF166534), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
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

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons (Call, Directions, Details)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Directions Button
                                Button(
                                    onClick = {
                                        val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${provider.latitude},${provider.longitude}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        try {
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Ouverture GPS...", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Itinéraire", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                // Call Button
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
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Appeler", modifier = Modifier.size(15.dp), tint = Color(0xFF16A34A))
                                    }
                                }

                                // Details Button
                                Button(
                                    onClick = { onNavigateToDetail(provider) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Détails", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
 * Builds JSON payload of providers to send to the Google Maps WebView.
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
 * Generates the Leaflet + Google Maps tiles HTML for nearby mechanics and spare parts shops.
 */
private fun buildNearbyMapHtml(
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
    <title>AutoConnect Mali Map</title>
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
        .gmap-pin {
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50% 50% 50% 0;
            transform: rotate(-45deg);
            width: 36px;
            height: 36px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.35);
            border: 2px solid #ffffff;
            cursor: pointer;
            transition: transform 0.2s ease;
        }
        .gmap-pin:active {
            transform: rotate(-45deg) scale(1.15);
        }
        .gmap-pin-inner {
            transform: rotate(45deg);
            font-size: 15px;
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

        roadLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
            maxZoom: 20,
            subdomains: ['mt0', 'mt1', 'mt2', 'mt3'],
            attribution: '© Google Maps'
        });

        satLayer = L.tileLayer('https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}', {
            maxZoom: 20,
            subdomains: ['mt0', 'mt1', 'mt2', 'mt3'],
            attribution: '© Google Maps'
        });

        map = L.map('map', {
            center: userCoords,
            zoom: 13,
            zoomControl: false,
            layers: [${if (isSatellite) "satLayer" else "roadLayer"}]
        });

        L.control.zoom({ position: 'bottomright' }).addTo(map);

        map.on('click', function() {
            if (window.AndroidBridge && window.AndroidBridge.onMapClicked) {
                window.AndroidBridge.onMapClicked();
            }
        });

        var userIcon = L.divIcon({
            className: 'user-beacon',
            html: '<div class="user-beacon-pulse"></div><div class="user-beacon-center"></div>',
            iconSize: [22, 22],
            iconAnchor: [11, 11]
        });
        userMarker = L.marker(userCoords, { icon: userIcon, zIndexOffset: 1000 }).addTo(map);

        markersGroup = L.layerGroup().addTo(map);

        function createPinIcon(category) {
            var pinClass = 'pin-other';
            var iconSymbol = '⚙️';
            if (category === 'MECANICIEN') {
                pinClass = 'pin-mechanic';
                iconSymbol = '🔧';
            } else if (category === 'PIECES') {
                pinClass = 'pin-parts';
                iconSymbol = '🔩';
            } else if (category === 'VULCANISATEUR') {
                pinClass = 'pin-tires';
                iconSymbol = '🛞';
            } else if (category === 'CARROSSIER') {
                pinClass = 'pin-other';
                iconSymbol = '🚗';
            }

            return L.divIcon({
                className: 'custom-pin-container',
                html: '<div class="gmap-pin ' + pinClass + '"><div class="gmap-pin-inner">' + iconSymbol + '</div></div>',
                iconSize: [36, 36],
                iconAnchor: [18, 36]
            });
        }

        function renderMarkers(services) {
            markersGroup.clearLayers();
            var bounds = L.latLngBounds([userCoords]);

            services.forEach(function(s) {
                var icon = createPinIcon(s.category);
                var marker = L.marker([s.lat, s.lng], { icon: icon });

                marker.on('click', function(e) {
                    L.DomEvent.stopPropagation(e);
                    if (window.AndroidBridge && window.AndroidBridge.onMarkerClick) {
                        window.AndroidBridge.onMarkerClick(s.id);
                    }
                    map.panTo([s.lat, s.lng], { animate: true, duration: 0.5 });
                });

                markersGroup.addLayer(marker);
                bounds.extend([s.lat, s.lng]);
            });

            if (services.length > 0) {
                map.fitBounds(bounds, { padding: [40, 40], maxZoom: 15 });
            }
        }

        renderMarkers(currentServices);

        function updateMarkersData(newServices) {
            currentServices = newServices;
            renderMarkers(currentServices);
        }

        function setMapType(isSat) {
            if (isSat) {
                map.removeLayer(roadLayer);
                map.addLayer(satLayer);
            } else {
                map.removeLayer(satLayer);
                map.addLayer(roadLayer);
            }
        }

        function setUserLocation(lat, lng) {
            userCoords = [lat, lng];
            if (userMarker) {
                userMarker.setLatLng(userCoords);
            }
            map.panTo(userCoords, { animate: true });
        }

        function centerOnUser() {
            map.flyTo(userCoords, 14, { animate: true });
        }
    </script>
</body>
</html>
""".trimIndent()
}
