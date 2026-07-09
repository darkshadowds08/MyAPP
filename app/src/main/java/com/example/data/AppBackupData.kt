package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppBackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val rickshaws: List<Rickshaw> = emptyList(),
    val rickshawRevenues: List<RickshawRevenue> = emptyList(),
    val shopRevenues: List<ShopRevenue> = emptyList(),
    val roomRevenues: List<RoomRevenue> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val deductions: List<Deduction> = emptyList(),
    val reports: List<Report> = emptyList(),
    val preferences: Map<String, String> = emptyMap()
)
