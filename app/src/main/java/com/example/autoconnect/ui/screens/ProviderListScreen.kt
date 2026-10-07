package com.example.autoconnect.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.theme.BluePrimary
import com.example.autoconnect.ui.viewmodel.ServicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderListScreen(
    category: ServiceCategory,
    servicesViewModel: ServicesViewModel,
    onNavigateToDetail: (ServiceProvider) -> Unit,
    onNavigateToMap: () -> Unit = {},
    onBack: () -> Unit
) {
    val allServices by servicesViewModel.allServices.collectAsState()
    val categoryServices = remember(allServices, category) {
        allServices.filter { it.category == category }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToMap) {
                        Icon(Icons.Default.Map, contentDescription = "Voir sur la Carte", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = category.color)
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 600.dp
            val listModifier = if (isTablet) {
                Modifier
                    .widthIn(max = 760.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(
                modifier = listModifier.fillMaxSize()
            ) {
                if (categoryServices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucun prestataire dans cette catégorie.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(categoryServices, key = { it.id }) { provider ->
                            Box(modifier = Modifier.padding(bottom = 8.dp)) {
                                ListProviderCard(
                                    provider = provider,
                                    onClick = { onNavigateToDetail(provider) },
                                    onNavigateToMap = onNavigateToMap
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
