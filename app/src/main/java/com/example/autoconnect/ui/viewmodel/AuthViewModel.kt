package com.example.autoconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.autoconnect.data.model.AppUser
import com.example.autoconnect.data.repository.AutoConnectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AutoConnectRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isAuthenticated: Boolean get() = _currentUser.value != null

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val user = repository.authenticate(username, password)
            _isLoading.value = false
            if (user != null) {
                _currentUser.value = user
                onResult(true)
            } else {
                _errorMessage.value = "Nom d'utilisateur ou mot de passe incorrect"
                onResult(false)
            }
        }
    }

    fun register(username: String, password: String, role: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val user = repository.registerUser(username, password, role)
            _isLoading.value = false
            if (user != null) {
                _currentUser.value = user
                onResult(true)
            } else {
                _errorMessage.value = "Ce nom d'utilisateur est déjà pris"
                onResult(false)
            }
        }
    }

    fun loginWithGoogle(user: AppUser) {
        _currentUser.value = user
        _errorMessage.value = null
    }

    fun loginAsGuest() {
        _currentUser.value = AppUser(
            id = "guest_user",
            username = "Invité AutoConnect",
            role = "client"
        )
        _errorMessage.value = null
    }

    // Strict authentication required for all accounts (Admin, Pro, Client)

    fun updateAdminPassword(newPassword: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.updateAdminPassword(newPassword)
                _isLoading.value = false
                onResult(true)
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erreur lors de la mise à jour du mot de passe"
                onResult(false)
            }
        }
    }

    private val _prestataires = MutableStateFlow<List<AppUser>>(emptyList())
    val prestataires: StateFlow<List<AppUser>> = _prestataires.asStateFlow()

    fun loadPrestataires() {
        viewModelScope.launch {
            _prestataires.value = repository.getPrestataires()
        }
    }

    fun createPrestataire(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val success = repository.createPrestataireOnly(username, password)
            _isLoading.value = false
            if (success) {
                loadPrestataires()
                onResult(true)
            } else {
                _errorMessage.value = "Ce nom d'utilisateur est déjà utilisé"
                onResult(false)
            }
        }
    }

    fun deletePrestataire(userId: String) {
        viewModelScope.launch {
            repository.deletePrestataire(userId)
            loadPrestataires()
        }
    }

    fun createProAccount(
        username: String,
        password: String,
        serviceName: String,
        category: com.example.autoconnect.data.model.ServiceCategory,
        city: String,
        phone: String,
        address: String,
        description: String,
        latitude: Double = 12.6392,
        longitude: Double = -8.0029,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val success = repository.createProAccountByAdmin(
                username = username,
                password = password,
                serviceName = serviceName,
                category = category,
                city = city,
                phone = phone,
                address = address,
                description = description,
                latitude = latitude,
                longitude = longitude
            )
            _isLoading.value = false
            if (success) {
                onResult(true)
            } else {
                _errorMessage.value = "Ce nom d'utilisateur existe déjà"
                onResult(false)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(private val repository: AutoConnectRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
