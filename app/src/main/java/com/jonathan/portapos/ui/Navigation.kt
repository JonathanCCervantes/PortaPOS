package com.jonathan.portapos.ui

// These are the "addresses" of each screen in the app
sealed class Screen(val route: String) {
    object PinEntry : Screen("pin_entry?purpose={purpose}") {
        fun createRoute(purpose: String = "login") = "pin_entry?purpose=$purpose"
    }
    object Menu     : Screen("menu")        // food menu / main POS screen
    object Cart     : Screen("cart")       // shopping cart review
    object Checkout : Screen("checkout")   // payment (cash or e-wallet)
    object Receipt  : Screen("receipt/{orderId}") {
        fun createRoute(orderId: Long) = "receipt/$orderId"
    }
    object Settings : Screen("settings")   // admin / product management
    object AddEditProduct : Screen("add_edit_product/{productId}") {
        fun createRoute(productId: Int = 0) = "add_edit_product/$productId"
    }
}
