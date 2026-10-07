package com.example.autoconnect.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.local.BookingEntity
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.viewmodel.AuthViewModel
import com.example.autoconnect.ui.viewmodel.ServicesViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    servicesViewModel: ServicesViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val bookings by servicesViewModel.allBookings.collectAsState()
    val allProviders by servicesViewModel.allServices.collectAsState()

    var selectedFilter by remember { mutableStateOf("Tous") }
    var showNewBookingDialog by remember { mutableStateOf(false) }

    var bookingToReview by remember { mutableStateOf<BookingEntity?>(null) }
    var reviewRating by remember { mutableStateOf(5.0) }
    var reviewComment by remember { mutableStateOf("") }

    val filterOptions = listOf("Tous", "CONFIRME", "EN_ATTENTE", "TERMINE", "ANNULE")

    val filteredBookings = bookings.filter { b ->
        if (selectedFilter == "Tous") true else b.status.equals(selectedFilter, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Rendez-vous & Réservations", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewBookingDialog = true },
                containerColor = BluePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau Rendez-vous")
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            val isWide = maxWidth >= 600.dp
            val contentModifier = if (isWide) {
                Modifier
                    .widthIn(max = 760.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(
                modifier = contentModifier.fillMaxSize()
            ) {
            // Filter Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val label = when (filter) {
                        "CONFIRME" -> "Confirmés"
                        "EN_ATTENTE" -> "En attente"
                        "TERMINE" -> "Terminés"
                        "ANNULE" -> "Annulés"
                        else -> "Tous"
                    }
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Aucun rendez-vous trouvé.", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Appuyez sur le bouton + pour planifier un rendez-vous avec un mécanicien.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBookings, key = { it.id }) { booking ->
                        BookingItemCard(
                            booking = booking,
                            onUpdateStatus = { newStatus ->
                                servicesViewModel.updateBookingStatus(booking.id, newStatus)
                                Toast.makeText(context, "Statut mis à jour: $newStatus", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                servicesViewModel.deleteBooking(booking.id)
                                Toast.makeText(context, "Réservation supprimée", Toast.LENGTH_SHORT).show()
                            },
                            onLeaveReview = {
                                bookingToReview = booking
                                reviewRating = 5.0
                                reviewComment = ""
                            }
                        )
                    }
                }
            }
        }
    }

        // Dialog New Booking
        if (showNewBookingDialog) {
            var selectedProviderName by remember { mutableStateOf(allProviders.firstOrNull()?.name ?: "Garage Auto") }
            var selectedProviderId by remember { mutableStateOf(allProviders.firstOrNull()?.id ?: "1") }
            var serviceType by remember { mutableStateOf("Révision générale & Diagnostic") }
            var dateInput by remember { mutableStateOf("2026-07-30") }
            var timeSlotInput by remember { mutableStateOf("10:00 - 11:30") }
            var clientNameInput by remember { mutableStateOf(currentUser?.username ?: "Client AutoConnect") }
            var clientPhoneInput by remember { mutableStateOf("+223 70 00 11 22") }
            var notesInput by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showNewBookingDialog = false },
                title = { Text("Planifier un Rendez-vous", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Choisissez le garage / prestataire :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(allProviders) { provider ->
                                FilterChip(
                                    selected = selectedProviderId == provider.id,
                                    onClick = {
                                        selectedProviderId = provider.id
                                        selectedProviderName = provider.name
                                    },
                                    label = { Text(provider.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BluePrimary, selectedLabelColor = Color.White)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = serviceType,
                            onValueChange = { serviceType = it },
                            label = { Text("Prestation souhaitée") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dateInput,
                                onValueChange = { dateInput = it },
                                label = { Text("Date (AAAA-MM-JJ)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = timeSlotInput,
                                onValueChange = { timeSlotInput = it },
                                label = { Text("Heure") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = clientPhoneInput,
                            onValueChange = { clientPhoneInput = it },
                            label = { Text("Téléphone de contact Mali") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Modèle véhicule & remarques") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (serviceType.isBlank() || dateInput.isBlank()) {
                                Toast.makeText(context, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val newBooking = BookingEntity(
                                id = UUID.randomUUID().toString(),
                                providerId = selectedProviderId,
                                providerName = selectedProviderName,
                                serviceType = serviceType,
                                clientName = clientNameInput,
                                clientPhone = clientPhoneInput,
                                date = dateInput,
                                timeSlot = timeSlotInput,
                                status = "EN_ATTENTE",
                                notes = notesInput
                            )
                            servicesViewModel.createBooking(newBooking)
                            showNewBookingDialog = false
                            Toast.makeText(context, "Rendez-vous enregistré avec succès !", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Confirmer le RDV", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewBookingDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // Dialog Leave Review
        bookingToReview?.let { booking ->
            AlertDialog(
                onDismissRequest = { bookingToReview = null },
                title = { Text("Évaluer le service", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Prestation : ${booking.serviceType}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary)
                        Text("Prestataire : ${booking.providerName}", fontSize = 12.sp, color = Color(0xFF334155))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            for (i in 1..5) {
                                IconButton(
                                    onClick = { reviewRating = i.toDouble() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (i <= reviewRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$i Étoiles",
                                        tint = if (i <= reviewRating) Color(0xFFFFB300) else Color.Gray
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            label = { Text("Votre avis sur ce garage") },
                            placeholder = { Text("Qualité du travail, ponctualité, amabilité...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (reviewComment.isBlank()) {
                                Toast.makeText(context, "Veuillez entrer une remarque ou appréciation", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val newReview = com.example.autoconnect.data.model.Review(
                                id = UUID.randomUUID().toString(),
                                serviceId = booking.providerId,
                                userName = currentUser?.username ?: booking.clientName,
                                comment = reviewComment.trim(),
                                rating = reviewRating,
                                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                            )
                            servicesViewModel.addReview(newReview)
                            bookingToReview = null
                            Toast.makeText(context, "Avis enregistré ! Merci pour votre évaluation.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Envoyer l'avis", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { bookingToReview = null }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
fun BookingItemCard(
    booking: BookingEntity,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit,
    onLeaveReview: () -> Unit = {}
) {
    val context = LocalContext.current
    val statusColor = when (booking.status.uppercase()) {
        "CONFIRME" -> Color(0xFF2E7D32)
        "EN_ATTENTE" -> Color(0xFFE65100)
        "TERMINE" -> Color(0xFF1E88E5)
        else -> Color(0xFFC62828)
    }

    val statusLabel = when (booking.status.uppercase()) {
        "CONFIRME" -> "CONFIRMÉ"
        "EN_ATTENTE" -> "EN ATTENTE DE VALIDATION"
        "TERMINE" -> "TERMINÉ"
        else -> "ANNULÉ"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = booking.providerName,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = booking.serviceType,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = BluePrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Date : ${booking.date} (${booking.timeSlot})",
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Client : ${booking.clientName} (${booking.clientPhone})",
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (booking.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notes : ${booking.notes}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Appeler", fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Button(
                    onClick = onLeaveReview,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Avis", fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                if (booking.status != "CONFIRME") {
                    Button(
                        onClick = { onUpdateStatus("CONFIRME") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Confirmer", fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                if (booking.status != "ANNULE") {
                    IconButton(
                        onClick = { onUpdateStatus("ANNULE") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Annuler", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
