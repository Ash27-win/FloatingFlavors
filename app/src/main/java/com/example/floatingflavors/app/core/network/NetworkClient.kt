package com.example.floatingflavors.app.core.network

import android.content.Context
import com.example.floatingflavors.app.chatbot.ChatApi
import com.example.floatingflavors.app.feature.admin.data.remote.AdminSettingsApi
import com.example.floatingflavors.app.feature.admin.presentation.tracking.data.AdminLocationApi
import com.example.floatingflavors.app.feature.auth.data.remote.AuthApi
import com.example.floatingflavors.app.feature.delivery.data.DeliveryTrackingApi
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
    const val BASE_URL = "http://10.111.48.250/floating_flavors_api/"  //APPA WIFI
//    const val BASE_URL = "http://10.56.232.250/floating_flavors_api/"
//    const val BASE_URL = "https://wv1qhk7m-80.inc1.devtunnels.ms/floating_flavors_api/"

    const val CHATBOT_BASE_URL = "http://10.198.130.250:8000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var applicationContext: Context? = null

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    // Cookie Store for Session Persistence (Critical for PHP Sessions)
    private val cookieStore = HashMap<String, List<okhttp3.Cookie>>()

    private val cookieJar = object : okhttp3.CookieJar {
        override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<okhttp3.Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private val httpClient: OkHttpClient by lazy {
        val ctx = applicationContext ?: throw IllegalStateException("NetworkClient must be initialized with context!")
        OkHttpClient.Builder()
            .cookieJar(cookieJar) // 🔥 Enable Cookies
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(ctx))
            .authenticator(TokenAuthenticator(ctx)) // 🔥 Silent Refresh Logic
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val chatbotRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(CHATBOT_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val chatApi: ChatApi by lazy {
        chatbotRetrofit.create(ChatApi::class.java)
    }


    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    // === NEW: Menu API for menu endpoints ===
    val menuApi: MenuApi by lazy { retrofit.create(MenuApi::class.java) }

    // === NEW: Orders API ===
    val ordersApi: OrdersApi by lazy { retrofit.create(OrdersApi::class.java) }

    // inside object NetworkClient (near other api fields)
    val adminSettingsApi: AdminSettingsApi by lazy { retrofit.create(AdminSettingsApi::class.java) }

    val adminApi: com.example.floatingflavors.app.feature.admin.data.remote.AdminApi by lazy {
        retrofit.create(com.example.floatingflavors.app.feature.admin.data.remote.AdminApi::class.java)
    }

    // User Home Screen API
    val homeApi: HomeApi by lazy {
        retrofit.create(HomeApi::class.java)
    }

    // Cart API
    val cartApi: CartApi by lazy {
        retrofit.create(CartApi::class.java)
    }

    // Checkout API
    val checkoutApi: CheckoutApi by lazy { retrofit.create(CheckoutApi::class.java) }

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

    val checkoutSummaryApi: CheckoutSummaryApi by lazy {
        retrofit.create(CheckoutSummaryApi::class.java)
    }

    val paymentApi: PaymentApi by lazy {
        retrofit.create(PaymentApi::class.java)
    }

    val userOrdersApi: UserOrdersApi by lazy {
        retrofit.create(UserOrdersApi::class.java)
    }

    val orderTrackingApi: OrderTrackingApi by lazy {
        retrofit.create(OrderTrackingApi::class.java)
    }

    val adminLocationApi: AdminLocationApi by lazy {
        retrofit.create(AdminLocationApi::class.java)
    }

    // In NetworkClient.kt, add:
    val deliveryLocationApi: DeliveryLocationApi by lazy {
        retrofit.create(DeliveryLocationApi::class.java)
    }

    val deliveryTrackingApi: DeliveryTrackingApi by lazy {
        retrofit.create(DeliveryTrackingApi::class.java)
    }

    // Delivery concept
    val deliveryApi: DeliveryApi by lazy {
        retrofit.create(DeliveryApi::class.java)
    }

    val notificationApi: com.example.floatingflavors.app.feature.notification.data.remote.NotificationApi by lazy {
        retrofit.create(com.example.floatingflavors.app.feature.notification.data.remote.NotificationApi::class.java)
    }
}