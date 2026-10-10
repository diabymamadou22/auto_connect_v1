package com.example.autoconnect.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autoconnect.data.local.BookingEntity
import com.example.autoconnect.data.model.OfferedService
import com.example.autoconnect.data.model.Review
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.viewmodel.AuthViewModel
import com.example.autoconnect.ui.viewmodel.ServicesViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(
    provider: ServiceProvider,
    authViewModel: AuthViewModel,
    servicesViewModel: ServicesViewModel,
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToMap: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val reviewsFlow = remember(provider.id) { servicesViewModel.getReviewsForService(provider.id) }
    val reviews by reviewsFlow.collectAsState()

    val offeredServicesFlow = remember(provider.id) { servicesViewModel.getOfferedServicesForProvider(provider.id) }
    val offeredServices by offeredServicesFlow.collectAsState()

    var selectedServiceForBooking by remember { mutableStateOf<OfferedService?>(null) }
    var showBookingSheet by remember { mutableStateOf(false) }
    val bookingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var userRating by remember { mutableDoubleStateOf(5.0) }
    var commentText by remember { mutableStateOf("") }
    var filterStar by remember { mutableStateOf<Int?>(null) } // null = All stars

    val displayedReviews = remember(reviews, filterStar) {
        if (filterStar == null) reviews
        else reviews.filter { Math.round(it.rating).toInt() == filterStar }
    }

    val imageUrl = when (provider.category) {
        ServiceCategory.PIECES -> "https://images.unsplash.com/photo-1507136566006-cfc505b114fc?auto=format&fit=crop&q=80&w=600"
        ServiceCategory.MECANICIEN -> "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&q=80&w=600"
        ServiceCategory.PNEUMATIQUE -> "https://images.unsplash.com/photo-1578844251758-2f71da64c96f?auto=format&fit=crop&q=80&w=600"
        ServiceCategory.AUTRE -> "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&q=80&w=600"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(provider.name, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    if (onNavigateToMap != null) {
                        IconButton(onClick = onNavigateToMap) {
                            Icon(Icons.Default.Map, contentDescription = "Carte Garages", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { servicesViewModel.toggleFavorite(provider.id) }) {
                        Icon(
                            imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (provider.isFavorite) Color.Red else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWide = maxWidth >= 600.dp
            val isNarrow = maxWidth < 360.dp
            val contentModifier = if (isWide) {
                Modifier
                    .widthIn(max = 760.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier.fillMaxWidth()
            }

            LazyColumn(
                modifier = contentModifier.fillMaxSize()
            ) {
            // Hero Image Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = provider.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = provider.category.color,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = provider.category.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Info Summary Header
            item {
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = provider.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                color = if (provider.isOpen) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (provider.isOpen) Color(0xFF4CAF50) else Color(0xFFF44336))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (provider.isOpen) "OUVERT" else "FERMÉ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (provider.isOpen) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${provider.rating} / 5.0",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(provider.city, color = Color.Gray, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Action Buttons (Call, WhatsApp, Map, Chat) - Adaptive for all screens
                        if (isNarrow) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ActionButton(
                                        modifier = Modifier.weight(1f),
                                        title = "Appeler",
                                        icon = Icons.Default.Call,
                                        color = Color(0xFF4CAF50),
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                            context.startActivity(intent)
                                        }
                                    )
                                    ActionButton(
                                        modifier = Modifier.weight(1f),
                                        title = "WhatsApp",
                                        icon = Icons.Default.Chat,
                                        color = Color(0xFF25D366),
                                        onClick = {
                                            val cleanNumber = provider.phone.replace(" ", "").replace("+", "")
                                            val url = "https://wa.me/$cleanNumber"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "WhatsApp non installé", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ActionButton(
                                        modifier = Modifier.weight(1f),
                                        title = "Itinéraire",
                                        icon = Icons.Default.Navigation,
                                        color = Color(0xFF7C3AED),
                                        onClick = {
                                            val uri = Uri.parse("geo:${provider.latitude},${provider.longitude}?q=${provider.latitude},${provider.longitude}(${Uri.encode(provider.name)})")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        }
                                    )
                                    ActionButton(
                                        modifier = Modifier.weight(1f),
                                        title = "Message",
                                        icon = Icons.Default.Chat,
                                        color = BluePrimary,
                                        onClick = {
                                            onNavigateToChat(provider.id)
                                        }
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    title = "Appeler",
                                    icon = Icons.Default.Call,
                                    color = Color(0xFF4CAF50),
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                        context.startActivity(intent)
                                    }
                                )

                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    title = "WhatsApp",
                                    icon = Icons.Default.Chat,
                                    color = Color(0xFF25D366),
                                    onClick = {
                                        val cleanNumber = provider.phone.replace(" ", "").replace("+", "")
                                        val url = "https://wa.me/$cleanNumber"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "WhatsApp non installé", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )

                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    title = "Itinéraire",
                                    icon = Icons.Default.Navigation,
                                    color = Color(0xFF7C3AED),
                                    onClick = {
                                        val uri = Uri.parse("geo:${provider.latitude},${provider.longitude}?q=${provider.latitude},${provider.longitude}(${Uri.encode(provider.name)})")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    }
                                )

                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    title = "Message",
                                    icon = Icons.Default.Chat,
                                    color = BluePrimary,
                                    onClick = {
                                        onNavigateToChat(provider.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION: Prestations & Tarifs proposés par le Prestataire
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Prestations & Tarifs",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Surface(
                                color = BluePrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${offeredServices.size} service(s)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Sélectionnez une prestation pour réserver votre créneau directement :",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        val formatter = remember { NumberFormat.getNumberInstance(Locale.FRANCE) }

                        if (offeredServices.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "🔧 Prestation Standard & Diagnostic",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (!provider.servicesOffered.isNull_or_blank()) "Prestations annoncées : ${provider.servicesOffered}" else "Révision, diagnostic valise et dépannage sur devis.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            selectedServiceForBooking = OfferedService(
                                                id = "custom_${provider.id}",
                                                providerId = provider.id,
                                                providerName = provider.name,
                                                title = "Révision & Diagnostic Général",
                                                description = "Demande d'intervention pour mon véhicule",
                                                priceCfa = 15000
                                            )
                                            showBookingSheet = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("📅 Prendre Rendez-vous", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                offeredServices.forEach { service ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = service.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = Color(0xFF0F172A),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Surface(
                                                            color = BluePrimary.copy(alpha = 0.1f),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = service.category,
                                                                color = BluePrimary,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                                                            Spacer(modifier = Modifier.width(2.dp))
                                                            Text(service.durationMinutes, fontSize = 11.sp, color = Color.Gray)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${formatter.format(service.priceCfa)} F",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp,
                                                    color = Color(0xFF059669),
                                                    maxLines = 1
                                                )
                                            }

                                            if (service.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = service.description,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Button(
                                                onClick = {
                                                    selectedServiceForBooking = service
                                                    showBookingSheet = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Réserver cette prestation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Description & Details Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Informations sur le service", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(provider.description, color = Color(0xFF4B5563), fontSize = 14.sp)

                        if (!provider.hours.isNull_or_blank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Horaires: ${provider.hours}", fontSize = 13.sp, color = Color(0xFF374151))
                            }
                        }

                        if (!provider.servicesOffered.isNull_or_blank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Prestations: ${provider.servicesOffered}", fontSize = 13.sp, color = Color(0xFF374151))
                            }
                        }
                    }
                }
            }

            // Map & GPS Location Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Carte & Localisation GPS", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                            }
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = provider.city,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Localisation : ${provider.city} • GPS: ${String.format(Locale.US, "%.4f", provider.latitude)}, ${String.format(Locale.US, "%.4f", provider.longitude)}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onNavigateToMap != null) {
                                Button(
                                    onClick = onNavigateToMap,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Voir Carte", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            Button(
                                onClick = {
                                    val uri = Uri.parse("geo:${provider.latitude},${provider.longitude}?q=${provider.latitude},${provider.longitude}(${Uri.encode(provider.name)})")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lancer GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }

            // Rating & Reviews Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Évaluations et Avis Clients", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Average Big Score Box
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1f", provider.rating),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                                Row {
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (i <= Math.round(provider.rating)) Color(0xFFFFB300) else Color(0xFFE2E8F0),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${reviews.size} avis",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(70.dp)
                                    .width(1.dp),
                                color = Color(0xFFE2E8F0)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Distribution Bars (5 to 1 stars)
                            Column(modifier = Modifier.weight(1f)) {
                                val totalCount = if (reviews.isEmpty()) 1 else reviews.size
                                for (star in 5 downTo 1) {
                                    val count = reviews.count { Math.round(it.rating).toInt() == star }
                                    val fraction = count.toFloat() / totalCount

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    ) {
                                        Text("${star}★", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.width(22.dp))
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE2E8F0))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFFB300))
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("$count", fontSize = 10.sp, color = Color(0xFF94A3B8), modifier = Modifier.width(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Add Review Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Donner votre avis sur ce prestataire", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                        Text("Partagez votre expérience avec la communauté AutoConnect", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Star selector & Label
                        val ratingText = when (userRating.toInt()) {
                            5 -> "5.0 - Excellent 🌟"
                            4 -> "4.0 - Très Bon 👍"
                            3 -> "3.0 - Correct 👌"
                            2 -> "2.0 - Moyen 😐"
                            else -> "1.0 - Décevant 👎"
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                for (i in 1..5) {
                                    IconButton(
                                        onClick = { userRating = i.toDouble() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (i <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "$i Étoiles",
                                            tint = if (i <= userRating) Color(0xFFFFB300) else Color(0xFFCBD5E1),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = ratingText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Feedback Tag Chips
                        Text("Mots-clés rapides :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val quickTags = listOf(
                            "⚡ Service Rapide",
                            "💰 Prix Abordable",
                            "👨‍🔧 Mécanicien Qualifié",
                            "🛠️ Travail Garanti",
                            "🤝 Excellent Accueil"
                        )

                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickTags) { tag ->
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.clickable {
                                        if (!commentText.contains(tag)) {
                                            commentText = if (commentText.isBlank()) tag else "$commentText $tag"
                                        }
                                    }
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BluePrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Décrivez le travail réalisé, la qualité de l'accueil, les tarifs...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (commentText.isBlank()) {
                                    Toast.makeText(context, "Veuillez rédiger votre commentaire ou choisir un mot-clé", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val newReview = Review(
                                    id = UUID.randomUUID().toString(),
                                    serviceId = provider.id,
                                    userName = currentUser?.username ?: "Client AutoConnect",
                                    comment = commentText.trim(),
                                    rating = userRating,
                                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                                )
                                servicesViewModel.addReview(newReview)
                                commentText = ""
                                Toast.makeText(context, "Avis et note envoyés avec succès !", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publier l'avis", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Reviews List Header & Star Filters
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Avis des clients (${reviews.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        if (filterStar != null) {
                            TextButton(onClick = { filterStar = null }) {
                                Text("Tous les avis", fontSize = 12.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Star Filter Row
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Surface(
                                onClick = { filterStar = null },
                                shape = RoundedCornerShape(12.dp),
                                color = if (filterStar == null) BluePrimary else Color(0xFFF1F5F9),
                                border = if (filterStar != null) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
                            ) {
                                Text(
                                    text = "Tous (${reviews.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (filterStar == null) FontWeight.Bold else FontWeight.Medium,
                                    color = if (filterStar == null) Color.White else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        for (star in 5 downTo 1) {
                            val count = reviews.count { Math.round(it.rating).toInt() == star }
                            if (count > 0 || reviews.isNotEmpty()) {
                                item {
                                    val isSelected = filterStar == star
                                    Surface(
                                        onClick = { filterStar = if (isSelected) null else star },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(0xFFD97706) else Color(0xFFF1F5F9),
                                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else Color(0xFFFFB300),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "$star ($count)",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Color(0xFF475569)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (displayedReviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filterStar != null) "Aucun avis avec $filterStar étoiles pour le moment." else "Aucun avis pour l'instant. Soyez le premier à noter ce garage !",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(displayedReviews, key = { it.id }) { review ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = BluePrimary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = review.userName.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = BluePrimary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(review.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                        Text(review.createdAt, fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFFFFFBEB),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(review.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB45309))
                                        }
                                    }

                                    if (currentUser?.role == "admin" || currentUser?.username == review.userName) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                servicesViewModel.deleteReview(review.id, provider.id)
                                                Toast.makeText(context, "Avis supprimé", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(review.comment, fontSize = 13.sp, color = Color(0xFF334155))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        if (showBookingSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBookingSheet = false },
                sheetState = bookingSheetState,
                containerColor = Color.White
            ) {
                ClientBookingSheetContent(
                    provider = provider,
                    service = selectedServiceForBooking,
                    currentUserName = currentUser?.username ?: "Client AutoConnect",
                    onConfirm = { booking ->
                        servicesViewModel.addBooking(booking) {
                            Toast.makeText(context, "Rendez-vous pour '${booking.serviceType}' envoyé au garage !", Toast.LENGTH_LONG).show()
                        }
                        showBookingSheet = false
                    },
                    onCancel = { showBookingSheet = false }
                )
            }
        }
    }
}
}

@Composable
fun ClientBookingSheetContent(
    provider: ServiceProvider,
    service: OfferedService?,
    currentUserName: String,
    onConfirm: (BookingEntity) -> Unit,
    onCancel: () -> Unit
) {
    var clientName by remember { mutableStateOf(currentUserName) }
    var clientPhone by remember { mutableStateOf("+223 ") }
    var vehicleModel by remember { mutableStateOf("Toyota Corolla") }
    var selectedDate by remember { mutableStateOf("Demain") }
    var selectedTimeSlot by remember { mutableStateOf("09:00 - 10:00") }
    var notes by remember { mutableStateOf("") }

    val serviceTitle = service?.title ?: "Révision générale"
    val priceText = service?.let { "${NumberFormat.getNumberInstance(Locale.FRANCE).format(it.priceCfa)} FCFA" } ?: "Sur devis"

    val dateOptions = listOf("Aujourd'hui", "Demain", "Après-demain", "Samedi prochain")
    val timeSlots = listOf("08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Réserver un Rendez-vous", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Chez ${provider.name} (${provider.city})", fontSize = 12.sp, color = BluePrimary, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Fermer")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Service Summary Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(serviceTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E3A8A))
                    service?.durationMinutes?.let {
                        Text("Durée estimée : $it", fontSize = 11.sp, color = Color(0xFF3B82F6))
                    }
                }
                Surface(
                    color = Color(0xFF059669),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = priceText,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = clientName,
            onValueChange = { clientName = it },
            label = { Text("Votre Nom complet") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = clientPhone,
            onValueChange = { clientPhone = it },
            label = { Text("Numéro de Téléphone (+223)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = vehicleModel,
            onValueChange = { vehicleModel = it },
            label = { Text("Véhicule (Marque & Modèle)") },
            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = BluePrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Jour souhaité :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dateOptions) { d ->
                val isSelected = selectedDate == d
                Surface(
                    modifier = Modifier.clickable { selectedDate = d },
                    color = if (isSelected) BluePrimary else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = d,
                        color = if (isSelected) Color.White else Color(0xFF334155),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Créneau horaire :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(timeSlots) { slot ->
                val isSelected = selectedTimeSlot == slot
                Surface(
                    modifier = Modifier.clickable { selectedTimeSlot = slot },
                    color = if (isSelected) Color(0xFF059669) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = slot,
                        color = if (isSelected) Color.White else Color(0xFF334155),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Précisions (symptôme, voyant allumé...)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Text("Annuler", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Button(
                onClick = {
                    if (clientPhone.isBlank() || clientPhone.length < 8) return@Button
                    val noteText = if (vehicleModel.isNotBlank()) "Véhicule: $vehicleModel. $notes" else notes
                    val booking = BookingEntity(
                        id = UUID.randomUUID().toString(),
                        providerId = provider.id,
                        providerName = provider.name,
                        serviceType = serviceTitle,
                        clientName = if (clientName.isBlank()) "Client AutoConnect" else clientName.trim(),
                        clientPhone = clientPhone.trim(),
                        date = selectedDate,
                        timeSlot = selectedTimeSlot,
                        status = "EN_ATTENTE",
                        notes = noteText.trim()
                    )
                    onConfirm(booking)
                },
                modifier = Modifier.weight(1.3f),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Confirmer le RDV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()
