package com.example.autoconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.ui.components.LocalMechanicSearchComponent
import com.example.autoconnect.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalMechanicSearchScreen(
    onNavigateToDetail: (ServiceProvider) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToBooking: (ServiceProvider) -> Unit,
    onNavigateToMap: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Recherche Spécialisée",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Trouvez un mécanicien ou atelier qualifié au Mali",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("mechanic_search_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier.testTag("mechanic_search_map_button")
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Voir sur la Carte",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LocalMechanicSearchComponent(
                onMechanicSelected = onNavigateToDetail,
                onNavigateToChat = onNavigateToChat,
                onNavigateToBooking = onNavigateToBooking,
                onNavigateToMap = { onNavigateToMap() }
            )
        }
    }
}
