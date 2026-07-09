package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

class AppRepository(private val appDao: AppDao) {

    // Rickshaws
    val allRickshaws: Flow<List<Rickshaw>> = appDao.getAllRickshaws()
    
    suspend fun insertRickshaw(rickshaw: Rickshaw): Long = withContext(Dispatchers.IO) {
        appDao.insertRickshaw(rickshaw)
    }

    suspend fun deleteRickshaw(rickshaw: Rickshaw) = withContext(Dispatchers.IO) {
        appDao.deleteRickshaw(rickshaw)
    }

    suspend fun getRickshawById(id: Int): Rickshaw? = withContext(Dispatchers.IO) {
        appDao.getRickshawById(id)
    }

    // Rickshaw Revenues
    val allRickshawRevenues: Flow<List<RickshawRevenue>> = appDao.getAllRickshawRevenues()

    fun getRickshawRevenuesInRange(startDate: Long, endDate: Long): Flow<List<RickshawRevenue>> {
        return appDao.getRickshawRevenuesInRange(startDate, endDate)
    }

    suspend fun insertRickshawRevenue(revenue: RickshawRevenue) = withContext(Dispatchers.IO) {
        appDao.insertRickshawRevenue(revenue)
    }

    suspend fun deleteRickshawRevenue(revenue: RickshawRevenue) = withContext(Dispatchers.IO) {
        appDao.deleteRickshawRevenue(revenue)
    }

    // Shop Revenues
    val allShopRevenues: Flow<List<ShopRevenue>> = appDao.getAllShopRevenues()

    suspend fun insertShopRevenue(revenue: ShopRevenue) = withContext(Dispatchers.IO) {
        appDao.insertShopRevenue(revenue)
    }

    suspend fun deleteShopRevenue(revenue: ShopRevenue) = withContext(Dispatchers.IO) {
        appDao.deleteShopRevenue(revenue)
    }

    // Room Revenues
    val allRoomRevenues: Flow<List<RoomRevenue>> = appDao.getAllRoomRevenues()

    suspend fun insertRoomRevenue(revenue: RoomRevenue) = withContext(Dispatchers.IO) {
        appDao.insertRoomRevenue(revenue)
    }

    suspend fun deleteRoomRevenue(revenue: RoomRevenue) = withContext(Dispatchers.IO) {
        appDao.deleteRoomRevenue(revenue)
    }

    // Rooms
    val allRooms: Flow<List<Room>> = appDao.getAllRooms()

    suspend fun insertRoom(room: Room) = withContext(Dispatchers.IO) {
        appDao.insertRoom(room)
    }

    suspend fun deleteRoom(room: Room) = withContext(Dispatchers.IO) {
        appDao.deleteRoom(room)
    }

    // Deductions
    val allDeductions: Flow<List<Deduction>> = appDao.getAllDeductions()

    fun getDeductionsInRange(startDate: Long, endDate: Long): Flow<List<Deduction>> {
        return appDao.getDeductionsInRange(startDate, endDate)
    }

    suspend fun insertDeduction(deduction: Deduction) = withContext(Dispatchers.IO) {
        appDao.insertDeduction(deduction)
    }

    suspend fun deleteDeduction(deduction: Deduction) = withContext(Dispatchers.IO) {
        appDao.deleteDeduction(deduction)
    }

    // Reports
    val allReports: Flow<List<Report>> = appDao.getAllReports()

    suspend fun insertReport(report: Report) = withContext(Dispatchers.IO) {
        appDao.insertReport(report)
    }

    suspend fun deleteReport(report: Report) = withContext(Dispatchers.IO) {
        appDao.deleteReport(report)
    }

    // Screenshot Helper: Copies image Uri to app's internal files and returns the absolute local path.
    suspend fun copyScreenshotToInternalStorage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val dir = File(context.filesDir, "screenshots")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = "screenshot_${System.currentTimeMillis()}.jpg"
            val file = File(dir, fileName)
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- Backup & Restore Implementation ---

