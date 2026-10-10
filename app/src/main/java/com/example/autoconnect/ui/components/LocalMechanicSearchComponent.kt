package com.example.autoconnect.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.data.service.FirestoreMechanicSearchService
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.theme.BlueSecondary
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

/**
 * Reusable, high-fidelity UI Component to search for local mechanics in Mali
 * with real-time Firestore synchronization and local fallback.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocalMechanicSearchComponent(
    modifier: Modifier = Modifier,
    onMechanicSelected: (ServiceProvider) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToBooking: (ServiceProvider) -> Unit,
    onNavigateToMap: (ServiceProvider) -> Unit
) {
    val context = LocalContext.current
    val searchService = remember { FirestoreMechanicSearchService(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("Toutes") }
    var onlyOpenNow by remember { mutableStateOf(false) }
    var minRating by remember { mutableDoubleStateOf(0.0) }
    var selectedSpecialty by remember { mutableStateOf("Tous") }

    var isLoading by remember { mutableStateOf(true) }
    var mechanicsList by remember { mutableStateOf<List<ServiceProvider>>(emptyList()) }
    var isFirestoreConnected by remember { mutableStateOf(true) }

    val maliCities = remember { searchService.getMaliCities() }
    val specialties = remember { searchService.getSpecialties() }

    // Real-time Firestore search trigger
    LaunchedEffect(searchQuery, selectedCity, onlyOpenNow, minRating, selectedSpecialty) {
        isLoading = true
        val combinedQuery = if (selectedSpecialty != "Tous") {
            if (searchQuery.isNotBlank()) "$searchQuery $selectedSpecialty" else selectedSpecialty
        } else {
            
            searchQuery

        }

        searchService.searchMechanicsRealtime(
            query = combinedQuery,
            city = if (selectedCity == "Toutes") null else selectedCity,
            onlyOpen = onlyOpenNow,
            minRating = minRating
        ).collectLatest { result ->
            isLoading = false
            result.onSuccess { list ->
                mechanicsList = list
                isFirestoreConnected = true
            }.onFailure {
                isFirestoreConnected = false
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        val isWide = maxWidth >= 600.dp
        val contentModifier = if (isWide) {
            Modifier
                .widthIn(max = 840.dp)
                .align(Alignment.TopCenter)
        } else {
            Modifier.fillMaxWidth()
        }

        Column(
            modifier = contentModifier.fillMaxSize()
        ) {
        // Top Firestore Search Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header & Firestore live badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BluePrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Handyman,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Artisans & Ateliers du Mali",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Professionnels certifiés • Contact direct • Suivi en direct",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Live Status Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isFirestoreConnected) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isFirestoreConnected) Color(0xFF16A34A) else Color(0xFFD97706))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isFirestoreConnected) "Cloud Actif" else "Hors-ligne",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFirestoreConnected) Color(0xFF15803D) else Color(0xFFB45309)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Modern Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Rechercher un garage, mécanicien, quartier...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = BluePrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firestore_mechanic_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mali Cities Horizontal Filter Pills
                Text("Villes & Régions :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(maliCities) { city ->
                        val isSelected = selectedCity == city
                        Surface(
                            onClick = { selectedCity = city },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) BluePrimary else Color(0xFFF1F5F9),
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
                        ) {
                            Text(
                                text = city,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Specialty Quick Chips
                Text("Prestations & Pannes :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(specialties) { specialty ->
                        val isSelected = selectedSpecialty == specialty
                        Surface(
                            onClick = { selectedSpecialty = specialty },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF0369A1) else Color(0xFFF1F5F9),
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
                        ) {
                            Text(
                                text = specialty,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Quick Filters: Open now & High Rating - Scrollable/Adaptive
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = onlyOpenNow,
                            onClick = { onlyOpenNow = !onlyOpenNow },
                            label = { Text("Ouvert maintenant", fontSize = 11.sp) },
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
                            label = { Text("⭐ 4.5+ étoiles", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = Color(0xFF92400E)
                            )
                        )
                    }
                }
            }
        }

        // Search Results Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${mechanicsList.size} mécaniciens trouvés dans le Cloud",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BluePrimary)
            }
        }

        // Mechanics List or Empty State
        if (mechanicsList.isEmpty() && !isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                                Icons.Default.Handyman,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Aucun mécanicien ne correspond à votre recherche",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vérifiez l'orthographe ou réinitialisez les filtres pour afficher tous les ateliers mécaniques répertoriés au Mali.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedCity = "Toutes"
                            selectedSpecialty = "Tous"
                            onlyOpenNow = false
                            minRating = 0.0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Réinitialiser les filtres", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mechanicsList, key = { it.id }) { mechanic ->
                    MaliMechanicCard(
                        mechanic = mechanic,
                        onCardClick = { onMechanicSelected(mechanic) },
                        onCallClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${mechanic.phone}"))
                            context.startActivity(intent)
                        },
                        onChatClick = { onNavigateToChat(mechanic.id) },
                        onBookingClick = { onNavigateToBooking(mechanic) },
                        onItineraryClick = { onNavigateToMap(mechanic) }
                    )
                }
            }
        }
    }
}
}

/**
 * Individual Mali Mechanic Card with verified badges, rating, services, and direct CUJ actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaliMechanicCard(
    mechanic: ServiceProvider,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onChatClick: () -> Unit,
    onBookingClick: () -> Unit,
    onItineraryClick: () -> Unit
) {
    val distanceKm = remember(mechanic) {
        String.format(Locale.US, "%.1f km", mechanic.getDistance(12.6392, -8.0029))
    }

    val servicesList = remember(mechanic.servicesOffered) {
        mechanic.servicesOffered?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("mali_mechanic_card_${mechanic.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Verified Badge, Rating, Open Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mechanic.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Garage Vérifié",
                            tint = BluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${mechanic.city} • $distanceKm",
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Rating Badge
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", mechanic.rating),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            if (mechanic.description.isNotBlank()) {
                Text(
                    text = mechanic.description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Hours, Open Status & Community Feedback badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (mechanic.isOpen) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (mechanic.isOpen) "OUVERT" else "FERMÉ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mechanic.isOpen) Color(0xFF15803D) else Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                mechanic.hours?.let { hours ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = hours,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Community Trust Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Avis certifiés",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    }
                }
            }

            // Service tags chips
            if (servicesList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    servicesList.take(3).forEach { serviceTag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFBFDBFE))
                        ) {
                            Text(
                                text = serviceTag,
                                fontSize = 10.sp,
                                color = BluePrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (servicesList.size > 3) {
                        Text(
                            text = "+${servicesList.size - 3} autres",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: Call, Chat, Appointment, Map
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call
                Button(
                    onClick = onCallClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Appeler", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Appel", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // Chat
                OutlinedButton(
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Message", tint = BluePrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // Book Appointment
                Button(
                    onClick = onBookingClick,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "RDV", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("RDV", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // GPS Map
                IconButton(
                    onClick = onItineraryClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(Icons.Default.NearMe, contentDescription = "Itinéraire GPS", tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
