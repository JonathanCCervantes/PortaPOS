package com.jonathan.portapos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jonathan.portapos.ui.Screen
import com.jonathan.portapos.ui.screens.*
import com.jonathan.portapos.ui.theme.PortaPOSTheme
import com.jonathan.portapos.ui.viewmodel.MainViewModel
import com.jonathan.portapos.ui.viewmodel.MainViewModelFactory

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PortaPOSTheme {
                // Detect if we're on a phone or tablet
                val windowSizeClass = calculateWindowSizeClass(this)
                val isTablet = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

                // Get our ViewModel
                val app = application as PortaPOSApplication
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(app.repository)
                )

                // Set up navigation
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.PinEntry.createRoute("login")
                ) {
                    composable(
                        Screen.PinEntry.route,
                        arguments = listOf(androidx.navigation.navArgument("purpose") { defaultValue = "login" })
                    ) { backStackEntry ->
                        val purpose = backStackEntry.arguments?.getString("purpose") ?: "login"
                        PinEntryScreen(
                            onCorrectPin = {
                                when (purpose) {
                                    "login" -> {
                                        navController.navigate(Screen.Menu.route) {
                                            popUpTo(Screen.PinEntry.route) { inclusive = true }
                                        }
                                    }
                                    "reset" -> {
                                        viewModel.resetSales()
                                        navController.popBackStack()
                                    }
                                    "unlock" -> {
                                        stopLockTask()
                                        navController.popBackStack()
                                    }
                                }
                            }
                        )
                    }
                    composable(Screen.Menu.route) {
                        MenuScreen(
                            isTablet = isTablet,
                            viewModel = viewModel,
                            onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onUnlock = { navController.navigate(Screen.PinEntry.createRoute("unlock")) }
                        )
                    }
                    composable(Screen.Cart.route) {
                        CartScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onCheckout = { navController.navigate(Screen.Checkout.route) }
                        )
                    }
                    composable(Screen.Checkout.route) {
                        CheckoutScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onOrderComplete = { orderId ->
                                navController.navigate(Screen.Receipt.createRoute(orderId)) {
                                    popUpTo(Screen.Menu.route) { inclusive = false }
                                }
                            }
                        )
                    }
                    composable(Screen.Receipt.route) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId")?.toLong() ?: 0
                        ReceiptScreen(
                            orderId = orderId,
                            repository = app.repository,
                            onDone = {
                                navController.navigate(Screen.Menu.route) {
                                    popUpTo(Screen.Menu.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute()) },
                            onEditProduct = { id -> navController.navigate(Screen.AddEditProduct.createRoute(id)) },
                            onRequirePin = { purpose -> navController.navigate(Screen.PinEntry.createRoute(purpose)) }
                        )
                    }
                    composable(Screen.AddEditProduct.route) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId")?.toInt() ?: 0
                        AddEditProductScreen(
                            productId = productId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
