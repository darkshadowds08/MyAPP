package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    val context: Context get() = getApplication()

    // Base flows
    val allRickshaws: StateFlow<List<Rickshaw>> = repository.allRickshaws
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRickshawRevenues: StateFlow<List<RickshawRevenue>> = repository.allRickshawRevenues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShopRevenues: StateFlow<List<ShopRevenue>> = repository.allShopRevenues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRoomRevenues: StateFlow<List<RoomRevenue>> = repository.allRoomRevenues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRooms: StateFlow<List<Room>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeductions: StateFlow<List<Deduction>> = repository.allDeductions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<Report>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Family configurations stored in SharedPreferences
    data class FamilyConfig(
        val id: Int,
        val name: String,
        val portion: Double
    )

    private val prefs = application.getSharedPreferences("family_settings", Context.MODE_PRIVATE)

    private val _familiesState = MutableStateFlow<List<FamilyConfig>>(emptyList())
    val familiesState: StateFlow<List<FamilyConfig>> = _familiesState.asStateFlow()

    private val _paidFamiliesMapState = MutableStateFlow<Map<String, Set<Int>>>(emptyMap())
    val paidFamiliesMapState: StateFlow<Map<String, Set<Int>>> = _paidFamiliesMapState.asStateFlow()

    private val _familySavedBalancesState = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val familySavedBalancesState: StateFlow<Map<Int, Double>> = _familySavedBalancesState.asStateFlow()

    private val _reportedWeeksState = MutableStateFlow<Set<String>>(emptySet())
    val reportedWeeksState: StateFlow<Set<String>> = _reportedWeeksState.asStateFlow()

    fun loadReportedWeeks() {
        val keys = prefs.all.keys.filter { it.startsWith("week_reported_") }
        val set = keys.filter { prefs.getBoolean(it, false) }.map { it.removePrefix("week_reported_") }.toSet()
        _reportedWeeksState.value = set
    }

    fun loadPaidFamilies() {
        val keys = prefs.all.keys.filter { it.startsWith("paid_families_for_week_") }
        val map = mutableMapOf<String, Set<Int>>()
        for (key in keys) {
            val weekKey = key.removePrefix("paid_families_for_week_")
            val str = prefs.getString(key, null)
            val set = if (str.isNullOrBlank()) emptySet() else str.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            map[weekKey] = set
        }
        _paidFamiliesMapState.value = map
    }

    fun getPaidFamilies(weekKey: String): Set<Int> {
        val str = prefs.getString("paid_families_for_week_$weekKey", null) ?: return emptySet()
        if (str.isBlank()) return emptySet()
        return str.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleFamilyPayment(weekKey: String, familyId: Int, isPaid: Boolean) {
        val current = getPaidFamilies(weekKey).toMutableSet()
        if (isPaid) {
            current.add(familyId)
        } else {
            current.remove(familyId)
        }
        prefs.edit().putString("paid_families_for_week_$weekKey", current.joinToString(",")).apply()
        _paidFamiliesMapState.value = _paidFamiliesMapState.value.toMutableMap().apply {
            put(weekKey, current)
        }
    }

    fun loadFamilySavedBalances() {
        val map = mutableMapOf<Int, Double>()
        val families = _familiesState.value
        for (f in families) {
            val saved = prefs.getFloat("family_saved_balance_${f.id}", 0f).toDouble()
            map[f.id] = saved
        }
        _familySavedBalancesState.value = map
    }

    fun payAmountFromSavedBalance(familyId: Int, amount: Double) {
        val current = prefs.getFloat("family_saved_balance_$familyId", 0f).toDouble()
        val remaining = (current - amount).coerceAtLeast(0.0)
        prefs.edit().putFloat("family_saved_balance_$familyId", remaining.toFloat()).apply()
        loadFamilySavedBalances()
    }

    fun clearFamilySavedBalance(familyId: Int) {
        prefs.edit().putFloat("family_saved_balance_$familyId", 0f).apply()
        loadFamilySavedBalances()
    }

    fun generateWeeklyDistributionReport(summary: WeeklySummary) {
        val weekKey = summary.weekKey
        val alreadyReported = prefs.getBoolean("week_reported_$weekKey", false)
        if (alreadyReported) return

        val now = System.currentTimeMillis()
        val families = familiesState.value
        if (families.isEmpty()) return

        val paidIds = getPaidFamilies(weekKey)
        val title = "تقرير أوتوماتيكي: إحصائية توزيع الأسر لـ $weekKey"
        
        val sb = StringBuilder()
        sb.append("تقرير إحصائية توزيع الأسر للأسبوع المنتهي.\n\n")
        sb.append("إحصائيات الأسبوع:\n")
        sb.append("- صافي الربح للتوزيع: ${String.format(Locale.US, "%,.2f", summary.netRevenue)} SDG\n")
        val totalFamiliesPayout = families.sumOf { it.portion }
        sb.append("- إجمالي أنصبة الأسر المحددة: ${String.format(Locale.US, "%,.2f", totalFamiliesPayout)} SDG\n\n")
        sb.append("حالة توزيع العائلات:\n")
        sb.append("-----------------------------\n")

        val editor = prefs.edit()
        for (family in families) {
            val isPaid = paidIds.contains(family.id)
            val familyShare = family.portion
            
            if (family.portion <= 0) {
                sb.append("- ${family.name}: غير نشطة (0 جنيه)\n")
                continue
            }

            if (isPaid) {
                sb.append("- ${family.name} (المبلغ المحدد: ${String.format(Locale.US, "%,.0f", family.portion)} SDG): تم الاستلام\n")
            } else {
                val currentSaved = prefs.getFloat("family_saved_balance_${family.id}", 0f).toDouble()
                val newSaved = currentSaved + familyShare
                editor.putFloat("family_saved_balance_${family.id}", newSaved.toFloat())
                
                sb.append("- ${family.name} (المبلغ المحدد: ${String.format(Locale.US, "%,.0f", family.portion)} SDG): لم يتم الاستلام ❌ (تم حفظ وحجز نصيبها البالغ ${String.format(Locale.US, "%,.0f", familyShare)} SDG وتراكمه في حسابها)\n")
            }
        }
        
        editor.putBoolean("week_reported_$weekKey", true)
        editor.apply()

        sb.append("\n-----------------------------\n")
        sb.append("الأرصدة المحفوظة الإجمالية للأسر بعد تحديث هذا الأسبوع:\n")
        for (family in families) {
            if (family.portion > 0) {
                val totalSaved = prefs.getFloat("family_saved_balance_${family.id}", 0f).toDouble()
                sb.append("- ${family.name}: رصيد محفوظ متراكم = ${String.format(Locale.US, "%,.0f", totalSaved)} SDG\n")
            }
        }

        val reportContent = sb.toString()
        viewModelScope.launch {
            repository.insertReport(
                Report(
                    title = title,
                    content = reportContent,
                    date = now
                )
            )
        }
        
        loadFamilySavedBalances()
        loadReportedWeeks()
    }

    fun checkAndGenerateAutomaticReports(summaries: List<WeeklySummary>) {
        val now = System.currentTimeMillis()
        for (summary in summaries) {
            val weekEnd = summary.startTimestamp + 7 * 24 * 60 * 60 * 1000L
            val weekKey = summary.weekKey
            val alreadyReported = prefs.getBoolean("week_reported_$weekKey", false)
            if (now >= weekEnd && !alreadyReported) {
                generateWeeklyDistributionReport(summary)
            }
        }
    }

    fun loadFamilies() {
        val list = mutableListOf<FamilyConfig>()
        val idsStr = prefs.getString("family_ids", "1,2,3,4,5,6,7,8,9") ?: "1,2,3,4,5,6,7,8,9"
        val ids = if (idsStr.isBlank()) emptyList() else idsStr.split(",").mapNotNull { it.toIntOrNull() }
        for (id in ids) {
            val name = prefs.getString("family_name_$id", "أسرة $id") ?: "أسرة $id"
            val portion = prefs.getFloat("family_portion_$id", 5000.0f).toDouble()
            list.add(FamilyConfig(id = id, name = name, portion = portion))
        }
        _familiesState.value = list
        loadFamilySavedBalances()
    }

    fun saveFamily(id: Int, name: String, portion: Double) {
        prefs.edit().apply {
            putString("family_name_$id", name)
            putFloat("family_portion_$id", portion.toFloat())
            apply()
        }
        loadFamilies()
    }

    fun saveFamilies(familiesList: List<FamilyConfig>) {
        val idsStr = familiesList.map { it.id }.joinToString(",")
        prefs.edit().apply {
            putString("family_ids", idsStr)
            for (f in familiesList) {
                putString("family_name_${f.id}", f.name)
                putFloat("family_portion_${f.id}", f.portion.toFloat())
            }
            apply()
        }
        loadFamilies()
    }

    // ------------------ Calculations & Summaries ------------------

    // Group rickshaw revenues and deductions by week
    data class WeeklySummary(
        val weekKey: String,          // formatted range
        val startTimestamp: Long,
        val totalRevenue: Double,
        val totalDeductions: Double,
        val netRevenue: Double,
        val sharePerFamily: Double,   // base share unit (netRevenue / totalPortions)
        val revenues: List<RickshawRevenue>,
        val deductions: List<Deduction>
    )

    val weeklyRickshawSummaries: StateFlow<List<WeeklySummary>> = combine(
        allRickshawRevenues,
        allDeductions,
        familiesState
    ) { revenues, deductions, families ->
        val summaries = mutableMapOf<String, MutableList<RickshawRevenue>>()
        val weekStarts = mutableMapOf<String, Long>()

        // Group revenues by week
        for (rev in revenues) {
            val weekKey = getWeekRangeString(rev.date)
            summaries.getOrPut(weekKey) { mutableListOf() }.add(rev)
            if (!weekStarts.containsKey(weekKey) || rev.date < weekStarts[weekKey]!!) {
                weekStarts[weekKey] = rev.date
            }
        }

        // Group deductions of category "RICKSHAW" by week
        val rickshawDeductions = deductions.filter { it.category == "RICKSHAW" }
        val deductionsByWeek = mutableMapOf<String, MutableList<Deduction>>()
        for (ded in rickshawDeductions) {
            val weekKey = getWeekRangeString(ded.date)
            deductionsByWeek.getOrPut(weekKey) { mutableListOf() }.add(ded)
            if (!weekStarts.containsKey(weekKey) || ded.date < weekStarts[weekKey]!!) {
                weekStarts[weekKey] = ded.date
            }
        }

        // Create weekly summaries
        val keys = (summaries.keys + deductionsByWeek.keys).distinct()
        val totalPortions = families.sumOf { it.portion }
        keys.map { weekKey ->
            val revList = summaries[weekKey] ?: emptyList()
            val dedList = deductionsByWeek[weekKey] ?: emptyList()
            val totalRev = revList.sumOf { it.amount }
            val totalDed = dedList.sumOf { it.amount }
            val net = totalRev - totalDed
            val share = if (net > 0 && totalPortions > 0) net / totalPortions else 0.0

            WeeklySummary(
                weekKey = weekKey,
                startTimestamp = weekStarts[weekKey] ?: 0L,
                totalRevenue = totalRev,
                totalDeductions = totalDed,
                netRevenue = net,
                sharePerFamily = share,
                revenues = revList,
                deductions = dedList
            )
        }.sortedByDescending { it.startTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Group shop revenues and deductions by month
    data class MonthlySummary(
        val monthKey: String,         // e.g., "يوليو 2026"
        val startTimestamp: Long,
        val totalRevenue: Double,
        val totalDeductions: Double,
        val netRevenue: Double,
        val revenues: List<ShopRevenue>,
        val deductions: List<Deduction>
    )

    val monthlyShopSummaries: StateFlow<List<MonthlySummary>> = combine(
        allShopRevenues,
        allDeductions
    ) { revenues, deductions ->
        val summaries = mutableMapOf<String, MutableList<ShopRevenue>>()
        val monthStarts = mutableMapOf<String, Long>()

        // Group revenues by month
        for (rev in revenues) {
            val monthKey = getArabicMonth(rev.date)
            summaries.getOrPut(monthKey) { mutableListOf() }.add(rev)
            if (!monthStarts.containsKey(monthKey) || rev.date < monthStarts[monthKey]!!) {
                monthStarts[monthKey] = rev.date
            }
        }

        // Group deductions of category "SHOP" by month
        val shopDeductions = deductions.filter { it.category == "SHOP" }
        val deductionsByMonth = mutableMapOf<String, MutableList<Deduction>>()
        for (ded in shopDeductions) {
            val monthKey = getArabicMonth(ded.date)
            deductionsByMonth.getOrPut(monthKey) { mutableListOf() }.add(ded)
            if (!monthStarts.containsKey(monthKey) || ded.date < monthStarts[monthKey]!!) {
                monthStarts[monthKey] = ded.date
            }
        }

        // Create monthly summaries
        val keys = (summaries.keys + deductionsByMonth.keys).distinct()
        keys.map { monthKey ->
            val revList = summaries[monthKey] ?: emptyList()
            val dedList = deductionsByMonth[monthKey] ?: emptyList()
            val totalRev = revList.sumOf { it.amount }
            val totalDed = dedList.sumOf { it.amount }
            val net = totalRev - totalDed

            MonthlySummary(
                monthKey = monthKey,
                startTimestamp = monthStarts[monthKey] ?: 0L,
                totalRevenue = totalRev,
                totalDeductions = totalDed,
                netRevenue = net,
                revenues = revList,
                deductions = dedList
            )
        }.sortedByDescending { it.startTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class RoomMonthlySummary(
        val monthKey: String,
        val startTimestamp: Long,
        val totalRevenue: Double,
        val totalDeductions: Double,
        val netRevenue: Double,
        val revenues: List<RoomRevenue>,
        val deductions: List<Deduction>
    )

    val monthlyRoomSummaries: StateFlow<List<RoomMonthlySummary>> = combine(
        allRoomRevenues,
        allDeductions
    ) { revenues, deductions ->
        val summaries = mutableMapOf<String, MutableList<RoomRevenue>>()
        val monthStarts = mutableMapOf<String, Long>()

        // Group revenues by month
        for (rev in revenues) {
            val monthKey = getArabicMonth(rev.date)
            summaries.getOrPut(monthKey) { mutableListOf() }.add(rev)
            if (!monthStarts.containsKey(monthKey) || rev.date < monthStarts[monthKey]!!) {
                monthStarts[monthKey] = rev.date
            }
        }

        // Group deductions of category "ROOM" by month
        val roomDeductions = deductions.filter { it.category == "ROOM" }
        val deductionsByMonth = mutableMapOf<String, MutableList<Deduction>>()
        for (ded in roomDeductions) {
            val monthKey = getArabicMonth(ded.date)
            deductionsByMonth.getOrPut(monthKey) { mutableListOf() }.add(ded)
            if (!monthStarts.containsKey(monthKey) || ded.date < monthStarts[monthKey]!!) {
                monthStarts[monthKey] = ded.date
            }
        }

        // Create monthly summaries
        val keys = (summaries.keys + deductionsByMonth.keys).distinct()
        keys.map { monthKey ->
            val revList = summaries[monthKey] ?: emptyList()
            val dedList = deductionsByMonth[monthKey] ?: emptyList()
            val totalRev = revList.sumOf { it.amount }
            val totalDed = dedList.sumOf { it.amount }
            val net = totalRev - totalDed

            RoomMonthlySummary(
                monthKey = monthKey,
                startTimestamp = monthStarts[monthKey] ?: 0L,
                totalRevenue = totalRev,
                totalDeductions = totalDed,
                netRevenue = net,
                revenues = revList,
                deductions = dedList
            )
        }.sortedByDescending { it.startTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadFamilies()
        loadPaidFamilies()
        loadFamilySavedBalances()
        loadReportedWeeks()

        // Automatically monitor weekly summaries to check and generate reports for completed weeks
        viewModelScope.launch {
            weeklyRickshawSummaries.collect { summaries ->
                checkAndGenerateAutomaticReports(summaries)
            }
        }
    }

    // ------------------ Database Insertions & Deletions ------------------

    fun addRickshaw(name: String, driverName: String) {
        viewModelScope.launch {
            repository.insertRickshaw(Rickshaw(name = name, driverName = driverName))
        }
    }

    fun updateRickshaw(rickshaw: Rickshaw) {
        viewModelScope.launch {
            repository.insertRickshaw(rickshaw)
        }
    }

    fun deleteRickshaw(rickshaw: Rickshaw) {
        viewModelScope.launch {
            repository.deleteRickshaw(rickshaw)
        }
    }

    fun addRoom(name: String, tenantName: String) {
        viewModelScope.launch {
            repository.insertRoom(Room(name = name, tenantName = tenantName))
        }
    }

    fun updateRoom(room: Room) {
        viewModelScope.launch {
            repository.insertRoom(room)
        }
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch {
            repository.deleteRoom(room)
        }
    }

    fun addRickshawRevenue(
        rickshawId: Int,
        amount: Double,
        date: Long,
        notes: String,
        screenshotUri: Uri?
    ) {
        viewModelScope.launch {
            val path = screenshotUri?.let { repository.copyScreenshotToInternalStorage(context, it) }
            repository.insertRickshawRevenue(
                RickshawRevenue(
                    rickshawId = rickshawId,
                    amount = amount,
                    date = date,
                    notes = notes,
                    screenshotPath = path
                )
            )

            // Auto generate report
            val rickshaw = repository.getRickshawById(rickshawId)
            val rName = rickshaw?.name ?: "الركشة رقم $rickshawId"
            val rDriver = rickshaw?.driverName?.let { " ($it)" } ?: ""
            val formattedDate = formatSimpleDate(date)
            val reportTitle = "تقرير إيراد تلقائي: $rName بتاريخ $formattedDate"
            val reportContent = """
                تم تسجيل إيراد جديد تلقائياً في النظام.

                تفاصيل الإيراد:
                ---------------------------
                - المصدر: $rName$rDriver
                - المبلغ: ${String.format(Locale.US, "%,.2f", amount)} SDG
                - التاريخ: $formattedDate
                - ملاحظات: ${notes.ifBlank { "لا توجد" }}
                ---------------------------
                تم حفظ هذا التقرير تلقائياً وتوثيقه لتسهيل مراجعة حسابات الإيرادات والمصروفات.
            """.trimIndent()

            repository.insertReport(
                Report(
                    title = reportTitle,
                    content = reportContent,
                    date = System.currentTimeMillis(),
                    screenshotPath = path
                )
            )
        }
    }

    fun deleteRickshawRevenue(revenue: RickshawRevenue) {
        viewModelScope.launch {
            repository.deleteRickshawRevenue(revenue)
        }
    }

    fun addShopRevenue(
        amount: Double,
        date: Long,
        notes: String,
        screenshotUri: Uri?
    ) {
        viewModelScope.launch {
            val path = screenshotUri?.let { repository.copyScreenshotToInternalStorage(context, it) }
            val monthStr = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(date))
            repository.insertShopRevenue(
                ShopRevenue(
                    amount = amount,
                    date = date,
                    monthString = monthStr,
                    notes = notes,
                    screenshotPath = path
                )
            )

            // Auto generate report
            val formattedDate = formatSimpleDate(date)
            val monthName = getArabicMonth(date)
            val reportTitle = "تقرير إيجار دكان تلقائي: لشهر $monthName بتاريخ $formattedDate"
            val reportContent = """
                تم تسجيل إيجار الدكان (إيراد الدكان الشهري) تلقائياً في النظام.

                تفاصيل الإيجار:
                ---------------------------
                - المصدر: إيجار دكان شهري
                - الشهر المستهدف: $monthName
                - المبلغ المستلم: ${String.format(Locale.US, "%,.2f", amount)} SDG
                - التاريخ: $formattedDate
                - ملاحظات: ${notes.ifBlank { "لا توجد" }}
                ---------------------------
                تم حفظ هذا التقرير تلقائياً وتوثيقه لتعزيز الشفافية والمسؤولية في الحسابات الشهرية.
            """.trimIndent()

            repository.insertReport(
                Report(
                    title = reportTitle,
                    content = reportContent,
                    date = System.currentTimeMillis(),
                    screenshotPath = path
                )
            )
        }
    }

    fun deleteShopRevenue(revenue: ShopRevenue) {
        viewModelScope.launch {
            repository.deleteShopRevenue(revenue)
        }
    }

    fun addRoomRevenue(
        roomName: String,
        amount: Double,
        date: Long,
        notes: String,
        screenshotUri: Uri?
    ) {
        viewModelScope.launch {
            val path = screenshotUri?.let { repository.copyScreenshotToInternalStorage(context, it) }
            val monthStr = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(date))
            repository.insertRoomRevenue(
                RoomRevenue(
                    roomName = roomName,
                    amount = amount,
                    date = date,
                    monthString = monthStr,
                    notes = notes,
                    screenshotPath = path
                )
            )

            // Auto generate report
            val formattedDate = formatSimpleDate(date)
            val monthName = getArabicMonth(date)
            val reportTitle = "تقرير إيجار غرفة تلقائي: لـ ($roomName) لشهر $monthName بتاريخ $formattedDate"
            val reportContent = """
                تم تسجيل إيجار غرفة جديد تلقائياً في النظام.

                تفاصيل الإيجار:
                ---------------------------
                - اسم الغرفة / المستأجر: $roomName
                - الشهر المستهدف: $monthName
                - المبلغ المستلم: ${String.format(Locale.US, "%,.2f", amount)} SDG
                - التاريخ: $formattedDate
                - ملاحظات: ${notes.ifBlank { "لا توجد" }}
                ---------------------------
                تم حفظ هذا التقرير تلقائياً وتوثيقه لتعزيز الشفافية والمسؤولية في الحسابات الشهرية.
            """.trimIndent()

            repository.insertReport(
                Report(
                    title = reportTitle,
                    content = reportContent,
                    date = System.currentTimeMillis(),
                    screenshotPath = path
                )
            )
        }
    }

    fun deleteRoomRevenue(revenue: RoomRevenue) {
        viewModelScope.launch {
            repository.deleteRoomRevenue(revenue)
        }
    }

    fun addDeduction(
        amount: Double,
        description: String,
        category: String,
        targetId: Int?,
        date: Long,
        screenshotUri: Uri?
    ) {
        viewModelScope.launch {
            val path = screenshotUri?.let { repository.copyScreenshotToInternalStorage(context, it) }
            repository.insertDeduction(
                Deduction(
                    amount = amount,
                    description = description,
                    category = category,
                    targetId = targetId,
                    date = date,
                    screenshotPath = path
                )
            )
        }
    }

    fun deleteDeduction(deduction: Deduction) {
        viewModelScope.launch {
            repository.deleteDeduction(deduction)
        }
    }

    fun addReport(
        title: String,
        content: String,
        date: Long,
        screenshotUri: Uri?
    ) {
        viewModelScope.launch {
            val path = screenshotUri?.let { repository.copyScreenshotToInternalStorage(context, it) }
            repository.insertReport(
                Report(
                    title = title,
                    content = content,
                    date = date,
                    screenshotPath = path
                )
            )
        }
    }

    fun deleteReport(report: Report) {
        viewModelScope.launch {
            repository.deleteReport(report)
        }
    }

    // ------------------ Date formatting Helpers ------------------

    companion object {
        fun getArabicMonth(timestamp: Long): String {
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val arabicMonths = arrayOf(
                "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
                "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
            )
            return "${arabicMonths[month]} $year"
        }

        fun getWeekRangeString(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale("ar")).apply {
                timeInMillis = timestamp
                // Align with standard week start (Sunday)
                set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startStr = SimpleDateFormat("yyyy/MM/dd", Locale("ar")).format(cal.time)
            cal.add(Calendar.DATE, 6)
            val endStr = SimpleDateFormat("yyyy/MM/dd", Locale("ar")).format(cal.time)
            return "الأسبوع من $startStr إلى $endStr"
        }

        fun formatSimpleDate(timestamp: Long): String {
            return SimpleDateFormat("yyyy/MM/dd", Locale("ar")).format(Date(timestamp))
        }
    }

    // --- Backup & Restore Methods ---
    fun exportBackup(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.exportBackup(getApplication())
            onResult(result)
        }
    }

    fun importBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.importBackup(getApplication(), jsonString)
            if (result) {
                loadFamilies()
                loadPaidFamilies()
                loadReportedWeeks()
                loadFamilySavedBalances()
            }
            onResult(result)
        }
    }

    fun uploadBackupToCloud(jsonString: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.uploadBackupToCloud(jsonString)
            onResult(result)
        }
    }

    fun downloadAndImportBackupFromCloud(url: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val jsonString = repository.downloadBackupFromCloud(url)
            if (jsonString != null) {
                val result = repository.importBackup(getApplication(), jsonString)
                if (result) {
                    loadFamilies()
                    loadPaidFamilies()
                    loadReportedWeeks()
                    loadFamilySavedBalances()
                }
                onResult(result)
            } else {
                onResult(false)
            }
        }
    }

    // --- Telegram Integration Methods ---
    fun getTelegramToken(): String {
        return prefs.getString("telegram_token", "") ?: ""
    }

    fun getTelegramChatId(): String {
        return prefs.getString("telegram_chat_id", "") ?: ""
    }

    fun getTelegramAutoSend(): Boolean {
        return prefs.getBoolean("telegram_auto_send", false)
    }

    fun saveTelegramConfig(token: String, chatId: String, autoSend: Boolean) {
        prefs.edit()
            .putString("telegram_token", token)
            .putString("telegram_chat_id", chatId)
            .putBoolean("telegram_auto_send", autoSend)
            .apply()
    }

    fun sendReportToTelegram(title: String, content: String, weekName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = getTelegramToken()
            val chatId = getTelegramChatId()
            if (token.isEmpty() || chatId.isEmpty()) {
                onResult(false)
                return@launch
            }
            val context = getApplication<Application>()
            val pdfBytes = PdfGenerator.generatePdfBytes(context, title, content)
            val cleanWeekName = weekName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val fileName = "تقرير_$cleanWeekName.pdf"
            val caption = "تقرير مالي لـ: $weekName"
            val success = repository.sendPdfToTelegram(token, chatId, pdfBytes, fileName, caption)
            onResult(success)
        }
    }
}

class AppViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
