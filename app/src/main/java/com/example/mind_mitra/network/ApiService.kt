package com.example.mind_mitra.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Single Retrofit contract for the Railway (or local) FastAPI backend.
 */
interface ApiService {

    @GET("memories/user/{user_id}")
    suspend fun getUserMemories(
        @Path("user_id") userId: String
    ): MemoryResponse

    @POST("memories/")
    suspend fun addMemory(
        @Body body: MemoryRequest
    )

    @POST("routine/")
    suspend fun addRoutine(
        @Body body: RoutineRequest
    )

    @Multipart
    @POST("photos/upload")
    suspend fun uploadPhoto(
        @Part("user_id") userId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("people") people: RequestBody,
        @Part("category") category: RequestBody,
        @Part file: MultipartBody.Part
    ): PhotoUploadResponse

    @GET("photos/signed-url")
    suspend fun getPhotoUrl(
        @Query("file_path") filePath: String
    ): PhotoUrlResponse

    @GET("routine/user/{user_id}")
    suspend fun getUserRoutine(
        @Path("user_id") userId: String
    ): RoutineResponse

    @GET("games/progress/{user_id}")
    suspend fun getGameProgress(
        @Path("user_id") userId: String
    ): GameProgressResponse

    @POST("games/progress")
    suspend fun saveGameProgress(
        @Body body: GameProgressRequest
    )

    @POST("api/agent/chat")
    suspend fun chatWithAgent(
        @Body body: AgentChatRequest
    ): AgentChatResponse
}
