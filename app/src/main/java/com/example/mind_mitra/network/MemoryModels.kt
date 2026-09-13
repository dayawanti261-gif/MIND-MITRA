package com.example.mind_mitra.network

data class MemoryResponse(
    val user_id: String,
    val memories: List<MemoryData>
)

data class MemoryData(
    // CHANGED: backend's memories.py now returns "id" (matching the
    // pattern used for games/routines), not "memory_id".
    val id: String,
    val title: String?,
    val description: String?,
    val category: String?,
    val photo_path: String?,
    val photo_url: String?,
    val people: String?,
    val place: String?,
    val year: Int?
)