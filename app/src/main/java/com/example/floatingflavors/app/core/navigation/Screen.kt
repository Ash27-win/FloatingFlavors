package com.example.floatingflavors.app.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")

    // Root containers
    object AdminRoot : Screen("admin_root")
    object UserRoot : Screen("user_root")

    // Admin inner screens
    object AdminDashboard : Screen("admin_dashboard")
    object AdminOrders : Screen("admin_orders")                 // <-- new
    object AdminMenuInventory : Screen("admin_menu_inventory")
    object AdminAddFood : Screen("admin_add_food")
    object AdminEditFood : Screen("admin_edit_food/{id}") {
        // helper to build concrete route
        fun createRoute(id: String) = "admin_edit_food/$id"
    }
    object AdminNotifications : Screen("admin_notifications")   // <-- new
    object AdminProfile : Screen("admin_profile")               // <-- new

    // User inner screens
    object UserHome : Screen("user_home")
    object UserMenuGrid : Screen("user_menu_grid")
    object UserBooking : Screen("user_booking")
    object UserOrders : Screen("user_orders")
    object UserProfile : Screen("user_profile")

    object TermsOfService : Screen("terms_of_service")
    object PrivacyPolicy : Screen("privacy_policy")


}
