package com.athreya.mathworkout.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import android.content.Context

/**
 * Network configuration for the Global Score API.
 */
object NetworkConfig {
    
    // For development, we'll use a local database service
    // In production, replace this with your actual API base URL
    private const val BASE_URL = "https://api.athreyassums.com/v1/"
    
    // Use local database service for genuine global scoring
    private const val USE_LOCAL_DATABASE_SERVICE = true
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    
    /**
     * Get the API service instance.
     * Returns either the real API service or local database service based on configuration.
     */
    fun getApiService(context: Context? = null): GlobalScoreApiService {
        return if (USE_LOCAL_DATABASE_SERVICE && context != null) {
            LocalDatabaseGlobalScoreService(context)
        } else {
            retrofit.create(GlobalScoreApiService::class.java)
        }
    }
}

/**
 * Sealed class for representing network results.
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : NetworkResult<T>()
    data class Loading<T>(val message: String = "Loading...") : NetworkResult<T>()
}

/**
 * Extension function to safely execute API calls and handle errors.
 */
suspend inline fun <T> safeApiCall(
    crossinline apiCall: suspend () -> retrofit2.Response<ApiResponse<T>>
): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true && body.data != null) {
                NetworkResult.Success(body.data)
            } else {
                NetworkResult.Error(body?.error ?: body?.message ?: "Unknown error occurred")
            }
        } else {
            NetworkResult.Error("Network error: ${response.code()} ${response.message()}", response.code())
        }
    } catch (e: Exception) {
        NetworkResult.Error("Network exception: ${e.localizedMessage ?: "Unknown error"}")
    }
}
