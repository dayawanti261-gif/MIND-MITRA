package com.example.mind_mitra.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

data class MemoryResponse(
    val user_id: String,
    val memories: List<MemoryData>
)

data class MemoryData(
    val id: String,
    val title: String?,
    val description: String?,
    val category: String?,
    val photo_path: String?,
    val photo_url: String?,
    @JsonAdapter(PeopleListDeserializer::class)
    val people: List<String>?,
    val place: String?,
    val year: Int?
)

/** Accepts backend people as either a JSON array or a comma-separated string. */
class PeopleListDeserializer : JsonDeserializer<List<String>?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<String>? {
        if (json == null || json.isJsonNull) return null
        if (json.isJsonArray) {
            return json.asJsonArray.mapNotNull {
                if (it.isJsonPrimitive) it.asString.trim().takeIf { s -> s.isNotEmpty() }
                else null
            }
        }
        if (json.isJsonPrimitive) {
            val raw = json.asString
            if (raw.isBlank()) return emptyList()
            return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        return null
    }
}

data class DeleteMemoryResponse(
    val message: String?,
    val memory_id: String?
)

data class MemoryRequest(
    val user_id: String,
    val title: String,
    val description: String,
    val category: String? = null,
    val photo_path: String? = null,
    val people: List<String>? = null,
    val place: String? = null,
    val year: Int? = null
)

data class RoutineRequest(
    val user_id: String,
    val title: String,
    val time: String,
    val days_of_week: String = "Every day",
    val enabled: Boolean = true,
    val reminder_note: String? = null
)

data class PhotoUrlResponse(
    val file_path: String,
    val signed_url: String
)

data class PhotoUploadResponse(
    val message: String?,
    val user_id: String?,
    val filename: String?,
    val content_type: String?,
    val file_path: String?
)
