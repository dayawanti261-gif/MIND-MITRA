package com.example.mind_mitra.data

/**
 * Compatibility aliases — all HTTP traffic goes through
 * [com.example.mind_mitra.network.RetrofitClient].
 */
@Deprecated("Use com.example.mind_mitra.network.RetrofitClient.apiService")
object ApiClient {
    val service get() = com.example.mind_mitra.network.RetrofitClient.apiService
}

typealias MemoryRequest = com.example.mind_mitra.network.MemoryRequest
typealias RoutineRequest = com.example.mind_mitra.network.RoutineRequest
