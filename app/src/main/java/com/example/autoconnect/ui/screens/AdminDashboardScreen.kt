package com.example.autoconnect.ui.screens

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.model.Review
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.viewmodel.AuthViewModel
import com.example.autoconnect.ui.viewmodel.ServicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    servicesViewModel: ServicesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allServices by servicesViewModel.allServices.collectAsState()
    val allReviews by servicesViewModel.allReviews.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // State for Pro Account Creation
    var proUsername by remember { mutableStateOf("") }
    var proPassword by remember { mutableStateOf("") }
    var proServiceName by remember { mutableStateOf("") }
    var proCategory by remember { mutableStateOf(ServiceCategory.MECANICIEN) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var proCity by remember { mutableStateOf("Bamako") }
    var cityExpanded by remember { mutableStateOf(false) }
    var proPhone by remember { mutableStateOf("+223 ") }
    var proAddress by remember { mutableStateOf("") }
    var proDescription by remember { mutableStateOf("") }
    var proLatText by remember { mutableStateOf("12.6392") }
    var proLngText by remember { mutableStateOf("-8.0029") }

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
                    proLatText = String.format(java.util.Locale.US, "%.5f", loc.latitude)
                    proLngText = String.format(java.util.Locale.US, "%.5f", loc.longitude)
                    Toast.makeText(context, "Position GPS capturée avec succès !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Mise à jour GPS en cours... Réessayez dans un instant.", Toast.LENGTH_SHORT).show()
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

    // State for Admin Password Modification
    var newAdminPassword by remember { mutableStateOf("") }
    var confirmAdminPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val cities = listOf("Bamako", "Sikasso", "Ségou", "Mopti", "Gao", "Kayes", "Koutiala")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administration AutoConnect", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6A1B9A))
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F6FA))
        ) {
            val isWide = maxWidth >= 600.dp
            val contentModifier = if (isWide) {
                Modifier
                    .widthIn(max = 780.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(
                modifier = contentModifier.fillMaxSize()
            ) {
                // Stats Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF6A1B9A))
                ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Espace Contrôle & Sécurité Admin",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 17.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Compte Administrateur Sécurisé (•••••)",
                                color = Color(0xFFE1BEE7),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(modifier = Modifier.weight(1f), label = "Garages / Pros", value = allServices.size.toString())
                        AdminStatCard(modifier = Modifier.weight(1f), label = "Avis Clients", value = allReviews.size.toString())
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF6A1B9A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF6A1B9A)
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("GARAGES", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("+ COMPTE PRO", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("SÉCURITÉ", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: GARAGES ET AVIS
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Text("Gestion des Prestataires", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF6A1B9A))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(items = allServices, key = { it.id }) { provider: ServiceProvider ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(provider.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${provider.category.title} • ${provider.city} • ${provider.phone}", color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    IconButton(
                                        onClick = {
                                            servicesViewModel.deleteService(provider.id)
                                            Toast.makeText(context, "Prestataire supprimé", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Modération des Avis Clients", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF6A1B9A))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(items = allReviews, key = { it.id }) { review: Review ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(review.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(review.rating.toString(), fontWeight = FontWeight.Bold)
                                            IconButton(
                                                onClick = {
                                                    servicesViewModel.deleteReview(review.id, review.serviceId)
                                                    Toast.makeText(context, "Avis supprimé", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                                            }
                                        }
                                    }
                                    Text(review.comment, fontSize = 13.sp, color = Color(0xFF374151))
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: CRÉER COMPTE PRO (Exclusif Administrateur)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF6A1B9A), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Création d'un Compte Prestataire", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF6A1B9A))
                                }
                                Text("Seul l'Administrateur peut ajouter de nouveaux comptes professionnels.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Identifiants de Connexion Pro", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                OutlinedTextField(
                                    value = proUsername,
                                    onValueChange = { proUsername = it },
                                    label = { Text("Nom d'utilisateur Pro") },
                                    leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = proPassword,
                                    onValueChange = { proPassword = it },
                                    label = { Text("Mot de passe du Pro") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Informations Établissement / Garage", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                OutlinedTextField(
                                    value = proServiceName,
                                    onValueChange = { proServiceName = it },
                                    label = { Text("Nom du Garage / Établissement") },
                                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Category selector
                                ExposedDropdownMenuBox(
                                    expanded = categoryExpanded,
                                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = proCategory.title,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Catégorie de service") },
                                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = categoryExpanded,
                                        onDismissRequest = { categoryExpanded = false }
                                    ) {
                                        for (cat in ServiceCategory.values()) {
                                            DropdownMenuItem(
                                                text = { Text(cat.title) },
                                                onClick = {
                                                    proCategory = cat
                                                    categoryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // City selector
                                ExposedDropdownMenuBox(
                                    expanded = cityExpanded,
                                    onExpandedChange = { cityExpanded = !cityExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = proCity,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Ville") },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = cityExpanded,
                                        onDismissRequest = { cityExpanded = false }
                                    ) {
                                        for (c in cities) {
                                            DropdownMenuItem(
                                                text = { Text(c) },
                                                onClick = {
                                                    proCity = c
                                                    cityExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = proPhone,
                                    onValueChange = { proPhone = it },
                                    label = { Text("Téléphone (+223)") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = proAddress,
                                    onValueChange = { proAddress = it },
                                    label = { Text("Adresse / Quartier") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = proDescription,
                                    onValueChange = { proDescription = it },
                                    label = { Text("Description des services") },
                                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // GPS Coordinates Section
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
                                                Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
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
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669))
                                            ) {
                                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Capter GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Sélection rapide par quartier à Bamako :", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.height(6.dp))

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(districtPresets) { district ->
                                                Surface(
                                                    modifier = Modifier.clickable {
                                                        proAddress = "Bamako, ${district.first}"
                                                        proLatText = district.second.toString()
                                                        proLngText = district.third.toString()
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
                                                value = proLatText,
                                                onValueChange = { proLatText = it },
                                                label = { Text("Latitude", fontSize = 12.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )

                                            OutlinedTextField(
                                                value = proLngText,
                                                onValueChange = { proLngText = it },
                                                label = { Text("Longitude", fontSize = 12.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        if (proUsername.isBlank() || proPassword.isBlank() || proServiceName.isBlank()) {
                                            Toast.makeText(context, "Veuillez remplir le nom d'utilisateur, mot de passe et nom du garage", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        val lat = proLatText.toDoubleOrNull() ?: 12.6392
                                        val lng = proLngText.toDoubleOrNull() ?: -8.0029

                                        authViewModel.createProAccount(
                                            username = proUsername,
                                            password = proPassword,
                                            serviceName = proServiceName,
                                            category = proCategory,
                                            city = proCity,
                                            phone = proPhone,
                                            address = if (proAddress.isBlank()) "$proCity, Mali" else proAddress,
                                            description = if (proDescription.isBlank()) "Service professionnel automobile à $proCity." else proDescription,
                                            latitude = lat,
                                            longitude = lng
                                        ) { success ->
                                            if (success) {
                                                Toast.makeText(context, "Compte Pro '$proServiceName' créé avec GPS ($lat, $lng) !", Toast.LENGTH_LONG).show()
                                                // Reset form
                                                proUsername = ""
                                                proPassword = ""
                                                proServiceName = ""
                                                proAddress = ""
                                                proDescription = ""
                                                proLatText = "12.6392"
                                                proLngText = "-8.0029"
                                                selectedTabIndex = 0 // Return to list
                                            } else {
                                                Toast.makeText(context, "Erreur : Ce nom d'utilisateur existe déjà", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Créer le compte prestataire", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: SÉCURITÉ ADMIN (Changer ou supprimer mot de passe)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF6A1B9A), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Sécurité du Compte Admin", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF6A1B9A))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Gestion des Accès Administrateur", fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A), fontSize = 13.sp)
                                Text("Vous pouvez modifier votre mot de passe Admin ou supprimer la protection.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text("Nouveau Mot de Passe Admin", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                                OutlinedTextField(
                                    value = newAdminPassword,
                                    onValueChange = { newAdminPassword = it },
                                    label = { Text("Nouveau mot de passe") },
                                    leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = confirmAdminPassword,
                                    onValueChange = { confirmAdminPassword = it },
                                    label = { Text("Confirmer le mot de passe") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6A1B9A)) },
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (newAdminPassword.isBlank()) {
                                            Toast.makeText(context, "Veuillez entrer le nouveau mot de passe", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (newAdminPassword != confirmAdminPassword) {
                                            Toast.makeText(context, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        authViewModel.updateAdminPassword(newAdminPassword) { success ->
                                            if (success) {
                                                Toast.makeText(context, "Mot de passe Admin mis à jour avec succès !", Toast.LENGTH_LONG).show()
                                                newAdminPassword = ""
                                                confirmAdminPassword = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                                ) {
                                    Text("Modifier le mot de passe", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        authViewModel.updateAdminPassword("") { success ->
                                            if (success) {
                                                Toast.makeText(context, "Mot de passe Admin supprimé ! L'Admin peut se connecter sans mot de passe.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Icon(Icons.Default.KeyOff, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Supprimer le mot de passe Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Red, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

@Composable
fun AdminStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