    suspend fun exportBackup(context: Context): String = withContext(Dispatchers.IO) {
        val rickshaws = appDao.getAllRickshaws().first()
        val rickshawRevenues = appDao.getAllRickshawRevenues().first()
        val shopRevenues = appDao.getAllShopRevenues().first()
        val roomRevenues = appDao.getAllRoomRevenues().first()
        val rooms = appDao.getAllRooms().first()
        val deductions = appDao.getAllDeductions().first()
        val reports = appDao.getAllReports().first()

        val prefs = context.getSharedPreferences("family_settings", Context.MODE_PRIVATE)
        val allPrefs = prefs.all
        val prefMap = mutableMapOf<String, String>()
        for ((key, value) in allPrefs) {
            if (value != null) {
                prefMap[key] = value.toString()
            }
        }

        val backupData = AppBackupData(
            version = 1,
            timestamp = System.currentTimeMillis(),
            rickshaws = rickshaws,
            rickshawRevenues = rickshawRevenues,
            shopRevenues = shopRevenues,
            roomRevenues = roomRevenues,
            rooms = rooms,
            deductions = deductions,
            reports = reports,
            preferences = prefMap
        )

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(AppBackupData::class.java)
        adapter.toJson(backupData)
    }

    suspend fun importBackup(context: Context, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(AppBackupData::class.java)
            val backupData = adapter.fromJson(jsonString) ?: return@withContext false

            // Clear existing database tables
            appDao.clearRickshaws()
            appDao.clearRickshawRevenues()
            appDao.clearShopRevenues()
            appDao.clearRoomRevenues()
            appDao.clearRooms()
            appDao.clearDeductions()
            appDao.clearReports()

            // Insert new data from backup
            appDao.insertAllRickshaws(backupData.rickshaws)
            appDao.insertAllRickshawRevenues(backupData.rickshawRevenues)
            appDao.insertAllShopRevenues(backupData.shopRevenues)
            appDao.insertAllRoomRevenues(backupData.roomRevenues)
            appDao.insertAllRooms(backupData.rooms)
            appDao.insertAllDeductions(backupData.deductions)
            appDao.insertAllReports(backupData.reports)

            // Restore SharedPreferences
            val prefs = context.getSharedPreferences("family_settings", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            for ((key, value) in backupData.preferences) {
                if (key.startsWith("family_portion_") || key.startsWith("family_saved_balance_")) {
                    value.toFloatOrNull()?.let { editor.putFloat(key, it) }
                } else if (key.startsWith("week_reported_")) {
                    editor.putBoolean(key, value.toBoolean())
                } else {
                    editor.putString(key, value)
                }
            }
            editor.apply()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun uploadBackupToCloud(jsonString: String): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "backup_${System.currentTimeMillis()}.json",
                    jsonString.toRequestBody("application/json".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("https://file.io")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyStr = response.body?.string() ?: return@withContext null
                
                // Parse the response using Moshi
                val moshi = Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .build()
                val mapAdapter = moshi.adapter(Map::class.java)
                val jsonMap = mapAdapter.fromJson(bodyStr)
                
                val success = jsonMap?.get("success") as? Boolean
                if (success == true) {
                    jsonMap["link"] as? String
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun downloadBackupFromCloud(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun sendPdfToTelegram(
        botToken: String,
        chatId: String,
        pdfBytes: ByteArray,
        fileName: String,
        caption: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val mediaType = "application/pdf".toMediaTypeOrNull()
            val fileBody = pdfBytes.toRequestBody(mediaType)
            
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("document", fileName, fileBody)
                
            if (caption != null) {
                builder.addFormDataPart("caption", caption)
            }
            
            val requestBody = builder.build()
            val request = Request.Builder()
                .url("https://api.telegram.org/bot$botToken/sendDocument")
                .post(requestBody)
                .build()
                
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
