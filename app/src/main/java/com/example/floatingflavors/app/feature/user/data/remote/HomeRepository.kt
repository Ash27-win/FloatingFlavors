package com.example.floatingflavors.app.feature.user.data

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.user.data.remote.dto.HomeResponseDto
import com.example.floatingflavors.app.feature.user.data.remote.dto.OfferDto
import com.example.floatingflavors.app.feature.user.data.remote.dto.UserStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeRepository {

    private val api = NetworkClient.homeApi

    suspend fun fetchHome(): RepoResult<HomeResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getHome(1)

                if (!response.isSuccessful) {
                    return@withContext RepoResult.Error("Server error ${response.code()}")
                }

                val body = response.body()
                if (body == null || body.status != "success" || body.data == null) {
                    return@withContext RepoResult.Error("Invalid response")
                }

                // ✅ IMPORTANT: data is ALREADY HomeResponseDto shape
                RepoResult.Success(
                    HomeResponseDto(
                        userStats = body.data.userStats,
                        featured = body.data.featured,
                        offer = body.data.offer
                    )
                )

            } catch (e: Exception) {
                RepoResult.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
