package com.example.floatingflavors.app.core.network


import com.example.floatingflavors.app.feature.auth.data.remote.AuthApi
import com.example.floatingflavors.app.feature.menu.data.remote.MenuApi // NEW
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {

    // For Android emulator, localhost = 10.0.2.2
//    const val BASE_URL = "http://10.114.15.250/floating_flavors_api/"  //APPA WIFI
    const val BASE_URL = "http://10.88.233.250/floating_flavors_api/"
//    const val BASE_URL = "https://wv1qhk7m-80.inc1.devtunnels.ms/floating_flavors_api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    // === NEW: Menu API for menu endpoints ===
    val menuApi: MenuApi = retrofit.create(MenuApi::class.java)
}
