package com.example.autoconnect.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.ui.screens.AddServiceScreen
import com.example.autoconnect.ui.screens.AdminDashboardScreen
import com.example.autoconnect.ui.screens.BookingsScreen
import com.example.autoconnect.ui.screens.ChatListScreen
import com.example.autoconnect.ui.screens.CostEstimatorScreen
import com.example.autoconnect.ui.screens.DirectChatScreen
import com.example.autoconnect.ui.screens.EmergencyScreen
import com.example.autoconnect.ui.screens.HomeScreen
import com.example.autoconnect.ui.screens.LocalMechanicSearchScreen
import com.example.autoconnect.ui.screens.LoginScreen
import com.example.autoconnect.ui.screens.MaintenanceLogScreen
import com.example.autoconnect.ui.screens.MapScreen
import com.example.autoconnect.ui.screens.NearbyScreen
import com.example.autoconnect.ui.screens.ProviderDetailScreen
import com.example.autoconnect.ui.screens.ProviderDiscoveryScreen
import com.example.autoconnect.ui.screens.ProviderListScreen
import com.example.autoconnect.ui.screens.ProviderPortalScreen
import com.example.autoconnect.ui.screens.SplashScreen
import com.example.autoconnect.ui.screens.TowingRequestScreen
import com.example.autoconnect.ui.screens.TutorialsScreen
import com.example.autoconnect.ui.screens.VehicleHealthReportScreen
import com.example.autoconnect.ui.viewmodel.AuthViewModel
import com.example.autoconnect.ui.viewmodel.ServicesViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object ProviderDetail : Screen("provider_detail/{providerId}") {
        fun createRoute(providerId: String) = "provider_detail/$providerId"
    }
    object AddService : Screen("add_service")
    object ProviderDiscovery : Screen("provider_discovery")
    object ProviderList : Screen("provider_list/{categoryName}") {
        fun createRoute(categoryName: String) = "provider_list/$categoryName"
    }
    object Nearby : Screen("nearby")
    object Map : Screen("map")
    object Emergency : Screen("emergency")
    object ProviderPortal : Screen("provider_portal")
    object AdminDashboard : Screen("admin_dashboard")
    object MaintenanceLog : Screen("maintenance_log")
    object CostEstimator : Screen("cost_estimator")
    object Tutorials : Screen("tutorials")
    object Bookings : Screen("bookings")
    object TowingRequest : Screen("towing_request")
    object VehicleHealthReport : Screen("vehicle_health_report")
    object MechanicSearch : Screen("mechanic_search")
    object ChatList : Screen("chat_list")
    object DirectChat : Screen("direct_chat/{providerId}") {
        fun createRoute(providerId: String) = "direct_chat/$providerId"
    }
}

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    servicesViewModel: ServicesViewModel,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val startDestination = Screen.Splash.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val target = if (currentUser != null) Screen.Home.route else Screen.Login.route
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                servicesViewModel = servicesViewModel,
                onNavigateToDetail = { provider ->
                    navController.navigate(Screen.ProviderDetail.createRoute(provider.id))
                },
                onNavigateToCategoryList = { category ->
                    navController.navigate(Screen.ProviderList.createRoute(category.name))
                },
                onNavigateToAddService = {
                    navController.navigate(Screen.AddService.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateToDiscovery = {
                    navController.navigate(Screen.ProviderDiscovery.route)
                },
                onNavigateToNearby = {
                    navController.navigate(Screen.Nearby.route)
                },
                onNavigateToEmergency = {
                    navController.navigate(Screen.Emergency.route)
                },
                onNavigateToProviderPortal = {
                    navController.navigate(Screen.ProviderPortal.route)
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Screen.AdminDashboard.route)
                },
                onNavigateToMaintenanceLog = {
                    navController.navigate(Screen.MaintenanceLog.route)
                },
                onNavigateToCostEstimator = {
                    navController.navigate(Screen.CostEstimator.route)
                },
                onNavigateToTutorials = {
                    navController.navigate(Screen.Tutorials.route)
                },
                onNavigateToBookings = {
                    navController.navigate(Screen.Bookings.route)
                },
                onNavigateToTowingRequest = {
                    navController.navigate(Screen.TowingRequest.route)
                },
                onNavigateToVehicleHealthReport = {
                    navController.navigate(Screen.VehicleHealthReport.route)
                },
                onNavigateToChatList = {
                    navController.navigate(Screen.ChatList.route)
                },
                onNavigateToMechanicSearch = {
                    navController.navigate(Screen.MechanicSearch.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ProviderDetail.route,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
            val allServices by servicesViewModel.allServices.collectAsState()
            val provider = allServices.find { it.id == providerId }

            if (provider != null) {
                ProviderDetailScreen(
                    provider = provider,
                    authViewModel = authViewModel,
                    servicesViewModel = servicesViewModel,
                    onNavigateToChat = { targetProviderId ->
                        navController.navigate(Screen.DirectChat.createRoute(targetProviderId))
                    },
                    onNavigateToMap = {
                        navController.navigate(Screen.Map.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AddService.route) {
            AddServiceScreen(
                authViewModel = authViewModel,
                servicesViewModel = servicesViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProviderDiscovery.route) {
            ProviderDiscoveryScreen(
                servicesViewModel = servicesViewModel,
                onNavigateToDetail = { provider ->
                    navController.navigate(Screen.ProviderDetail.createRoute(provider.id))
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateToChat = { targetProviderId ->
                    navController.navigate(Screen.DirectChat.createRoute(targetProviderId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ProviderList.route,
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val category = ServiceCategory.fromString(categoryName)
            ProviderListScreen(
                category = category,
                servicesViewModel = servicesViewModel,
                onNavigateToDetail = { provider ->
                    navController.navigate(Screen.ProviderDetail.createRoute(provider.id))
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Nearby.route) {
            NearbyScreen(
                servicesViewModel = servicesViewModel,
                onNavigateToDetail = { provider ->
                    navController.navigate(Screen.ProviderDetail.createRoute(provider.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                servicesViewModel = servicesViewModel,
                onNavigateToDetail = { provider ->
                    navController.navigate(Screen.ProviderDetail.createRoute(provider.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Emergency.route) {
            EmergencyScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProviderPortal.route) {
            ProviderPortalScreen(
                authViewModel = authViewModel,
                servicesViewModel = servicesViewModel,
                onNavigateToAddService = { navController.navigate(Screen.AddService.route) },
                onNavigateToDetail = { provider ->
                    navController.navigate(Screen.ProviderDetail.createRoute(provider.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                authViewModel = authViewModel,
                servicesViewModel = servicesViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MaintenanceLog.route) {
            MaintenanceLogScreen(
                onNavigateToCategoryList = { categoryName ->
                    navController.navigate(Screen.ProviderList.createRoute(categoryName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CostEstimator.route) {
            CostEstimatorScreen(
                onNavigateToMecaniciens = {
                    navController.navigate(Screen.ProviderList.createRoute(ServiceCategory.MECANICIEN.name))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Tutorials.route) {
            TutorialsScreen(
                servicesViewModel = servicesViewModel,
                onNavigateToMecaniciens = {
                    navController.navigate(Screen.ProviderList.createRoute(ServiceCategory.MECANICIEN.name))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Bookings.route) {
            BookingsScreen(
                servicesViewModel = servicesViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TowingRequest.route) {
            TowingRequestScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.VehicleHealthReport.route) {
            VehicleHealthReportScreen(
                servicesViewModel = servicesViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChatList.route) {
            ChatListScreen(
                servicesViewModel = servicesViewModel,
                onNavigateToChat = { targetProviderId ->
                    navController.navigate(Screen.DirectChat.createRoute(targetProviderId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MechanicSearch.route) {
            LocalMechanicSearchScreen(
                onNavigateToDetail = { provider ->
                    navController.navigate(Screen.ProviderDetail.createRoute(provider.id))
                },
                onNavigateToChat = { targetProviderId ->
                    navController.navigate(Screen.DirectChat.createRoute(targetProviderId))
                },
                onNavigateToBooking = { provider ->
                    navController.navigate(Screen.Bookings.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DirectChat.route,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
            DirectChatScreen(
                providerId = providerId,
                servicesViewModel = servicesViewModel,
                onNavigateToBookings = {
                    navController.navigate(Screen.Bookings.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
