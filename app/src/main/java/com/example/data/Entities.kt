package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rickshaws")
data class Rickshaw(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val driverName: String
)

@Entity(tableName = "rickshaw_revenues")
data class RickshawRevenue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rickshawId: Int,
    val amount: Double,
    val date: Long,
    val notes: String = "",
    val screenshotPath: String? = null
)

@Entity(tableName = "shop_revenues")
data class ShopRevenue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val date: Long,
    val monthString: String, // format "YYYY-MM"
    val notes: String = "",
    val screenshotPath: String? = null
)

@Entity(tableName = "deductions")
data class Deduction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val description: String,
    val category: String, // "RICKSHAW", "SHOP", "GENERAL"
    val targetId: Int? = null, // specific Rickshaw ID if related to a rickshaw
    val date: Long,
    val screenshotPath: String? = null
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: Long,
    val screenshotPath: String? = null
)

@Entity(tableName = "room_revenues")
data class RoomRevenue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomName: String,
    val amount: Double,
    val date: Long,
    val monthString: String, // format "YYYY-MM"
    val notes: String = "",
    val screenshotPath: String? = null
)

@Entity(tableName = "rooms")
data class Room(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val tenantName: String
)
