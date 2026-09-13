package com.example.mind_mitra.network

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    // NEW: attaches "Authorization: Bearer <Firebase ID token>" to every
    // request. The backend's get_current_uid now requires this header on
    // memories/games/routine endpoints — without it every call returns 401,
    // which was being swallowed silently and showing up as "no memories found".
    private val authInterceptor = Interceptor { chain ->
        val currentUser = FirebaseAuth.getInstance().currentUser

        val token = if (currentUser != null) {
            try {
                // Blocking is fine here — this interceptor runs on OkHttp's
                // background dispatcher thread, not the main thread.
                Tasks.await(currentUser.getIdToken(false), 10, TimeUnit.SECONDS)
                    .token
            } catch (e: Exception) {
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
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}