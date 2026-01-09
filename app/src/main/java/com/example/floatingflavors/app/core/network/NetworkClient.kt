package com.example.floatingflavors.app.core.network


import com.example.floatingflavors.app.chatbot.ChatApi
import com.example.floatingflavors.app.feature.admin.data.remote.AdminSettingsApi
import com.example.floatingflavors.app.feature.admin.presentation.tracking.data.AdminLocationApi
import com.example.floatingflavors.app.feature.auth.data.remote.AuthApi
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryApi
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.data.DeliveryLocationApi
import com.example.floatingflavors.app.feature.menu.data.remote.MenuApi // NEW
import com.example.floatingflavors.app.feature.orders.data.remote.OrdersApi
import com.example.floatingflavors.app.feature.user.data.booking.BookingApi
import com.example.floatingflavors.app.feature.user.data.booking_checkout.AddressCheckoutApi
import com.example.floatingflavors.app.feature.user.data.booking_checkout.CheckoutSummaryApi
import com.example.floatingflavors.app.feature.user.data.booking_checkout.PaymentApi
import com.example.floatingflavors.app.feature.user.data.cart.CartApi
import com.example.floatingflavors.app.feature.user.data.cart.CheckoutApi
import com.example.floatingflavors.app.feature.user.data.membership.MembershipApi
import com.example.floatingflavors.app.feature.user.data.order.UserOrdersApi
import com.example.floatingflavors.app.feature.user.data.remote.api.HomeApi
import com.example.floatingflavors.app.feature.user.data.settings.AddressApi
import com.example.floatingflavors.app.feature.user.data.settings.UserSettingsApi
import com.example.floatingflavors.app.feature.user.data.settings.EditProfileApi
import com.example.floatingflavors.app.feature.user.data.tracking.OrderTrackingApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {

    // For Android emulator, localhost = 10.0.2.2
//    const val BASE_URL = "http://10.198.130.250/floating_flavors_api/"  //APPA WIFI
    const val BASE_URL = "http://10.88.233.250/floating_flavors_api/"
//    const val BASE_URL = "https://wv1qhk7m-80.inc1.devtunnels.ms/floating_flavors_api/"

    const val CHATBOT_BASE_URL = "http://10.198.130.250:8000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val chatbotRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(CHATBOT_BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val chatApi: ChatApi by lazy {
        chatbotRetrofit.create(ChatApi::class.java)
    }


    val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    // === NEW: Menu API for menu endpoints ===
    val menuApi: MenuApi = retrofit.create(MenuApi::class.java)

    // === NEW: Orders API ===
    val ordersApi: OrdersApi = retrofit.create(OrdersApi::class.java)

    // inside object NetworkClient (near other api fields)
    val adminSettingsApi: AdminSettingsApi = retrofit.create(AdminSettingsApi::class.java)

    // User Home Screen API
    val homeApi: HomeApi by lazy {
        retrofit.create(HomeApi::class.java)
    }

    // Cart API
    val cartApi: CartApi by lazy {
        retrofit.create(CartApi::class.java)
    }

    // Checkout API
    val checkoutApi: CheckoutApi = retrofit.create(CheckoutApi::class.java)

    val userSettingsApi: UserSettingsApi by lazy {
        retrofit.create(UserSettingsApi::class.java)
    }

    val editProfileApi: EditProfileApi by lazy {
        retrofit.create(EditProfileApi::class.java)
    }

    val addressApi: AddressApi by lazy {
        retrofit.create(AddressApi::class.java)
    }

    val membershipApi: MembershipApi by lazy {
        retrofit.create(MembershipApi::class.java)
    }

    // Booking API
    val bookingApi: BookingApi by lazy {
        retrofit.create(BookingApi::class.java)
    }

    // CHECKOUT SCREEN LOGIC
    val addressCheckoutApi: AddressCheckoutApi by lazy {
        retrofit.create(AddressCheckoutApi::class.java)
    }

    val checkoutSummaryApi: CheckoutSummaryApi =
        retrofit.create(CheckoutSummaryApi::class.java)

    val paymentApi: PaymentApi by lazy {
        retrofit.create(PaymentApi::class.java)
    }

    val userOrdersApi: UserOrdersApi by lazy {
        retrofit.create(UserOrdersApi::class.java)
    }

    val orderTrackingApi: OrderTrackingApi =
        retrofit.create(OrderTrackingApi::class.java)

    val adminLocationApi: AdminLocationApi by lazy {
        retrofit.create(AdminLocationApi::class.java)
    }

    // In NetworkClient.kt, add:
    val deliveryLocationApi: DeliveryLocationApi by lazy {
        retrofit.create(DeliveryLocationApi::class.java)
    }

    // Delivery concept
    val deliveryApi: DeliveryApi =
        retrofit.create(DeliveryApi::class.java)

}