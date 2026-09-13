package com.example.mind_mitra.data

data class MusicPreferenceItem(
    val id: String,
    val label: String,
    val searchQuery: String = "",
    val youtubeUrl: String = "",
    val category: String = "other"
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "label" to label,
        "searchQuery" to searchQuery,
        "youtubeUrl" to youtubeUrl,
        "category" to category
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): MusicPreferenceItem? {
            val label = (map["label"] as? String)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            return MusicPreferenceItem(
                id = (map["id"] as? String)?.takeIf { it.isNotBlank() }
                    ?: java.util.UUID.randomUUID().toString(),
                label = label,
                searchQuery = (map["searchQuery"] as? String)?.trim() ?: "",
                youtubeUrl = (map["youtubeUrl"] as? String)?.trim() ?: "",
                category = (map["category"] as? String)?.trim()?.ifBlank { "other" } ?: "other"
            )
        }

        fun parseList(raw: Any?): List<MusicPreferenceItem> {
            if (raw !is List<*>) return emptyList()
            return raw.mapNotNull { item ->
                when (item) {
                    is Map<*, *> -> fromMap(item.entries.associate { (k, v) -> k.toString() to v })
                    else -> null
                }
            }
        }
    }
}
