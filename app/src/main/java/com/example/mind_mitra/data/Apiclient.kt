package com.example.mind_mitra.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File

// ============================================================================
// ATTACHES THE CALLER'S FIREBASE ID TOKEN TO EVERY REQUEST
// ============================================================================
// The backend's get_current_uid() dependency expects:
//     Authorization: Bearer <idToken>
// Without this every call gets HTTP 401.
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val user = FirebaseAuth.getInstance().currentUser
        val token = user?.let {
            // NOTE: this blocks the calling thread briefly. In production,
            // fetch the token with `.await()` in a suspend function *before*
            // building the request instead of inside the interceptor.
            kotlinx.coroutines.runBlocking { it.getIdToken(false).await().token }
        }

        val request = chain.request().newBuilder().apply {
            if (token != null) addHeader("Authorization", "Bearer $token")
        }.build()

        return chain.proceed(request)
    }
}

data class MemoryRequest(
    val user_id: String,
    val title: String,
    val description: String,
    val category: String = "",
    val imageUrl: String? = null,
    val people: List<String> = emptyList()
)

data class RoutineRequest(
    val user_id: String,
    val title: String,
    val time: String
)

interface MindMitraApi {

    @POST("memories/")
    suspend fun addMemory(@Body body: MemoryRequest)

    @POST("routine/")
    suspend fun addRoutine(@Body body: RoutineRequest)

    @Multipart
    @POST("photos/upload")
    suspend fun uploadPhoto(
        @Part("user_id") userId: okhttp3.RequestBody,
        @Part("title") title: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody,
        @Part file: MultipartBody.Part
    )
}

object ApiClient {

    // CHANGE THIS to your deployed backend URL (or 10.0.2.2 for the
    // Android emulator talking to a backend running on your own machine).
    private const val BASE_URL = "https://mind-mitra-production.up.railway.app/"

    val service: MindMitraApi by lazy {
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MindMitraApi::class.java)
    }
}