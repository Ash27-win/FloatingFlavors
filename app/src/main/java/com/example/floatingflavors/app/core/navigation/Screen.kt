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

    object UserOrderDetails : Screen("user_order_details/{orderId}") {
        fun createRoute(orderId: String) =
            "user_order_details/$orderId"
    }

    object UserOrderTracking : Screen("user_order_tracking/{orderId}/{type}") {
        fun createRoute(orderId: Int, type: String) =
            "user_order_tracking/$orderId/$type"
    }

    object UserProfile : Screen("user_profile")

    // User inner screens
    object UserNotifications : Screen("user_notifications")

    object UserMembership : Screen("user_membership")

    // 🔥 NEW — BOOKING MENU (TEMP SCREEN)
    object UserBookingMenu : Screen("user_booking_menu/{bookingId}") {
        fun createRoute(bookingId: Int) = "user_booking_menu/$bookingId"
    }

    // CHECKOUT SCREENS ADDRESS, SUMMARY, PAYMENT
    object CheckoutAddress : Screen("checkout_address/{bookingId}") {
        fun createRoute(bookingId: Int): String =
            "checkout_address/$bookingId"
    }

    object CheckoutSummary : Screen("checkout_summary/{bookingId}/{addressId}") {
        fun createRoute(bookingId: Int, addressId: Int): String =
            "checkout_summary/$bookingId/$addressId"
    }

    object CheckoutPayment : Screen("checkout_payment/{bookingId}/{addressId}") {
        fun createRoute(bookingId: Int, addressId: Int): String =
            "checkout_payment/$bookingId/$addressId"
    }

    object OrderSuccess : Screen("order_success/{txnId}/{method}") {
        fun createRoute(txnId: String, method: String): String =
            "order_success/$txnId/$method"
    }


    object EditProfile : Screen("edit_profile")

    object SavedAddresses : Screen("saved_addresses")
    object AddAddress : Screen("add_address")

    object EditAddress : Screen("edit_address/{addressId}") {
        fun createRoute(addressId: Int) = "edit_address/$addressId"
    }
    object TermsOfService : Screen("terms_of_service")
    object PrivacyPolicy : Screen("privacy_policy")


}
