package com.example.mind_mitra.network

import retrofit2.http.Query
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("memories/user/{user_id}")
    suspend fun getUserMemories(
        @Path("user_id") userId: String
    ): MemoryResponse

    @GET("photos/signed-url")
    suspend fun getPhotoUrl(
        @Query("file_path") filePath: String
    ): PhotoUrlResponse
}