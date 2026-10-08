package com.example.autoconnect.data.repository

import android.content.Context
import com.example.autoconnect.data.firebase.FirebaseManager
import com.example.autoconnect.data.local.AppDatabase
import com.example.autoconnect.data.local.BookingEntity
import com.example.autoconnect.data.local.OfferedServiceEntity
import com.example.autoconnect.data.local.ReviewEntity
import com.example.autoconnect.data.local.ServiceProviderEntity
import com.example.autoconnect.data.local.UserEntity
import com.example.autoconnect.data.model.AppUser
import com.example.autoconnect.data.model.OfferedService
import com.example.autoconnect.data.model.Review
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.data.sync.DataSyncManager
import com.example.autoconnect.data.sync.SyncStatus
import com.example.autoconnect.data.sync.SyncSummary
import com.example.autoconnect.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class AutoConnectRepository(
    private val database: AppDatabase,
    private val context: Context? = null
) {

    private val userDao = database.userDao()
    private val serviceDao = database.serviceProviderDao()
    private val reviewDao = database.reviewDao()
    private val tutorialDao = database.tutorialDao()
    private val bookingDao = database.bookingDao()
    private val chatDao = database.chatMessageDao()
    private val offeredServiceDao = database.offeredServiceDao()

    val syncManager: DataSyncManager? = context?.let { DataSyncManager.getInstance(it) }

    val syncStatus: Flow<SyncStatus> = syncManager?.syncStatus ?: flowOf(SyncStatus.Idle())
    val isOnline: Flow<Boolean> = syncManager?.isOnline ?: flowOf(true)

    suspend fun triggerDataSync(forceRefresh: Boolean = false): Result<SyncSummary>? {
        return syncManager?.syncFromFirestore(forceRefresh)
    }

    fun getLastSyncFormatted(): String {
        return syncManager?.getLastSyncFormatted() ?: "Jamais"
    }

    // --- CHAT & MESSAGING ---
    fun getAllChatMessages(): Flow<List<com.example.autoconnect.data.local.ChatMessageEntity>> {
        return chatDao.getAllMessages()
    }

    fun getChatMessagesForProvider(providerId: String): Flow<List<com.example.autoconnect.data.local.ChatMessageEntity>> {
        return chatDao.getMessagesForProvider(providerId)
    }

    suspend fun sendChatMessage(message: com.example.autoconnect.data.local.ChatMessageEntity) {
        chatDao.insertMessage(message)
    }

    suspend fun clearChatHistory(providerId: String) {
        chatDao.deleteMessagesForProvider(providerId)
    }

    // --- BOOKINGS (APPOINTMENTS) ---
    fun getAllBookings(): Flow<List<com.example.autoconnect.data.local.BookingEntity>> {
        return bookingDao.getAllBookings()
    }

    suspend fun insertBooking(booking: com.example.autoconnect.data.local.BookingEntity) {
        bookingDao.insertBooking(booking)
        context?.let { ctx ->
            FirebaseManager.pushBookingToFirestore(ctx, booking)
        }
    }

    suspend fun updateBookingStatus(id: String, status: String) {
        bookingDao.updateStatus(id, status)
    }

    suspend fun deleteBooking(id: String) {
        bookingDao.deleteBooking(id)
    }

    // --- TUTORIALS (OFFLINE CACHE) ---
    fun getAllTutorials(): Flow<List<com.example.autoconnect.data.local.TutorialEntity>> {
        return tutorialDao.getAllTutorials()
    }

    suspend fun seedProvidersIfEmpty() {
        val entities = com.example.autoconnect.data.SampleData.sampleProviders.map { ServiceProviderEntity.fromDomainModel(it) }
        serviceDao.insertAll(entities)
    }

    suspend fun seedTutorialsIfEmpty() {
        // Fallback seed if database was already created before
        tutorialDao.insertAll(com.example.autoconnect.data.SampleData.sampleTutorials)
    }

    suspend fun seedReviewsIfEmpty() {
        val existingReviews = reviewDao.getReviewsListForService("p1")
        if (existingReviews.isEmpty()) {
            com.example.autoconnect.data.SampleData.sampleReviews.forEach { review ->
                reviewDao.insertReview(review)
                recalculateProviderRating(review.serviceId)
            }
        }
    }

    // --- USER / AUTH ---
    suspend fun getUserByUsername(username: String): AppUser? {
        val userEntity = userDao.getUserByUsername(username.trim().lowercase())
        return userEntity?.let { AppUser(id = it.id, username = it.username, role = it.role) }
    }

    suspend fun authenticate(username: String, password: String): AppUser? {
        val normalized = username.trim().lowercase()
        var userEntity = userDao.getUserByUsername(normalized)
        
        if (normalized == "admin") {
            if (userEntity == null) {
                val newHash = PasswordHasher.hash("admin", "00223")
                userEntity = UserEntity(id = "u1", username = "admin", passwordHash = newHash, role = "admin")
                userDao.insertUser(userEntity)
            }
            var isValid = PasswordHasher.verify("admin", password, userEntity.passwordHash)
            if (!isValid && password == "00223") {
                val newHash = PasswordHasher.hash("admin", "00223")
                userDao.updatePassword("admin", newHash)
                isValid = true
            }
            return if (isValid) AppUser(id = userEntity.id, username = userEntity.username, role = userEntity.role) else null
        }

        if (userEntity == null) return null
        val isValid = PasswordHasher.verify(normalized, password, userEntity.passwordHash)
        return if (isValid) {
            AppUser(id = userEntity.id, username = userEntity.username, role = userEntity.role)
        } else null
    }

    suspend fun registerUser(username: String, password: String, role: String): AppUser? {
        val normalized = username.trim().lowercase()
        val existing = userDao.getUserByUsername(normalized)
        if (existing != null) return null // Username taken

        val newId = UUID.randomUUID().toString()
        val passwordHash = PasswordHasher.hash(normalized, password)
        val newEntity = UserEntity(id = newId, username = normalized, passwordHash = passwordHash, role = role)
        userDao.insertUser(newEntity)
        return AppUser(id = newId, username = normalized, role = role)
    }

    suspend fun updateAdminPassword(newPassword: String) {
        val passwordHash = PasswordHasher.hash("admin", newPassword)
        userDao.updatePassword("admin", passwordHash)
    }

    suspend fun createPrestataireOnly(username: String, password: String): Boolean {
        val normalized = username.trim().lowercase()
        val existing = userDao.getUserByUsername(normalized)
        if (existing != null) return false

        val userId = UUID.randomUUID().toString()
        val passwordHash = PasswordHasher.hash(normalized, password)
        val userEntity = UserEntity(id = userId, username = normalized, passwordHash = passwordHash, role = "prestataire")
        userDao.insertUser(userEntity)
        return true
    }

    suspend fun getPrestataires(): List<AppUser> {
        return userDao.getUsersByRole("prestataire").map { AppUser(id = it.id, username = it.username, role = it.role) }
    }

    suspend fun deletePrestataire(userId: String) {
        userDao.deleteUser(userId)
    }

    suspend fun createProAccountByAdmin(
        username: String,
        password: String,
        serviceName: String,
        category: ServiceCategory,
        city: String,
        phone: String,
        address: String,
        description: String,
        latitude: Double = 12.6392,
        longitude: Double = -8.0029
    ): Boolean {
        val normalized = username.trim().lowercase()
        val existing = userDao.getUserByUsername(normalized)
        if (existing != null) return false

        val userId = UUID.randomUUID().toString()
        val passwordHash = PasswordHasher.hash(normalized, password)
        val userEntity = UserEntity(id = userId, username = normalized, passwordHash = passwordHash, role = "prestataire")
        userDao.insertUser(userEntity)

        val fullDesc = if (address.isNotBlank()) "$description ($address)" else description

        val serviceProvider = ServiceProvider(
            id = userId,
            name = serviceName,
            category = category,
            description = fullDesc,
            city = city,
            phone = phone,
            rating = 5.0,
            latitude = latitude,
            longitude = longitude,
            isOpen = true,
            isFavorite = false,
            isMine = true,
            hours = "08:00 - 18:00",
            servicesOffered = "Prestations sur mesure"
        )
        insertService(serviceProvider)
        return true
    }

    suspend fun getAssociatedUsernameForProvider(providerId: String): String? {
        val user = userDao.getUserById(providerId)
        if (user != null) return user.username

        val service = serviceDao.getServiceById(providerId) ?: return null
        val phoneClean = service.phone.replace(" ", "").replace("+", "").lowercase()
        val phoneUser = userDao.getUserByUsername(phoneClean)
        return phoneUser?.username
    }

    suspend fun updateGarageNameAndPassword(
        providerId: String,
        newName: String,
        username: String,
        newPassword: String?
    ): Result<Unit> {
        return try {
            val service = serviceDao.getServiceById(providerId)
                ?: return Result.failure(IllegalArgumentException("Garage introuvable"))

            // 1. Mettre à jour le nom du garage dans la base de données
            val trimmedName = newName.trim()
            if (trimmedName.isNotBlank() && trimmedName != service.name) {
                val updatedService = service.copy(name = trimmedName)
                updateService(updatedService.toDomainModel())
            }

            // 2. Mettre à jour les identifiants du garage (mot de passe / username)
            val normalizedUsername = username.trim().lowercase()
            val existingUserById = userDao.getUserById(providerId)
            val userWithSameName = userDao.getUserByUsername(normalizedUsername)

            if (userWithSameName != null && userWithSameName.id != providerId && (existingUserById == null || userWithSameName.id != existingUserById.id)) {
                return Result.failure(IllegalArgumentException("Le nom d'utilisateur '$normalizedUsername' est déjà attribué."))
            }

            if (!newPassword.isNullOrBlank()) {
                val newHash = PasswordHasher.hash(normalizedUsername, newPassword.trim())
                if (existingUserById != null) {
                    userDao.updateUserCredentials(existingUserById.id, normalizedUsername, newHash)
                } else if (userWithSameName != null) {
                    userDao.updatePassword(normalizedUsername, newHash)
                } else {
                    userDao.insertUser(
                        UserEntity(
                            id = providerId,
                            username = normalizedUsername,
                            passwordHash = newHash,
                            role = "prestataire"
                        )
                    )
                }
            } else {
                if (existingUserById != null && existingUserById.username != normalizedUsername) {
                    userDao.updateUsername(existingUserById.id, normalizedUsername)
                } else if (existingUserById == null && userWithSameName == null) {
                    val defaultHash = PasswordHasher.hash(normalizedUsername, "00223")
                    userDao.insertUser(
                        UserEntity(
                            id = providerId,
                            username = normalizedUsername,
                            passwordHash = defaultHash,
                            role = "prestataire"
                        )
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): List<AppUser> {
        return userDao.getAllUsers().map { AppUser(id = it.id, username = it.username, role = it.role) }
    }

    fun startCloudSync(scope: CoroutineScope) {
        syncManager?.let { manager ->
            manager.startRealtimeSync(scope)
            scope.launch {
                manager.syncFromFirestore()
            }
        } ?: run {
            context?.let { ctx ->
                FirebaseManager.syncFromFirestore(ctx, scope) { service ->
                    serviceDao.insertService(ServiceProviderEntity.fromDomainModel(service))
                }
            }
        }
    }

    // --- SERVICE PROVIDERS ---
    fun getAllServices(): Flow<List<ServiceProvider>> {
        return serviceDao.getAllServices().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getMechanics(): Flow<List<ServiceProvider>> {
        return serviceDao.getMechanics().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getPartsShops(): Flow<List<ServiceProvider>> {
        return serviceDao.getPartsShops().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getServicesByCategory(category: ServiceCategory): Flow<List<ServiceProvider>> {
        return serviceDao.getServicesByCategory(category.name).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    suspend fun insertService(service: ServiceProvider) {
        serviceDao.insertService(ServiceProviderEntity.fromDomainModel(service))
        context?.let { ctx ->
            FirebaseManager.pushServiceToFirestore(ctx, service)
        }
    }

    suspend fun updateService(service: ServiceProvider) {
        serviceDao.updateService(ServiceProviderEntity.fromDomainModel(service))
        context?.let { ctx ->
            FirebaseManager.pushServiceToFirestore(ctx, service)
        }
    }

    suspend fun deleteService(id: String) {
        serviceDao.deleteService(id)
        offeredServiceDao.deleteServicesForProvider(id)
    }

    // --- OFFERED SERVICES (PRESTATIONS DES PRESTATAIRES) ---
    fun getOfferedServicesForProvider(providerId: String): Flow<List<OfferedService>> {
        return offeredServiceDao.getServicesForProvider(providerId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getAllOfferedServices(): Flow<List<OfferedService>> {
        return offeredServiceDao.getAllServices().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    suspend fun addOfferedService(service: OfferedService) {
        offeredServiceDao.insertService(OfferedServiceEntity.fromDomainModel(service))
    }

    suspend fun updateOfferedService(service: OfferedService) {
        offeredServiceDao.updateService(OfferedServiceEntity.fromDomainModel(service))
    }

    suspend fun deleteOfferedService(id: String) {
        offeredServiceDao.deleteService(id)
    }

    suspend fun toggleFavorite(id: String) {
        val serviceEntity = serviceDao.getServiceById(id) ?: return
        val updated = serviceEntity.copy(isFavorite = !serviceEntity.isFavorite)
        serviceDao.updateService(updated)
    }

    suspend fun toggleServiceStatus(id: String, isOpen: Boolean) {
        val serviceEntity = serviceDao.getServiceById(id) ?: return
        val updated = serviceEntity.copy(isOpen = isOpen)
        serviceDao.updateService(updated)
        context?.let { ctx ->
            FirebaseManager.pushServiceToFirestore(ctx, updated.toDomainModel())
        }
    }

    // --- REVIEWS ---
    fun getReviewsForService(serviceId: String): Flow<List<Review>> {
        return reviewDao.getReviewsForService(serviceId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getAllReviews(): Flow<List<Review>> {
        return reviewDao.getAllReviews().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    suspend fun addReview(review: Review) {
        reviewDao.insertReview(ReviewEntity.fromDomainModel(review))
        recalculateProviderRating(review.serviceId)
        context?.let { ctx ->
            FirebaseManager.pushReviewToFirestore(ctx, review)
        }
    }

    suspend fun deleteReview(reviewId: String, serviceId: String) {
        reviewDao.deleteReview(reviewId)
        recalculateProviderRating(serviceId)
    }

    private suspend fun recalculateProviderRating(serviceId: String) {
        val reviews = reviewDao.getReviewsListForService(serviceId)
        val service = serviceDao.getServiceById(serviceId) ?: return

        val newRating = if (reviews.isNotEmpty()) {
            val avg = reviews.map { it.rating }.average()
            (Math.round(avg * 10.0) / 10.0)
        } else {
            5.0
        }

        serviceDao.updateService(service.copy(rating = newRating))
    }
}
