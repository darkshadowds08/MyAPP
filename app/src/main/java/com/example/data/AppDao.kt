package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Rickshaws
    @Query("SELECT * FROM rickshaws ORDER BY name ASC")
    fun getAllRickshaws(): Flow<List<Rickshaw>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRickshaw(rickshaw: Rickshaw): Long

    @Delete
    suspend fun deleteRickshaw(rickshaw: Rickshaw)

    @Query("SELECT * FROM rickshaws WHERE id = :id LIMIT 1")
    suspend fun getRickshawById(id: Int): Rickshaw?

    // Rickshaw Revenues
    @Query("SELECT * FROM rickshaw_revenues ORDER BY date DESC")
    fun getAllRickshawRevenues(): Flow<List<RickshawRevenue>>

    @Query("SELECT * FROM rickshaw_revenues WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getRickshawRevenuesInRange(startDate: Long, endDate: Long): Flow<List<RickshawRevenue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRickshawRevenue(revenue: RickshawRevenue)

    @Delete
    suspend fun deleteRickshawRevenue(revenue: RickshawRevenue)

    // Shop Revenues
    @Query("SELECT * FROM shop_revenues ORDER BY date DESC")
    fun getAllShopRevenues(): Flow<List<ShopRevenue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopRevenue(revenue: ShopRevenue)

    @Delete
    suspend fun deleteShopRevenue(revenue: ShopRevenue)

    // Room Revenues
    @Query("SELECT * FROM room_revenues ORDER BY date DESC")
    fun getAllRoomRevenues(): Flow<List<RoomRevenue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomRevenue(revenue: RoomRevenue)

    @Delete
    suspend fun deleteRoomRevenue(revenue: RoomRevenue)

    // Rooms
    @Query("SELECT * FROM rooms ORDER BY id ASC")
    fun getAllRooms(): Flow<List<Room>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: Room): Long

    @Delete
    suspend fun deleteRoom(room: Room)

    @Query("SELECT * FROM rooms WHERE id = :id")
    suspend fun getRoomById(id: Int): Room?

    // Deductions
    @Query("SELECT * FROM deductions ORDER BY date DESC")
    fun getAllDeductions(): Flow<List<Deduction>>

    @Query("SELECT * FROM deductions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getDeductionsInRange(startDate: Long, endDate: Long): Flow<List<Deduction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeduction(deduction: Deduction)

    @Delete
    suspend fun deleteDeduction(deduction: Deduction)

    // Reports
    @Query("SELECT * FROM reports ORDER BY date DESC")
    fun getAllReports(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Delete
    suspend fun deleteReport(report: Report)

    // Batch operations for Backup & Restore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRickshaws(list: List<Rickshaw>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRickshawRevenues(list: List<RickshawRevenue>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllShopRevenues(list: List<ShopRevenue>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRoomRevenues(list: List<RoomRevenue>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRooms(list: List<Room>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDeductions(list: List<Deduction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReports(list: List<Report>)

    @Query("DELETE FROM rickshaws")
    suspend fun clearRickshaws()

    @Query("DELETE FROM rickshaw_revenues")
    suspend fun clearRickshawRevenues()

    @Query("DELETE FROM shop_revenues")
    suspend fun clearShopRevenues()

    @Query("DELETE FROM room_revenues")
    suspend fun clearRoomRevenues()

    @Query("DELETE FROM rooms")
    suspend fun clearRooms()

    @Query("DELETE FROM deductions")
    suspend fun clearDeductions()

    @Query("DELETE FROM reports")
    suspend fun clearReports()
}
