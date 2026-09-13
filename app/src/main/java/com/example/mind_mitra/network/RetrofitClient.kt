package com.example.mind_mitra.network

import com.example.mind_mitra.BuildConfig
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Single Retrofit client for all backend calls.
 * BASE_URL comes from BuildConfig (Railway by default).
 */
object RetrofitClient {

    private val authInterceptor = Interceptor { chain ->
        val currentUser = FirebaseAuth.getInstance().currentUser

        val token = if (currentUser != null) {
            try {
                Tasks.await(currentUser.getIdToken(false), 10, TimeUnit.SECONDS)
                    .token
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
