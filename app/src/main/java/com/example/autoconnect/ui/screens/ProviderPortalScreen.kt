package com.example.autoconnect.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.autoconnect.data.model.OfferedService
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.viewmodel.AuthViewModel
import com.example.autoconnect.ui.viewmodel.ServicesViewModel
import java.text.NumberFormat
import java.util.Locale
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
    val allOfferedServices by servicesViewModel.allOfferedServices.collectAsState()

    val isProUser = currentUser != null && (currentUser?.role == "prestataire" || currentUser?.role == "admin")

    // Find the current provider establishment
    val currentWorkshop = remember(allServices, currentUser) {
        if (currentUser?.role == "admin") {
            allServices.firstOrNull()
        } else {
            allServices.firstOrNull { it.id == currentUser?.id }
                ?: allServices.firstOrNull { it.isMine }
                ?: allServices.firstOrNull { it.name.contains(currentUser?.username ?: "", ignoreCase = true) }
        }
    }

    val workshopId = currentWorkshop?.id ?: "g1"
    val workshopName = currentWorkshop?.name ?: (currentUser?.username?.replaceFirstChar { it.uppercase() } + " Atelier Auto")

    // Offered services belonging to this provider
    val myOfferedServices = remember(allOfferedServices, workshopId, currentUser) {
        if (currentUser?.role == "admin") {
            allOfferedServices
        } else {
            val list = allOfferedServices.filter { it.providerId == workshopId }
            if (list.isEmpty() && (currentUser?.username == "prestataire" || currentWorkshop?.isMine == true)) {
                allOfferedServices.filter { it.providerId == "g1" || it.providerId == workshopId }
            } else {
                list
            }
        }
    }

    // Customer bookings for this workshop
    val myBookings = remember(allBookings, workshopId, currentWorkshop) {
        allBookings.filter { booking ->
            booking.providerId == workshopId ||
                    booking.providerId == "g1" ||
                    booking.providerName.equals(workshopName, ignoreCase = true)
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mes Services, 1: RDV Clients, 2: Mon Établissement
    var showCreateServiceSheet by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<OfferedService?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Portail Prestataire", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Création & Gestion de mes Services", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    if (isProUser && currentWorkshop != null) {
                        IconButton(onClick = { onNavigateToDetail(currentWorkshop) }) {
                            Icon(Icons.Default.Storefront, contentDescription = "Voir ma fiche client", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
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
                ProAccessRestrictedCard(onLoginClick = onBack)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Prestataire Workshop Summary Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = null,
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = workshopName,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 17.sp
                                            )
                                            Text(
                                                text = "Compte Prestataire certifié par l'Admin",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        color = if (currentWorkshop?.isOpen != false) Color(0xFF059669) else Color(0xFFDC2626),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (currentWorkshop?.isOpen != false) "OUVERT" else "FERMÉ",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PortalStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "Services Créés",
                                        value = myOfferedServices.size.toString()
                                    )
                                    PortalStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "RDV Reçus",
                                        value = myBookings.size.toString()
                                    )
                                    PortalStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "Ville",
                                        value = currentWorkshop?.city ?: "Bamako"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Role explanation banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Espace Réservé Prestataire : Vous créez et gérez vos propres prestations, tarifs en FCFA et disponibilités.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E3A8A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Navigation Tabs
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
                                            "Mes Services (${myOfferedServices.size})",
                                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    icon = { Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = {
                                        Text(
                                            "RDV Reçus (${myBookings.size})",
                                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                                Tab(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    text = {
                                        Text(
                                            "Mon Garage",
                                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    icon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // TAB 0: MES SERVICES & PRESTATIONS (Créés par le Prestataire)
                    if (selectedTab == 0) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Catalogue de mes Services", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                    Text("Vos clients verront ces services et leurs prix en FCFA", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Button(
                                    onClick = {
                                        serviceToEdit = null
                                        showCreateServiceSheet = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ajouter Service", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (myOfferedServices.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Aucun service créé pour l'instant", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                                        Text(
                                            "Créez votre première prestation (vidange, freinage, diagnostic, vente pièces...) avec son tarif en FCFA.",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                serviceToEdit = null
                                                showCreateServiceSheet = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Créer une prestation", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            items(myOfferedServices, key = { it.id }) { service ->
                                OfferedServiceProCard(
                                    service = service,
                                    onEdit = {
                                        serviceToEdit = service
                                        showCreateServiceSheet = true
                                    },
                                    onDelete = {
                                        servicesViewModel.deleteOfferedService(service.id) {
                                            Toast.makeText(context, "Prestation supprimée", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onToggleAvailability = { isAvail ->
                                        servicesViewModel.toggleOfferedServiceAvailability(service, isAvail)
                                        Toast.makeText(context, if (isAvail) "Service activé" else "Service désactivé", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    // TAB 1: RENDEZ-VOUS REÇUS
                    if (selectedTab == 1) {
                        item {
                            Text("Demandes de Rendez-vous Clients", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (myBookings.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(44.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Aucun rendez-vous pour le moment", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        Text("Lorsque des clients réservent vos services, les créneaux s'affichent ici.", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            items(myBookings, key = { it.id }) { booking ->
                                ProBookingCard(
                                    booking = booking,
                                    onUpdateStatus = { newStatus ->
                                        servicesViewModel.updateBookingStatus(booking.id, newStatus)
                                        Toast.makeText(context, "Statut : $newStatus", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    // TAB 2: MON GARAGE & HORAIRES
                    if (selectedTab == 2) {
                        item {
                            currentWorkshop?.let { workshop ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Text("Gestion de l'Atelier", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Open / Closed Status Toggle
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (workshop.isOpen) "Atelier Ouvert aux Clients" else "Atelier Fermé Actuellement",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (workshop.isOpen) Color(0xFF047857) else Color(0xFFB91C1C)
                                                )
                                                Text(
                                                    text = "Visibilité en direct sur la carte et l'accueil",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }

                                            Switch(
                                                checked = workshop.isOpen,
                                                onCheckedChange = { isOpen ->
                                                    servicesViewModel.toggleServiceStatus(workshop.id, isOpen)
                                                    Toast.makeText(context, if (isOpen) "Atelier marqué Ouvert" else "Atelier marqué Fermé", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text("Coordonnées de l'établissement :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("📍 Ville : ${workshop.city}", fontSize = 13.sp, color = Color(0xFF334155))
                                        Text("📞 Téléphone : ${workshop.phone}", fontSize = 13.sp, color = Color(0xFF334155))
                                        Text("⏰ Horaires : ${workshop.hours ?: "08:00 - 18:30"}", fontSize = 13.sp, color = Color(0xFF334155))
                                        Text("📝 Description : ${workshop.description}", fontSize = 13.sp, color = Color(0xFF475569))

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = { onNavigateToDetail(workshop) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Storefront, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Voir ma fiche garage telle que vue par les clients", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

    // Modal Bottom Sheet: Prestataire creates/edits a service (OfferedService)
    if (showCreateServiceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateServiceSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            OfferedServiceFormSheet(
                providerId = workshopId,
                providerName = workshopName,
                serviceToEdit = serviceToEdit,
                onSave = { newService ->
                    if (serviceToEdit != null) {
                        servicesViewModel.updateOfferedService(newService) {
                            Toast.makeText(context, "Prestation '${newService.title}' mise à jour !", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        servicesViewModel.addOfferedService(newService) {
                            Toast.makeText(context, "Prestation '${newService.title}' créée avec succès !", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showCreateServiceSheet = false
                },
                onCancel = { showCreateServiceSheet = false }
            )
        }
    }
}

@Composable
fun OfferedServiceProCard(
    service: OfferedService,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.FRANCE) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
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
                    Text(
                        text = service.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = BluePrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = service.category,
                                color = BluePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(service.durationMinutes, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = BluePrimary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                    }
                }
            }

            if (service.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = service.description,
                    color = Color(0xFF475569),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${formatter.format(service.priceCfa)} FCFA",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF059669)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (service.isAvailable) "Disponible" else "Suspendu",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (service.isAvailable) Color(0xFF047857) else Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = service.isAvailable,
                        onCheckedChange = onToggleAvailability,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferedServiceFormSheet(
    providerId: String,
    providerName: String,
    serviceToEdit: OfferedService?,
    onSave: (OfferedService) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(serviceToEdit?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(serviceToEdit?.category ?: "Mécanique") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf(serviceToEdit?.priceCfa?.toString() ?: "15000") }
    var duration by remember { mutableStateOf(serviceToEdit?.durationMinutes ?: "45 min") }
    var description by remember { mutableStateOf(serviceToEdit?.description ?: "") }
    var isAvailable by remember { mutableStateOf(serviceToEdit?.isAvailable ?: true) }

    val categories = listOf(
        "Mécanique",
        "Diagnostic valise",
        "Freinage",
        "Climatisation",
        "Électricité",
        "Pneumatique",
        "Carrosserie",
        "Pièces détachées",
        "Entretien périodique",
        "Dépannage / Remorquage"
    )

    val quickServicePresets = listOf(
        Triple("Vidange Moteur 10W40 + Filtre", 15000, "30 min"),
        Triple("Diagnostic Électronique Valise OBD", 10000, "20 min"),
        Triple("Plaquettes de frein avant avec pose", 20000, "45 min"),
        Triple("Recharge Climatisation gaz R134a", 20000, "45 min"),
        Triple("Montage + Équilibrage pneus", 10000, "30 min"),
        Triple("Rénovation Alternateur / Démarreur", 25000, "1h30")
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
            Column {
                Text(
                    text = if (serviceToEdit != null) "Modifier la Prestation" else "Créer une Nouvelle Prestation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Pour l'atelier : $providerName",
                    fontSize = 11.sp,
                    color = BluePrimary
                )
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Fermer")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (serviceToEdit == null) {
            Text("Suggestions rapides de prestations au Mali :", fontSize = 11.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(6.dp))
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(quickServicePresets) { preset ->
                    Surface(
                        modifier = Modifier.clickable {
                            title = preset.first
                            priceText = preset.second.toString()
                            duration = preset.third
                            description = "Prestation professionnelle de ${preset.first} avec garantie pièce et main d'œuvre."
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Text(
                            text = "+ ${preset.first}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = BluePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre du service (ex: Vidange + Filtre à huile)") },
            leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = BluePrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Catégorie de la prestation") },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = BluePrimary) },
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
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            selectedCategory = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Prix (FCFA)") },
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF059669)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Durée estimée") },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = BluePrimary) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description & Pièces incluses dans ce service") },
            minLines = 3,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Service immédiatement disponible pour réservation", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Switch(
                checked = isAvailable,
                onCheckedChange = { isAvailable = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
            )
        }

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
                    if (title.isBlank()) return@Button
                    val price = priceText.toIntOrNull() ?: 15000
                    val newService = OfferedService(
                        id = serviceToEdit?.id ?: UUID.randomUUID().toString(),
                        providerId = providerId,
                        providerName = providerName,
                        title = title.trim(),
                        description = if (description.isBlank()) "Prestation de qualité réalisée par $providerName." else description.trim(),
                        priceCfa = price,
                        durationMinutes = if (duration.isBlank()) "45 min" else duration.trim(),
                        category = selectedCategory,
                        isAvailable = isAvailable
                    )
                    onSave(newService)
                },
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Text("Enregistrer", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
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
                    Text(booking.clientName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Text("Tél: ${booking.clientPhone}", color = BluePrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
            Text("Service demandé : ${booking.serviceType}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("Créneau : ${booking.date} à ${booking.timeSlot}", color = Color.Gray, fontSize = 12.sp)

            if (booking.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Véhicule / Remarque : ${booking.notes}", color = Color(0xFF475569), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.clientPhone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Appeler", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                if (booking.status != "CONFIRME") {
                    OutlinedButton(
                        onClick = { onUpdateStatus("CONFIRME") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Confirmer", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                OutlinedButton(
                    onClick = { onUpdateStatus("TERMINE") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("Terminé", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                IconButton(
                    onClick = { onUpdateStatus("ANNULE") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Annuler", tint = Color.Red, modifier = Modifier.size(18.dp))
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
                text = "Seuls les prestataires créés et validés par l'Administrateur ont accès à cet espace pour créer leurs services.",
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
                Text("Se Connecter avec un Compte Prestataire", color = Color.White, fontWeight = FontWeight.Bold)
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
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
        }
    }
}
