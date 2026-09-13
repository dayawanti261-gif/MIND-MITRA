package com.example.mind_mitra.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException
import java.io.IOException

object LinkPatientHelper {

    fun mapError(throwable: Throwable, fallbackPatientNotFound: String, fallbackInvalidPin: String, fallbackNetwork: String, fallbackServer: String): String {
        if (throwable is IOException) {
            return fallbackNetwork
        }
        if (throwable is HttpException) {
            val body = throwable.response()?.errorBody()?.string()
            val detail = parseDetail(body)
            if (!detail.isNullOrBlank()) return detail
            return when (throwable.code()) {
                404 -> fallbackPatientNotFound
                401 -> fallbackInvalidPin
                409 -> detail ?: fallbackServer
                403 -> fallbackServer
                in 500..599 -> fallbackServer
                else -> fallbackServer
            }
        }
        val message = throwable.message ?: ""
        if (message.contains("PERMISSION_DENIED", ignoreCase = true)) {
            return fallbackServer
        }
        return fallbackServer
    }

    private fun parseDetail(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val json = Gson().fromJson(body, JsonObject::class.java)
            json.get("detail")?.asString
        } catch (_: Exception) {
            null
        }
    }
}
