package com.example.autoconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.autoconnect.ui.navigation.NavGraph
import com.example.autoconnect.ui.theme.AutoConnectTheme
import com.example.autoconnect.ui.viewmodel.AuthViewModel
import com.example.autoconnect.ui.viewmodel.ServicesViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.Factory((application as AutoConnectApplication).repository)
    }

    private val servicesViewModel: ServicesViewModel by viewModels {
        ServicesViewModel.Factory((application as AutoConnectApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoConnectTheme {
                NavGraph(
                    authViewModel = authViewModel,
                    servicesViewModel = servicesViewModel
                )
            }
        }
    }
}
