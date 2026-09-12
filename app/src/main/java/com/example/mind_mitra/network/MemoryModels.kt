package com.example.mind_mitra.network

data class MemoryResponse(
    val user_id: String,
    val memories: List<MemoryData>
)

data class MemoryData(
    val memory_id: String,
    val user_id: String,
    val title: String?,
    val description: String?,
    val category: String?,
    val photo_path: String?,
    val photo_url: String?,
    val people: String?,
    val place: String?,
    val year: Int?
)