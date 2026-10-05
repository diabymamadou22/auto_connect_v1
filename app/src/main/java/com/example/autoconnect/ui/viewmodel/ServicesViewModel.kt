package com.example.autoconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.autoconnect.data.model.Review
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.data.repository.AutoConnectRepository
import com.example.autoconnect.data.sync.SyncStatus
import com.example.autoconnect.data.sync.SyncSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServicesViewModel(private val repository: AutoConnectRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.seedProvidersIfEmpty()
            repository.seedTutorialsIfEmpty()
            repository.seedReviewsIfEmpty()
            repository.startCloudSync(this)
        }
    }

    val syncStatus: StateFlow<SyncStatus> = repository.syncStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncStatus.Idle()
        )

    val isOnline: StateFlow<Boolean> = repository.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val allServices: StateFlow<List<ServiceProvider>> = repository.getAllServices()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mechanics: StateFlow<List<ServiceProvider>> = repository.getMechanics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val partsShops: StateFlow<List<ServiceProvider>> = repository.getPartsShops()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun syncDataNow(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.triggerDataSync(forceRefresh = true)
            if (result != null && result.isSuccess) {
                val summary = result.getOrNull()
                val msg = summary?.message ?: "Données synchronisées avec succès."
                onComplete?.invoke(true, msg)
            } else {
                val errorMsg = result?.exceptionOrNull()?.localizedMessage ?: "Synchronisation hors-ligne."
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    fun getLastSyncFormatted(): String {
        return repository.getLastSyncFormatted()
    }

    val allTutorials: StateFlow<List<com.example.autoconnect.data.local.TutorialEntity>> = repository.getAllTutorials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBookings: StateFlow<List<com.example.autoconnect.data.local.BookingEntity>> = repository.getAllBookings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allChatMessages: StateFlow<List<com.example.autoconnect.data.local.ChatMessageEntity>> = repository.getAllChatMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allReviews: StateFlow<List<Review>> = repository.getAllReviews()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCity = MutableStateFlow("Toutes")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _onlyOpen = MutableStateFlow(false)
    val onlyOpen: StateFlow<Boolean> = _onlyOpen.asStateFlow()

    private val _sortBy = MutableStateFlow("rating") // "rating", "distance", "name"
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    val filteredServices: StateFlow<List<ServiceProvider>> = combine(
        allServices, searchQuery, selectedCity, onlyOpen, sortBy
    ) { services, query, city, openOnly, sort ->
        var list = services.filter { s ->
            val matchesQuery = query.isEmpty() ||
                    s.name.contains(query, ignoreCase = true) ||
                    s.description.contains(query, ignoreCase = true) ||
                    s.city.contains(query, ignoreCase = true)
            val matchesCity = city == "Toutes" || s.city.equals(city, ignoreCase = true)
            val matchesOpen = !openOnly || s.isOpen
            matchesQuery && matchesCity && matchesOpen
        }

        list = when (sort) {
            "name" -> list.sortedBy { it.name.lowercase() }
            "distance" -> list.sortedBy { it.getDistance(12.6392, -8.0029) } // Default Bamako coordinates
            else -> list.sortedByDescending { it.rating }
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCity(city: String) {
        _selectedCity.value = city
    }

    fun setOnlyOpen(onlyOpen: Boolean) {
        _onlyOpen.value = onlyOpen
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    fun resetFilters() {
        _selectedCity.value = "Toutes"
        _onlyOpen.value = false
        _sortBy.value = "rating"
        _searchQuery.value = ""
    }

    fun createBooking(booking: com.example.autoconnect.data.local.BookingEntity) {
        viewModelScope.launch {
            repository.insertBooking(booking)
        }
    }

    fun updateBookingStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(id, status)
        }
    }

    fun sendChatMessage(
        providerId: String,
        providerName: String,
        userMessageText: String,
        isQuoteRequest: Boolean = false
    ) {
        viewModelScope.launch {
            val userMsg = com.example.autoconnect.data.local.ChatMessageEntity(
                id = java.util.UUID.randomUUID().toString(),
                providerId = providerId,
                providerName = providerName,
                sender = "user",
                message = userMessageText,
                timestamp = System.currentTimeMillis()
            )
            repository.sendChatMessage(userMsg)

            // Simulate mechanic response after short delay
            kotlinx.coroutines.delay(1200)

            val autoReplyText = if (isQuoteRequest || userMessageText.contains("devis", ignoreCase = true) || userMessageText.contains("prix", ignoreCase = true)) {
                "Bonjour ! Nous avons bien reçu votre demande d'estimation pour $providerName. Nos techniciens qualifiés examinent la fiche de votre véhicule."
            } else if (userMessageText.contains("bruit", ignoreCase = true) || userMessageText.contains("frein", ignoreCase = true)) {
                "Bonjour. Les bruits de grincement au freinage indiquent généralement une usure des plaquettes. Repassez à l'atelier pour un contrôle visuel gratuit !"
            } else if (userMessageText.contains("voyant", ignoreCase = true) || userMessageText.contains("moteur", ignoreCase = true)) {
                "Bonjour. Si le voyant moteur est orange fixe, vous pouvez rouler prudemment jusqu'à notre garage pour un passage à la valise de diagnostic."
            } else {
                "Merci pour votre message ! Un mécanicien de $providerName est en ligne et étudie votre demande. N'hésitez pas à nous laisser votre numéro."
            }

            val autoQuoteAmount = if (isQuoteRequest || userMessageText.contains("devis", ignoreCase = true)) {
                when {
                    userMessageText.contains("vidange", ignoreCase = true) -> "35 000 FCFA"
                    userMessageText.contains("pneu", ignoreCase = true) -> "40 000 FCFA"
                    userMessageText.contains("frein", ignoreCase = true) -> "25 000 FCFA"
                    else -> "15 000 - 50 000 FCFA (selon pièces)"
                }
            } else ""

            val mechanicReply = com.example.autoconnect.data.local.ChatMessageEntity(
                id = java.util.UUID.randomUUID().toString(),
                providerId = providerId,
                providerName = providerName,
                sender = "mechanic",
                message = autoReplyText,
                timestamp = System.currentTimeMillis(),
                isQuote = autoQuoteAmount.isNotBlank(),
                quoteAmount = autoQuoteAmount
            )
            repository.sendChatMessage(mechanicReply)
        }
    }

    fun clearChatHistory(providerId: String) {
        viewModelScope.launch {
            repository.clearChatHistory(providerId)
        }
    }

    fun deleteBooking(id: String) {
        viewModelScope.launch {
            repository.deleteBooking(id)
        }
    }

    fun addService(service: ServiceProvider) {
        viewModelScope.launch {
            repository.insertService(service)
        }
    }

    fun updateService(service: ServiceProvider) {
        viewModelScope.launch {
            repository.updateService(service)
        }
    }

    fun deleteService(id: String) {
        viewModelScope.launch {
            repository.deleteService(id)
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun toggleServiceStatus(id: String, isOpen: Boolean) {
        viewModelScope.launch {
            repository.toggleServiceStatus(id, isOpen)
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            repository.addReview(review)
        }
    }

    fun deleteReview(reviewId: String, serviceId: String) {
        viewModelScope.launch {
            repository.deleteReview(reviewId, serviceId)
        }
    }

    fun getReviewsForService(serviceId: String): StateFlow<List<Review>> {
        return repository.getReviewsForService(serviceId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    class Factory(private val repository: AutoConnectRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ServicesViewModel(repository) as T
        }
    }
}
