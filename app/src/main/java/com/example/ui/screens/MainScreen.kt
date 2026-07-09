package com.example.ui.screens

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.PdfGenerator
import com.example.ui.theme.RedDeduction
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Observers
    val rickshaws by viewModel.allRickshaws.collectAsState()
    val rooms by viewModel.allRooms.collectAsState()
    val weeklySummaries by viewModel.weeklyRickshawSummaries.collectAsState()
    val monthlySummaries by viewModel.monthlyShopSummaries.collectAsState()
    val monthlyRoomSummaries by viewModel.monthlyRoomSummaries.collectAsState()
    val deductions by viewModel.allDeductions.collectAsState()
    val reports by viewModel.allReports.collectAsState()
    val families by viewModel.familiesState.collectAsState()
    val paidFamiliesMap by viewModel.paidFamiliesMapState.collectAsState()
    val reportedWeeks by viewModel.reportedWeeksState.collectAsState()

    // Screen Viewer States
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
    var showBackupRestoreScreen by remember { mutableStateOf(false) }

    if (showBackupRestoreScreen) {
        BackupRestoreScreen(
            viewModel = viewModel,
            onBack = { showBackupRestoreScreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                val (title, subtitle) = when (selectedTab) {
                    0 -> "إيرادات المجموعة" to "تحديث: ${AppViewModel.formatSimpleDate(System.currentTimeMillis())}"
                    1 -> "إيرادات الركشات" to "تسجيل الإيراد الأسبوعي للركشات وإدارة الأسطول"
                    2 -> "إيرادات الدكان" to "تسجيل الإيراد الشهري والمبيعات"
                    3 -> "إيجار الغرف" to "متابعة وتحصيل إيجار الغرف شهرياً"
                    4 -> "كشف توزيعات الأسر" to "متابعة أنصبة الأسر والأرصدة"
                    5 -> "الخصومات والمصاريف" to "تسجيل فوري للمنصرفات والخصومات"
                    6 -> "التقارير والمستندات" to "توثيق الأعمال وتصدير كشوفات الحساب"
                    else -> "إيرادات المجموعة" to ""
                }
                HighDensityHeader(
                    title = title,
                    subtitle = subtitle,
                    onNotificationClick = {
                        Toast.makeText(context, "لا توجد إشعارات جديدة حالياً", Toast.LENGTH_SHORT).show()
                    },
                    onBackupClick = {
                        showBackupRestoreScreen = true
                    }
                )
            },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3F4F9),
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 1.dp, color = Color(0xFFC4C6CF).copy(alpha = 0.5f), shape = RoundedCornerShape(0.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.TwoWheeler, contentDescription = "الركشات") },
                    label = { Text("الركشات", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_rickshaws")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "الدكان") },
                    label = { Text("الدكان", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_shop")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الغرف") },
                    label = { Text("الغرف", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_rooms")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.People, contentDescription = "التوزيعات") },
                    label = { Text("التوزيعات", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_distributions")
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.TrendingDown, contentDescription = "الخصومات") },
                    label = { Text("الخصومات", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_deductions")
                )
                NavigationBarItem(
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 },
                    icon = { Icon(Icons.Default.Description, contentDescription = "التقارير") },
                    label = { Text("التقارير", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_reports")
                )
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Animated transitions between tabs
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        weeklySummaries = weeklySummaries,
                        monthlySummaries = monthlySummaries,
                        monthlyRoomSummaries = monthlyRoomSummaries,
                        families = families,
                        onSaveFamilies = { list -> viewModel.saveFamilies(list) },
                        onViewImage = { fullScreenImagePath = it },
                        onNavigateToTab = { selectedTab = it },
                        onGenerateMockData = {
                            if (rickshaws.isEmpty()) {
                                viewModel.addRickshaw("ركشة رقم ١", "أحمد محمد")
                            }
                            val rid = if (rickshaws.isNotEmpty()) rickshaws.first().id else 1
                            viewModel.addRickshawRevenue(
                                rickshawId = rid,
                                amount = 18500.0,
                                date = System.currentTimeMillis(),
                                notes = "إيراد أسبوعي - الركشة الأولى",
                                screenshotUri = null
                            )
                            viewModel.addDeduction(
                                amount = 2500.0,
                                description = "صيانة دورية وتغيير زيت",
                                category = "RICKSHAW",
                                targetId = if (rickshaws.isNotEmpty()) rid else null,
                                date = System.currentTimeMillis(),
                                screenshotUri = null
                            )
                            Toast.makeText(context, "تم توليد بيانات إيرادات وخصومات تجريبية لتبسيط العرض!", Toast.LENGTH_LONG).show()
                        }
                    )
                    1 -> RickshawRevenuesScreen(
                        viewModel = viewModel,
                        rickshaws = rickshaws,
                        weeklySummaries = weeklySummaries,
                        onViewImage = { fullScreenImagePath = it }
                    )
                    2 -> ShopRevenuesScreen(
                        viewModel = viewModel,
                        monthlySummaries = monthlySummaries,
                        onViewImage = { fullScreenImagePath = it }
                    )
                    3 -> RoomRevenuesScreen(
                        viewModel = viewModel,
                        rooms = rooms,
                        monthlyRoomSummaries = monthlyRoomSummaries,
                        onViewImage = { fullScreenImagePath = it }
                    )
                    4 -> FamilyDistributionsScreen(viewModel = viewModel)
                    5 -> DeductionsScreen(
                        viewModel = viewModel,
                        rickshaws = rickshaws,
                        deductions = deductions,
                        onViewImage = { fullScreenImagePath = it }
                    )
                    6 -> ReportsScreen(
                        viewModel = viewModel,
                        reports = reports,
                        onViewImage = { fullScreenImagePath = it }
                    )
                }
            }

            // Full screen Image Viewer Dialog
            fullScreenImagePath?.let { path ->
                ImageViewerDialog(
                    imagePath = path,
                    onDismiss = { fullScreenImagePath = null }
                )
            }
        }
    }
}
}

data class LocalFamilyItem(
    val id: Int,
    val name: String,
    val portion: String
)

// ==========================================
// 1. DASHBOARD SCREEN (الرئيسية)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyEditDialog(
    families: List<AppViewModel.FamilyConfig>,
    onDismiss: () -> Unit,
    onSaveFamilies: (List<AppViewModel.FamilyConfig>) -> Unit
) {
    var localFamilies by remember {
        mutableStateOf(
            families.map { 
                val portionText = if (it.portion <= 0.0) "" else String.format(Locale.US, "%.0f", it.portion)
                LocalFamilyItem(it.id, it.name, portionText) 
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "إدارة أسماء وأنصبة الأسر",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0061A4),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "عدّل اسم كل أسرة وقيمة نصيبها الأسبوعي المحدد بالمبلغ (وليس بالنسبة). الأسرة ذات النصيب 0 تُعتبر غير نشطة. يمكنك إضافة وحذف الأسر ديناميكياً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    localFamilies.forEachIndexed { i, family ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F8)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFFD1E4FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${i + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF001D36)
                                    )
                                }

                                OutlinedTextField(
                                    value = family.name,
                                    onValueChange = { newValue ->
                                        localFamilies = localFamilies.toMutableList().apply {
                                            this[i] = this[i].copy(name = newValue)
                                        }
                                    },
                                    label = { Text("الاسم", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.8f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF1B1B1F),
                                        unfocusedTextColor = Color(0xFF1B1B1F),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedLabelColor = Color(0xFF0061A4),
                                        unfocusedLabelColor = Color(0xFF44474E)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = family.portion,
                                    onValueChange = { newValue ->
                                        localFamilies = localFamilies.toMutableList().apply {
                                            this[i] = this[i].copy(portion = newValue)
                                        }
                                    },
                                    label = { Text("المبلغ المحدد (SDG)", fontSize = 10.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.2f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF1B1B1F),
                                        unfocusedTextColor = Color(0xFF1B1B1F),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedLabelColor = Color(0xFF0061A4),
                                        unfocusedLabelColor = Color(0xFF44474E)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                IconButton(
                                    onClick = {
                                        localFamilies = localFamilies.toMutableList().apply {
                                            removeAt(i)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف الأسرة",
                                        tint = Color(0xFFBA1A1A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Button to add a new family with portion field right there
                    Button(
                        onClick = {
                            val nextId = if (localFamilies.isEmpty()) 1 else (localFamilies.maxOf { it.id }) + 1
                            localFamilies = localFamilies + LocalFamilyItem(nextId, "أسرة $nextId", "")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD1E4FF),
                            contentColor = Color(0xFF001D36)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp).testTag("add_family_option_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة أسرة جديدة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val updatedFamilies = localFamilies.map { family ->
                                val name = family.name.takeIf { it.isNotBlank() } ?: "أسرة ${family.id}"
                                val portion = family.portion.toDoubleOrNull() ?: 0.0
                                AppViewModel.FamilyConfig(id = family.id, name = name, portion = portion)
                            }
                            onSaveFamilies(updatedFamilies)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("حفظ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RickshawsEditDialog(
    rickshaws: List<Rickshaw>,
    onDismiss: () -> Unit,
    onAddRickshaw: (String, String) -> Unit,
    onUpdateRickshaw: (Rickshaw) -> Unit,
    onDeleteRickshaw: (Rickshaw) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var newDriver by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "إدارة ركشات الأسطول",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0061A4),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "يمكنك تعديل أسماء الركشات وسائقيها، أو حذف ركشة من الأسطول، أو إضافة ركشة جديدة بالأسفل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // List of existing Rickshaws
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rickshaws.isEmpty()) {
                        Text(
                            text = "لا توجد ركشات مسجلة حالياً.",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    } else {
                        rickshaws.forEach { r ->
                            // Local temporary state for editing each row
                            var editName by remember(r.id) { mutableStateOf(r.name) }
                            var editDriver by remember(r.id) { mutableStateOf(r.driverName) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F8)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("الاسم/الرقم", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = Color(0xFF1B1B1F),
                                            unfocusedTextColor = Color(0xFF1B1B1F)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = editDriver,
                                        onValueChange = { editDriver = it },
                                        label = { Text("السائق", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = Color(0xFF1B1B1F),
                                            unfocusedTextColor = Color(0xFF1B1B1F)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (editName.isNotBlank() && editDriver.isNotBlank()) {
                                                onUpdateRickshaw(r.copy(name = editName, driverName = editDriver))
                                                Toast.makeText(context, "تم حفظ التعديل بنجاح", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "الرجاء عدم ترك الحقول فارغة", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "حفظ التعديل",
                                            tint = Color(0xFF386A20),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteRickshaw(r) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف الركشة",
                                            tint = Color(0xFFBA1A1A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section to add a new Rickshaw
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "إضافة ركشة جديدة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061A4)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("اسم/رقم الركشة", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF1B1B1F),
                                    unfocusedTextColor = Color(0xFF1B1B1F)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newDriver,
                                onValueChange = { newDriver = it },
                                label = { Text("اسم السائق", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF1B1B1F),
                                    unfocusedTextColor = Color(0xFF1B1B1F)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    if (newName.isNotBlank() && newDriver.isNotBlank()) {
                                        onAddRickshaw(newName, newDriver)
                                        newName = ""
                                        newDriver = ""
                                        Toast.makeText(context, "تمت إضافة الركشة بنجاح", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "الرجاء تعبئة حقول الركشة الجديدة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44474E)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("إغلاق", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsEditDialog(
    rooms: List<com.example.data.Room>,
    onDismiss: () -> Unit,
    onAddRoom: (String, String) -> Unit,
    onUpdateRoom: (com.example.data.Room) -> Unit,
    onDeleteRoom: (com.example.data.Room) -> Unit
) {
    var newRoomName by remember { mutableStateOf("") }
    var newTenant by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "إدارة غرف الإيجار",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006874),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "يمكنك تعديل أسماء الغرف ومستأجريها، أو حذف غرفة، أو إضافة غرفة جديدة بالأسفل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // List of existing Rooms
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rooms.isEmpty()) {
                        Text(
                            text = "لا توجد غرف مسجلة حالياً.",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    } else {
                        rooms.forEach { r ->
                            // Local temporary state for editing each row
                            var editName by remember(r.id) { mutableStateOf(r.name) }
                            var editTenant by remember(r.id) { mutableStateOf(r.tenantName) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F8)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("رقم/اسم الغرفة", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = Color(0xFF1B1B1F),
                                            unfocusedTextColor = Color(0xFF1B1B1F)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = editTenant,
                                        onValueChange = { editTenant = it },
                                        label = { Text("المستأجر", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = Color(0xFF1B1B1F),
                                            unfocusedTextColor = Color(0xFF1B1B1F)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (editName.isNotBlank() && editTenant.isNotBlank()) {
                                                onUpdateRoom(r.copy(name = editName, tenantName = editTenant))
                                                Toast.makeText(context, "تم حفظ التعديل بنجاح", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "الرجاء عدم ترك الحقول فارغة", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "حفظ التعديل",
                                            tint = Color(0xFF386A20),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteRoom(r) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف الغرفة",
                                            tint = Color(0xFFBA1A1A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section to add a new Room
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "إضافة غرفة جديدة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006874)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newRoomName,
                                onValueChange = { newRoomName = it },
                                label = { Text("اسم/رقم الغرفة", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF1B1B1F),
                                    unfocusedTextColor = Color(0xFF1B1B1F)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newTenant,
                                onValueChange = { newTenant = it },
                                label = { Text("اسم المستأجر", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF1B1B1F),
                                    unfocusedTextColor = Color(0xFF1B1B1F)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    if (newRoomName.isNotBlank() && newTenant.isNotBlank()) {
                                        onAddRoom(newRoomName, newTenant)
                                        newRoomName = ""
                                        newTenant = ""
                                        Toast.makeText(context, "تمت إضافة الغرفة بنجاح", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "الرجاء تعبئة حقول الغرفة الجديدة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006874)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44474E)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("إغلاق", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DashboardOverviewCard(
    grandTotalRevenue: Double,
    totalRickshawRevenue: Double,
    totalShopRevenue: Double,
    totalRoomRevenue: Double,
    grandTotalDeductions: Double,
    grandTotalPaidToFamilies: Double
) {
    val netTreasuryBalance = grandTotalRevenue - (grandTotalDeductions + grandTotalPaidToFamilies)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_overview_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F4F8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFF0061A4),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "كشف الحساب المجمع للمجموعة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0061A4)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0061A4), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "تحديث فوري",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grand total collected amount
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "إجمالي المبالغ الكلية المقبوضة (المجموع الكلي)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF44474E),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%,.2f", grandTotalRevenue),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B1B1F)
                    )
                    Text(
                        text = "SDG",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF44474E),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Breakdown Grid (3 rows)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Rickshaws item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "إيرادات الركشات الكلية",
                        amount = totalRickshawRevenue,
                        icon = Icons.Default.TwoWheeler,
                        iconColor = Color(0xFF0061A4),
                        bgColor = Color(0xFFE0EAFC)
                    )
                    // Shop item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "إيرادات الدكان الكلية",
                        amount = totalShopRevenue,
                        icon = Icons.Default.Storefront,
                        iconColor = Color(0xFF386A20),
                        bgColor = Color(0xFFE8F5E9)
                    )
                }
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Room item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "إيرادات الغرف الكلية",
                        amount = totalRoomRevenue,
                        icon = Icons.Default.ReceiptLong,
                        iconColor = Color(0xFF006874),
                        bgColor = Color(0xFFE0F7FA)
                    )
                    // Deductions item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "إجمالي المصاريف والخصومات",
                        amount = grandTotalDeductions,
                        icon = Icons.Default.TrendingDown,
                        iconColor = Color(0xFFBA1A1A),
                        bgColor = Color(0xFFFFE9E9)
                    )
                }
                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Distributed item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "المسلم والموزع للأسر",
                        amount = grandTotalPaidToFamilies,
                        icon = Icons.Default.People,
                        iconColor = Color(0xFF555F71),
                        bgColor = Color(0xFFF3F4F9)
                    )
                    // Net balance item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "رصيد الخزنة المتبقي",
                        amount = netTreasuryBalance,
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = if (netTreasuryBalance >= 0) Color(0xFF386A20) else Color(0xFFBA1A1A),
                        bgColor = if (netTreasuryBalance >= 0) Color(0xFFE8F5E9) else Color(0xFFFFE9E9)
                    )
                }
            }
        }
    }
}

@Composable
fun BreakdownItem(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF44474E),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(bgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = String.format(Locale.US, "%,.0f", amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B1B1F)
            )
            Text(
                text = "جنيه سوداني",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF44474E).copy(alpha = 0.6f),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    weeklySummaries: List<AppViewModel.WeeklySummary>,
    monthlySummaries: List<AppViewModel.MonthlySummary>,
    monthlyRoomSummaries: List<AppViewModel.RoomMonthlySummary>,
    families: List<AppViewModel.FamilyConfig>,
    onSaveFamilies: (List<AppViewModel.FamilyConfig>) -> Unit,
    onViewImage: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onGenerateMockData: () -> Unit
) {
    var showFamilyEditDialog by remember { mutableStateOf(false) }
    val paidFamiliesMap by viewModel.paidFamiliesMapState.collectAsState()
    val reportedWeeks by viewModel.reportedWeeksState.collectAsState()

    val totalRickshawRevenue = weeklySummaries.sumOf { it.totalRevenue }
    val totalShopRevenue = monthlySummaries.sumOf { it.totalRevenue }
    val totalRoomRevenue = monthlyRoomSummaries.sumOf { it.totalRevenue }
    val grandTotalRevenue = totalRickshawRevenue + totalShopRevenue + totalRoomRevenue

    val grandTotalDeductions = weeklySummaries.sumOf { it.totalDeductions } +
            monthlySummaries.sumOf { it.totalDeductions } +
            monthlyRoomSummaries.sumOf { it.totalDeductions }

    // Calculate total paid/distributed to families
    var grandTotalPaidToFamilies = 0.0
    for (summary in weeklySummaries) {
        val paidIds = paidFamiliesMap[summary.weekKey] ?: emptySet()
        for (family in families) {
            if (family.portion > 0 && paidIds.contains(family.id)) {
                grandTotalPaidToFamilies += family.portion
            }
        }
    }

    if (showFamilyEditDialog) {
        FamilyEditDialog(
            families = families,
            onDismiss = { showFamilyEditDialog = false },
            onSaveFamilies = onSaveFamilies
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Grand Overview Card
        item {
            DashboardOverviewCard(
                grandTotalRevenue = grandTotalRevenue,
                totalRickshawRevenue = totalRickshawRevenue,
                totalShopRevenue = totalShopRevenue,
                totalRoomRevenue = totalRoomRevenue,
                grandTotalDeductions = grandTotalDeductions,
                grandTotalPaidToFamilies = grandTotalPaidToFamilies
            )
        }

        // Section 1: Rickshaw Weekly Revenues & Division
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تقسيم إيرادات الركشات أسبوعياً",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0061A4)
                )
                Button(
                    onClick = { showFamilyEditDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E2EC),
                        contentColor = Color(0xFF1B1B1F)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("edit_families_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إدارة الأسر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (weeklySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لم يتم تسجيل أي إيراد ركشات حتى الآن. توجه لتبويب الإيرادات لإضافة أول إدخال أسبوعي، أو استخدم زر سكرين شوت أدناه للتوليد السريع.",
                    icon = Icons.Default.TwoWheeler
                )
            }
        } else {
            items(weeklySummaries.take(3)) { summary ->
                val currentPaid = paidFamiliesMap[summary.weekKey] ?: emptySet()
                val isReported = reportedWeeks.contains(summary.weekKey)
                WeeklySummaryCard(
                    summary = summary,
                    families = families,
                    currentPaid = currentPaid,
                    isReported = isReported,
                    onTogglePaid = { familyId, isPaid ->
                        viewModel.toggleFamilyPayment(summary.weekKey, familyId, isPaid)
                    },
                    onGenerateReport = {
                        viewModel.generateWeeklyDistributionReport(summary)
                    },
                    onViewImage = onViewImage
                )
            }
        }

        // Section 2: Shop Monthly Revenues
        item {
            Text(
                text = "متابعة إيرادات الدكان شهرياً",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1F),
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
        }

        if (monthlySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لم يتم تسجيل أي إيراد للدكان حتى الآن. توجه لتبويب الإيرادات لإضافة إدخال الدكان الشهري.",
                    icon = Icons.Default.Storefront
                )
            }
        } else {
            items(monthlySummaries.take(3)) { summary ->
                MonthlySummaryCard(summary = summary, onViewImage = onViewImage)
            }
        }

        // Quick action buttons at the bottom of the scrollable column to match the HTML design
        item {
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionsRow(
                onScreenshotClick = onGenerateMockData,
                onNewReportClick = { onNavigateToTab(6) },
                onDeductionClick = { onNavigateToTab(5) }
            )
        }
    }
}

@Composable
fun HighDensityHeader(
    title: String,
    subtitle: String,
    onNotificationClick: () -> Unit = {},
    onBackupClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFFF6F8FC))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFD1E4FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0xFF001D36),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1F),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Backup/Cloud configuration button
            IconButton(
                onClick = onBackupClick,
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.5f), CircleShape)
                    .size(40.dp)
                    .testTag("header_backup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "النسخ الاحتياطي والربط",
                    tint = Color(0xFF0061A4),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.5f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF1B1B1F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onScreenshotClick: () -> Unit,
    onNewReportClick: () -> Unit,
    onDeductionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 📷 Screenshot button
        Button(
            onClick = onScreenshotClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD1E4FF),
                contentColor = Color(0xFF001D36)
            ),
            contentPadding = PaddingValues(4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📷", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("سكرين شوت", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 📝 New Report button
        Button(
            onClick = onNewReportClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD1E4FF),
                contentColor = Color(0xFF001D36)
            ),
            contentPadding = PaddingValues(4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📝", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("تقرير جديد", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 💸 Deduct button
        Button(
            onClick = onDeductionClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFBA1A1A),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("💸", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("خصم مبلغ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FamilyDistributionGrid(
    weekKey: String,
    sharePerFamily: Double,
    families: List<AppViewModel.FamilyConfig>,
    currentPaid: Set<Int>,
    onTogglePaid: (Int, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF0061A4), CircleShape)
                    )
                    Text(
                        text = "حالة توزيع الأسر للأسبوع الحالي",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1F)
                    )
                }
                Text(
                    text = "اضغط للتغيير",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF44474E),
                    fontSize = 10.sp
                )
            }

            // 3-column chunked grid
            val chunkedFamilies = families.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunkedFamilies.forEach { rowFamilies ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowFamilies.forEach { family ->
                            val index = family.id
                            val isPaid = currentPaid.contains(index)
                            val bg = if (isPaid) Color(0xFFF1F3F8) else Color(0xFFFFF4E5)
                            val textColor = if (isPaid) Color(0xFF1B1B1F) else Color(0xFF8B5000)
                            val borderMod = if (isPaid) {
                                Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(12.dp))
                            } else {
                                Modifier.border(1.dp, Color(0xFFE9C3C3), RoundedCornerShape(12.dp))
                            }

                            val familyName = family.name
                            val portion = family.portion
                            val familyShare = portion

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .then(borderMod)
                                    .clickable {
                                        onTogglePaid(index, !isPaid)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = familyName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF1B1B1F),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 10.sp
                                    )
                                    if (portion > 0) {
                                        Text(
                                            text = String.format(Locale.US, "%,.0f SDG", familyShare),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF0061A4),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        Text(
                                            text = "غير نشط",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFBA1A1A),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isPaid) "تم الاستلام" else "لم يستلم",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        if (rowFamilies.size < 3) {
                            for (i in 0 until (3 - rowFamilies.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklySummaryCard(
    summary: AppViewModel.WeeklySummary,
    families: List<AppViewModel.FamilyConfig>,
    currentPaid: Set<Int>,
    isReported: Boolean,
    onTogglePaid: (Int, Boolean) -> Unit,
    onGenerateReport: () -> Unit,
    onViewImage: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Week Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF0061A4), CircleShape)
                )
                Text(
                    text = summary.weekKey,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1F)
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFFD1E4FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ركشات - أسبوعي",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF001D36),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Grid of values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Column 1: Total Weekly Revenue Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإجمالي الأسبوعي",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44474E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "+",
                            color = Color(0xFF0061A4),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.totalRevenue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B1B1F)
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF44474E).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }

            // Column 2: Deductions Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFE9E9))
                    .border(1.dp, Color(0xFFE9C3C3), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الخصومات (صيانة)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44474E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "-",
                            color = Color(0xFFBA1A1A),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.totalDeductions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFBA1A1A)
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFBA1A1A).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Main Net Profit Distribution Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0061A4))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeCount = families.count { it.portion > 0 }
                    Text(
                        text = "صافي الربح للتوزيع ($activeCount أسر نشطة)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "رقمي",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.netRevenue),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalActivePayout = families.filter { it.portion > 0 }.sumOf { it.portion }
                    Text(
                        text = "إجمالي أنصبة الأسر المحددة للأسبوع:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%,.0f SDG", totalActivePayout),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        // Interactive Families Status Grid
        FamilyDistributionGrid(
            weekKey = summary.weekKey,
            sharePerFamily = summary.sharePerFamily,
            families = families,
            currentPaid = currentPaid,
            onTogglePaid = onTogglePaid
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (!isReported) {
            Button(
                onClick = onGenerateReport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0061A4),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("settle_week_button_${summary.weekKey}")
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ التوزيع وعمل التقرير النهائي للأسبوع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0061A4), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تم حفظ التوزيع وعمل التقرير النهائي للأسبوع بنجاح", color = Color(0xFF0061A4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Attached screenshots list if any
        val pathWithScreenshots = (summary.revenues.mapNotNull { it.screenshotPath } +
                summary.deductions.mapNotNull { it.screenshotPath }).distinct()

        if (pathWithScreenshots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "لقطات الشاشة المرفقة بهذا الأسبوع:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF44474E),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                pathWithScreenshots.forEach { path ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(8.dp))
                            .clickable { onViewImage(path) }
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Screenshot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(
    summary: AppViewModel.MonthlySummary,
    onViewImage: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Month Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF0061A4), CircleShape)
                )
                Text(
                    text = summary.monthKey,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1F)
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFFE0E2EC), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "الدكان - شهري",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1B1B1F),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Grid of values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Column 1: Total Revenue Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إيراد الدكان",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44474E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "+",
                            color = Color(0xFF0061A4),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.totalRevenue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B1B1F)
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF44474E).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }

            // Column 2: Deductions Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFE9E9))
                    .border(1.dp, Color(0xFFE9C3C3), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "خصومات ومصاريف",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44474E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "-",
                            color = Color(0xFFBA1A1A),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.totalDeductions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFBA1A1A)
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFBA1A1A).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Main Net Profit Distribution Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0061A4))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "صافي ربح الدكان للتوزيع",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "شهري",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.netRevenue),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        // Attached screenshots list if any
        val pathWithScreenshots = (summary.revenues.mapNotNull { it.screenshotPath } +
                summary.deductions.mapNotNull { it.screenshotPath }).distinct()

        if (pathWithScreenshots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "لقطات الشاشة المرفقة بهذا الشهر:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF44474E),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                pathWithScreenshots.forEach { path ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(8.dp))
                            .clickable { onViewImage(path) }
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Screenshot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialMiniCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.US, "%,.0f", amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "SDG",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}


// ==========================================
// 2. RICKSHAW REVENUES SCREEN (إيرادات الركشات)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RickshawRevenuesScreen(
    viewModel: AppViewModel,
    rickshaws: List<Rickshaw>,
    weeklySummaries: List<AppViewModel.WeeklySummary>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current

    // Forms fields
    var selectedRickshaw by remember { mutableStateOf<Rickshaw?>(null) }
    var rickshawAmount by remember { mutableStateOf("") }
    var rickshawNotes by remember { mutableStateOf("") }
    var rickshawDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var rickshawScreenshotUri by remember { mutableStateOf<Uri?>(null) }

    // Dialog state
    var showRickshawsEditDialog by remember { mutableStateOf(false) }

    // Setup photo picker
    val rickshawPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) rickshawScreenshotUri = uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تسجيل إيراد ركشة أسبوعي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Fleet management settings button (just like family settings!)
                        Button(
                            onClick = { showRickshawsEditDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0E2EC),
                                contentColor = Color(0xFF1B1B1F)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("manage_rickshaws_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إدارة الأسطول", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Fleet Quick Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedRickshaw?.name ?: "اختر الركشة...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الركشة") },
                                trailingIcon = {
                                    IconButton(onClick = { dropdownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("rickshaw_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                if (rickshaws.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("لا يوجد ركشات، أضف ركشة أولاً") },
                                        onClick = { dropdownExpanded = false }
                                    )
                                } else {
                                    rickshaws.forEach { r ->
                                        DropdownMenuItem(
                                            text = { Text("${r.name} - ${r.driverName}") },
                                            onClick = {
                                                selectedRickshaw = r
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Amount
                    OutlinedTextField(
                        value = rickshawAmount,
                        onValueChange = { rickshawAmount = it },
                        label = { Text("المبلغ بالإيراد (SDG)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("rickshaw_amount"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Notes
                    OutlinedTextField(
                        value = rickshawNotes,
                        onValueChange = { rickshawNotes = it },
                        label = { Text("ملاحظات (الأسبوع، تفاصيل...)") },
                        modifier = Modifier.fillMaxWidth().testTag("rickshaw_notes"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Screenshot Selection & Simulated Preview
                    ScreenshotPickerSection(
                        selectedUri = rickshawScreenshotUri,
                        onSelectRealPhoto = {
                            rickshawPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            rickshawScreenshotUri = MockImageGenerator.generate(context)
                        },
                        onClearPhoto = { rickshawScreenshotUri = null }
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            val amt = rickshawAmount.toDoubleOrNull()
                            if (selectedRickshaw == null) {
                                Toast.makeText(context, "الرجاء اختيار ركشة", Toast.LENGTH_SHORT).show()
                            } else if (amt == null || amt <= 0) {
                                Toast.makeText(context, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addRickshawRevenue(
                                    rickshawId = selectedRickshaw!!.id,
                                    amount = amt,
                                    date = rickshawDate,
                                    notes = rickshawNotes,
                                    screenshotUri = rickshawScreenshotUri
                                )
                                Toast.makeText(context, "تم حفظ الإيراد أسبوعياً بنجاح", Toast.LENGTH_SHORT).show()
                                // Reset fields
                                rickshawAmount = ""
                                rickshawNotes = ""
                                rickshawScreenshotUri = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_rickshaw_revenue"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ الإيراد وتوزيعه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Section: Registered Fleet or Fleet Summary
        item {
            Text(
                text = "سجل إيرادات الركشات الأسبوعية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of all entries
        if (weeklySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد إيرادات ركشات مسجلة حتى الآن.",
                    icon = Icons.Default.ReceiptLong
                )
            }
        } else {
            items(weeklySummaries) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(summary.weekKey, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        summary.revenues.forEach { rev ->
                            val rName = rickshaws.find { it.id == rev.rickshawId }?.name ?: "ركشة غير معروفة"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "$rName • ${AppViewModel.formatSimpleDate(rev.date)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    if (rev.notes.isNotEmpty()) {
                                        Text(text = rev.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = String.format(Locale.US, "%,.0f SDG", rev.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    
                                    rev.screenshotPath?.let { path ->
                                        IconButton(onClick = { onViewImage(path) }) {
                                            Icon(Icons.Default.Image, contentDescription = "View", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteRickshawRevenue(rev) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedDeduction, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRickshawsEditDialog) {
        RickshawsEditDialog(
            rickshaws = rickshaws,
            onDismiss = { showRickshawsEditDialog = false },
            onAddRickshaw = { name, driver -> viewModel.addRickshaw(name, driver) },
            onUpdateRickshaw = { r -> viewModel.updateRickshaw(r) },
            onDeleteRickshaw = { r -> viewModel.deleteRickshaw(r) }
        )
    }
}


// ==========================================
// 2B. SHOP REVENUES SCREEN (إيرادات الدكان)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopRevenuesScreen(
    viewModel: AppViewModel,
    monthlySummaries: List<AppViewModel.MonthlySummary>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current

    // Forms fields
    var shopAmount by remember { mutableStateOf("") }
    var shopNotes by remember { mutableStateOf("") }
    var shopDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var shopScreenshotUri by remember { mutableStateOf<Uri?>(null) }

    // Setup photo picker
    val shopPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) shopScreenshotUri = uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "تسجيل إيراد الدكان الشهري",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // Amount
                    OutlinedTextField(
                        value = shopAmount,
                        onValueChange = { shopAmount = it },
                        label = { Text("المبلغ الشهري الإجمالي (SDG)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("shop_amount"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Notes
                    OutlinedTextField(
                        value = shopNotes,
                        onValueChange = { shopNotes = it },
                        label = { Text("ملاحظات (الشهر، المبيعات...)") },
                        modifier = Modifier.fillMaxWidth().testTag("shop_notes"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Screenshot Selection
                    ScreenshotPickerSection(
                        selectedUri = shopScreenshotUri,
                        onSelectRealPhoto = {
                            shopPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            shopScreenshotUri = MockImageGenerator.generate(context)
                        },
                        onClearPhoto = { shopScreenshotUri = null }
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            val amt = shopAmount.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                Toast.makeText(context, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addShopRevenue(
                                    amount = amt,
                                    date = shopDate,
                                    notes = shopNotes,
                                    screenshotUri = shopScreenshotUri
                                )
                                Toast.makeText(context, "تم حفظ إيراد الدكان شهرياً بنجاح", Toast.LENGTH_SHORT).show()
                                // Reset fields
                                shopAmount = ""
                                shopNotes = ""
                                shopScreenshotUri = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_shop_revenue"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ إيراد الدكان", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "سجل إيرادات الدكان الشهرية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of all entries
        if (monthlySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد إيرادات دكان مسجلة حتى الآن.",
                    icon = Icons.Default.ReceiptLong
                )
            }
        } else {
            items(monthlySummaries) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(summary.monthKey, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        summary.revenues.forEach { rev ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "إيراد دكان • ${AppViewModel.formatSimpleDate(rev.date)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    if (rev.notes.isNotEmpty()) {
                                        Text(text = rev.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = String.format(Locale.US, "%,.0f SDG", rev.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                                    
                                    rev.screenshotPath?.let { path ->
                                        IconButton(onClick = { onViewImage(path) }) {
                                            Icon(Icons.Default.Image, contentDescription = "View", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteShopRevenue(rev) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedDeduction, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. DEDUCTIONS SCREEN (الخصومات)
// ==========================================
@Composable
fun DeductionsScreen(
    viewModel: AppViewModel,
    rickshaws: List<Rickshaw>,
    deductions: List<Deduction>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GENERAL") } // "RICKSHAW", "SHOP", "GENERAL"
    var selectedRickshaw by remember { mutableStateOf<Rickshaw?>(null) }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) screenshotUri = uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Deduction addition Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "خصم مبالغ لتنفيذ عمل معين (مصاريف)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RedDeduction
                    )

                    // Description / Specific Work description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("نوع العمل المنجز / سبب الخصم") },
                        placeholder = { Text("مثال: صيانة محرك، تغيير زيت، فواتير كهرباء الدكان...") },
                        modifier = Modifier.fillMaxWidth().testTag("deduction_desc"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Category selection (Chip row)
                    Text("الجهة المستهدفة بالخصم:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    listOf(
                            "RICKSHAW" to "ركشة",
                            "SHOP" to "الدكان",
                            "ROOM" to "الغرف",
                            "GENERAL" to "عام / آخر"
                        ).forEach { (cat, label) ->
                            val isSelected = category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(label, fontWeight = FontWeight.Bold) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f).testTag("chip_$cat")
                            )
                        }
                    }

                    // If RICKSHAW selected, choose which rickshaw
                    if (category == "RICKSHAW") {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedRickshaw?.name ?: "اختر ركشة للتخصيص (اختياري)...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الركشة المستهدفة") },
                                trailingIcon = {
                                    IconButton(onClick = { dropdownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("deduction_rickshaw_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("عام لجميع الركشات") },
                                    onClick = {
                                        selectedRickshaw = null
                                        dropdownExpanded = false
                                    }
                                )
                                rickshaws.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r.name) },
                                        onClick = {
                                            selectedRickshaw = r
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("المبلغ المخصوم (SDG)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("deduction_amount"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Photo selector
                    ScreenshotPickerSection(
                        selectedUri = screenshotUri,
                        onSelectRealPhoto = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            screenshotUri = MockImageGenerator.generate(context)
                        },
                        onClearPhoto = { screenshotUri = null }
                    )

                    // Submit button
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (description.isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة تفاصيل العمل/الخصم", Toast.LENGTH_SHORT).show()
                            } else if (amt == null || amt <= 0) {
                                Toast.makeText(context, "الرجاء إدخال مبلغ صحيح للخصم", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addDeduction(
                                    amount = amt,
                                    description = description,
                                    category = category,
                                    targetId = if (category == "RICKSHAW") selectedRickshaw?.id else null,
                                    date = System.currentTimeMillis(),
                                    screenshotUri = screenshotUri
                                )
                                Toast.makeText(context, "تم تسجيل عملية الخصم بنجاح", Toast.LENGTH_SHORT).show()
                                amount = ""
                                description = ""
                                screenshotUri = null
                                selectedRickshaw = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedDeduction),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_deduction"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تسجيل وحفظ الخصم", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "جميع الخصومات والمصاريف المسجلة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of deductions
        if (deductions.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا يوجد أي خصومات مسجلة.",
                    icon = Icons.Default.TrendingDown
                )
            }
        } else {
            items(deductions) { ded ->
                val badgeText = when (ded.category) {
                    "RICKSHAW" -> "ركشات"
                    "SHOP" -> "الدكان"
                    "ROOM" -> "الغرف"
                    else -> "عام"
                }
                val badgeColor = when (ded.category) {
                    "RICKSHAW" -> MaterialTheme.colorScheme.primary
                    "SHOP" -> MaterialTheme.colorScheme.secondary
                    "ROOM" -> Color(0xFF006874)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = AppViewModel.formatSimpleDate(ded.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ded.description,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "-%,.0f SDG", ded.amount),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = RedDeduction
                            )

                            ded.screenshotPath?.let { path ->
                                IconButton(onClick = { onViewImage(path) }) {
                                    Icon(Icons.Default.Image, contentDescription = "View Screenshot", tint = RedDeduction, modifier = Modifier.size(20.dp))
                                }
                            }

                            IconButton(onClick = { viewModel.deleteDeduction(ded) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف الخصم", tint = RedDeduction.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. REPORTS SCREEN (التقارير)
// ==========================================
@Composable
fun ReportsScreen(
    viewModel: AppViewModel,
    reports: List<Report>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }

    // Dialog Viewer State for detailed report view
    var selectedReportForDetails by remember { mutableStateOf<Report?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) screenshotUri = uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "كتابة تقرير مالي / عام جديد",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان التقرير") },
                        placeholder = { Text("مثال: تقرير أعمال الصيانة الأسبوعية") },
                        modifier = Modifier.fillMaxWidth().testTag("report_title"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Content / Body of Report
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("محتوى التقرير والتفاصيل") },
                        placeholder = { Text("اكتب تفاصيل التقرير هنا بوضوح...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("report_content"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Screenshot attaching
                    ScreenshotPickerSection(
                        selectedUri = screenshotUri,
                        onSelectRealPhoto = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            screenshotUri = MockImageGenerator.generate(context)
                        },
                        onClearPhoto = { screenshotUri = null }
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            if (title.isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة عنوان للتقرير", Toast.LENGTH_SHORT).show()
                            } else if (content.isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة محتوى التقرير", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addReport(
                                    title = title,
                                    content = content,
                                    date = System.currentTimeMillis(),
                                    screenshotUri = screenshotUri
                                )
                                if (viewModel.getTelegramAutoSend()) {
                                    viewModel.sendReportToTelegram(title, content, title) { success ->
                                        if (success) {
                                            Toast.makeText(context, "تم إرسال التقرير تلقائياً إلى تليجرام ✈️", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "فشل الإرسال التلقائي إلى تليجرام. يرجى التحقق من الإعدادات ⚠️", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                Toast.makeText(context, "تم حفظ التقرير بنجاح", Toast.LENGTH_SHORT).show()
                                title = ""
                                content = ""
                                screenshotUri = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_report"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ التقرير ونشره", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Section Header
        item {
            Text(
                text = "جميع التقارير المسجلة والمحفوظة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of reports
        if (reports.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد أي تقارير مسجلة حتى الآن.",
                    icon = Icons.Default.Description
                )
            }
        } else {
            items(reports) { rep ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedReportForDetails = rep },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rep.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = AppViewModel.formatSimpleDate(rep.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(onClick = {
                                    val weekName = if (rep.title.contains("لـ ")) {
                                        rep.title.substringAfter("لـ ").trim()
                                    } else {
                                        rep.title.replace(":", "_").replace("/", "_").trim()
                                    }
                                    PdfGenerator.saveReportToPdf(context, rep.title, rep.content, weekName)
                                }) {
                                    Icon(Icons.Default.Save, contentDescription = "حفظ كـ PDF", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = {
                                    val weekName = if (rep.title.contains("لـ ")) {
                                        rep.title.substringAfter("لـ ").trim()
                                    } else {
                                        rep.title.replace(":", "_").replace("/", "_").trim()
                                    }
                                    if (viewModel.getTelegramToken().isEmpty() || viewModel.getTelegramChatId().isEmpty()) {
                                        Toast.makeText(context, "الرجاء ضبط إعدادات تليجرام في قسم النسخ الاحتياطي أولاً ⚠️", Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.sendReportToTelegram(rep.title, rep.content, weekName) { success ->
                                            if (success) {
                                                Toast.makeText(context, "تم إرسال التقرير بنجاح إلى تليجرام ✈️", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "فشل إرسال التقرير لتليجرام. تحقق من الإعدادات أو الاتصال ❌", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "إرسال إلى تليجرام", tint = Color(0xFF0088CC), modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = { viewModel.deleteReport(rep) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف التقرير", tint = RedDeduction.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = rep.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (rep.screenshotPath != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "يحتوي على لقطة شاشة مرفقة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog of selected report
    selectedReportForDetails?.let { rep ->
        AlertDialog(
            onDismissRequest = { selectedReportForDetails = null },
            title = {
                Column {
                    Text(rep.title, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "التاريخ: ${AppViewModel.formatSimpleDate(rep.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = rep.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    rep.screenshotPath?.let { path ->
                        Column {
                            Text("الصورة المرفقة بالتقرير:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .clickable { onViewImage(path) }
                            ) {
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = "Report Attachment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val weekName = if (rep.title.contains("لـ ")) {
                                rep.title.substringAfter("لـ ").trim()
                            } else {
                                rep.title.replace(":", "_").replace("/", "_").trim()
                            }
                            PdfGenerator.saveReportToPdf(context, rep.title, rep.content, weekName)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ كـ PDF")
                    }
                    Button(
                        onClick = {
                            val weekName = if (rep.title.contains("لـ ")) {
                                rep.title.substringAfter("لـ ").trim()
                            } else {
                                rep.title.replace(":", "_").replace("/", "_").trim()
                            }
                            if (viewModel.getTelegramToken().isEmpty() || viewModel.getTelegramChatId().isEmpty()) {
                                Toast.makeText(context, "الرجاء ضبط إعدادات تليجرام في قسم النسخ الاحتياطي أولاً ⚠️", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.sendReportToTelegram(rep.title, rep.content, weekName) { success ->
                                    if (success) {
                                        Toast.makeText(context, "تم إرسال التقرير بنجاح إلى تليجرام ✈️", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "فشل إرسال التقرير لتليجرام. تحقق من الإعدادات أو الاتصال ❌", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0088CC),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إرسال تليجرام ✈️")
                    }
                    Button(
                        onClick = { selectedReportForDetails = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("إغلاق التقرير")
                    }
                }
            }
        )
    }
}


// ==========================================
// COMMON UI COMPOSABLES & HELPERS
// ==========================================

@Composable
fun EmptyStateCard(
    message: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ScreenshotPickerSection(
    selectedUri: Uri?,
    onSelectRealPhoto: () -> Unit,
    onSelectMockPhoto: () -> Unit,
    onClearPhoto: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "إرفاق إيصال / لقطة شاشة (Screenshot):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (selectedUri != null) {
                // Image chosen state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Selected Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تم إرفاق لقطة شاشة بنجاح!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ستحفظ ضمن السجل المالي المحلي للعملية.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onClearPhoto,
                        modifier = Modifier
                            .background(RedDeduction.copy(alpha = 0.1f), CircleShape)
                            .testTag("clear_photo")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = RedDeduction)
                    }
                }
            } else {
                // Button Row for choosing or generating mock photo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelectRealPhoto,
                        modifier = Modifier.weight(1f).testTag("select_real_photo"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اختر صورة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSelectMockPhoto,
                        modifier = Modifier.weight(1f).testTag("select_mock_photo"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إيصال تجريبي مالي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ImageViewerDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "FullScreen Screenshot",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
            }
        }
    }
}


// ==========================================
// MOCK RECEIPT IMAGE GENERATION ENGINE
// ==========================================
object MockImageGenerator {
    fun generate(context: Context): Uri {
        val dir = File(context.filesDir, "screenshots")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "mock_receipt_${System.currentTimeMillis()}.png")

        val bitmap = android.graphics.Bitmap.createBitmap(400, 400, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint()

        // Draw background
        paint.color = android.graphics.Color.WHITE
        canvas.drawRect(0f, 0f, 400f, 400f, paint)

        // Draw Emerald header
        paint.color = android.graphics.Color.rgb(13, 148, 136)
        canvas.drawRect(10f, 10f, 390f, 60f, paint)

        // Title
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 20f
        paint.isAntiAlias = true
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("إيصال مالي معتمد", 200f, 42f, paint)

        // Reset Paint
        paint.color = android.graphics.Color.BLACK
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        paint.textSize = 15f

        val xRight = 350f
        canvas.drawText("متابع الركشات والدكان - نظام التقسيم", xRight, 100f, paint)
        
        paint.color = android.graphics.Color.rgb(100, 116, 139)
        canvas.drawText("التاريخ: ${SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())}", xRight, 140f, paint)
        canvas.drawText("نوع العملية: إثبات إيراد مالي أسبوعي", xRight, 180f, paint)
        canvas.drawText("المبلغ: تم الاستلام نقداً", xRight, 220f, paint)
        canvas.drawText("التقسيم: موزع بالتساوي على 9 أسر مستفيدة", xRight, 260f, paint)

        // Draw Gold Accent seal
        paint.color = android.graphics.Color.rgb(245, 158, 11)
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(80f, 320f, 45f, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.textSize = 14f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("مقبول", 80f, 315f, paint)
        canvas.drawText("تم التحقق", 80f, 335f, paint)

        try {
            val out = java.io.FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Uri.fromFile(file)
    }
}

@Composable
fun FamilyDistributionsScreen(viewModel: AppViewModel) {
    val families by viewModel.familiesState.collectAsState()
    val savedBalances by viewModel.familySavedBalancesState.collectAsState()
    val weeklySummaries by viewModel.weeklyRickshawSummaries.collectAsState()
    val reportedWeeks by viewModel.reportedWeeksState.collectAsState()
    val paidFamiliesMap by viewModel.paidFamiliesMapState.collectAsState()

    var selectedFamilyForPay by remember { mutableStateOf<AppViewModel.FamilyConfig?>(null) }
    var payAmount by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card showing total saved balances
        item {
            val totalSaved = savedBalances.values.sum()
            Card(
                modifier = Modifier.fillMaxWidth().testTag("distributions_hero_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF001D36))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "إجمالي الأرصدة والأنصبة المحفوظة للأسر",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.US, "%,.2f SDG", totalSaved),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "هذه الأرصدة تراكمت تلقائياً من الأسابيع المغلقة للأسر التي لم تستلم نصيبها.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section Title: Families List
        item {
            Text(
                text = "كشف أرصدة وأنصبة العائلات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (families.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد أسر مضافة حالياً. يمكنك إضافتها من الصفحة الرئيسية > إدارة الأسر.",
                    icon = Icons.Default.People
                )
            }
        } else {
            items(families) { family ->
                val balance = savedBalances[family.id] ?: 0.0
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("family_balance_card_${family.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = family.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "النصيب الأسبوعي المحدد: ${String.format(Locale.US, "%,.0f", family.portion)} SDG",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (balance > 0) Color(0xFFFFF4E5) else Color(0xFFF1F3F8),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%,.0f SDG", balance),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (balance > 0) Color(0xFF8B5000) else Color(0xFF44474E)
                                )
                            }
                        }

                        if (balance > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Clear/Pay all button
                                Button(
                                    onClick = {
                                        viewModel.clearFamilySavedBalance(family.id)
                                        Toast.makeText(context, "تم صرف كامل الرصيد المحفوظ لـ ${family.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE8F0FE),
                                        contentColor = Color(0xFF0061A4)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("pay_all_balance_${family.id}")
                                ) {
                                    Text("صرف كامل الرصيد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Pay part button
                                OutlinedButton(
                                    onClick = {
                                        selectedFamilyForPay = family
                                        payAmount = ""
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("pay_part_balance_${family.id}")
                                ) {
                                    Text("صرف جزء من الرصيد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "ليس لديه أي مستحقات متراكمة في الرصيد المحفوظ.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Weekly summary status overview
        item {
            Text(
                text = "متابعة وإغلاق الأسابيع",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (weeklySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد أسابيع مسجلة بعد لتسويتها.",
                    icon = Icons.Default.DateRange
                )
            }
        } else {
            items(weeklySummaries) { summary ->
                val isReported = reportedWeeks.contains(summary.weekKey)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("week_settlement_card_${summary.weekKey}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isReported) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
                                         else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(
                        1.dp, 
                        if (isReported) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) 
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Week Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            if (isReported) Color.Gray else Color(0xFF0061A4), 
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = "الأسبوع: ${summary.weekKey}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReported) Color.Gray else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isReported) Color(0xFFE8F0FE).copy(alpha = 0.5f) else Color(0xFFFFF4E5),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isReported) "تمت التسوية والتقرير" else "قيد التوزيع والتسوية",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReported) Color(0xFF0061A4).copy(alpha = 0.6f) else Color(0xFF8B5000)
                                )
                            }
                        }

                        // Summary Statistics Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "صافي ربح الركشات:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%,.0f SDG", summary.netRevenue),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                val totalActivePayout = families.filter { it.portion > 0 }.sumOf { it.portion }
                                Text(
                                    text = "إجمالي أنصبة الأسر:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%,.0f SDG", totalActivePayout),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Families options (if not reported, user can configure "دفع" or "حفظ")
                        if (!isReported) {
                            Text(
                                text = "حدّد خيار (دفع) أو (حفظ للرصيد) لكل عائلة لهذا الأسبوع:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val activeFamilies = families.filter { it.portion > 0 }
                            if (activeFamilies.isEmpty()) {
                                Text(
                                    text = "لا توجد أسر نشطة لتوزيعها.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                val currentPaid = paidFamiliesMap[summary.weekKey] ?: emptySet()
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    activeFamilies.forEach { family ->
                                        val isPaid = currentPaid.contains(family.id)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = family.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "النصيب المحدد: ${String.format(Locale.US, "%,.0f SDG", family.portion)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF0061A4),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            // Toggle selector buttons: [دفع] [حفظ]
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Pay button (دفع فوراً)
                                                val paySelected = isPaid
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (paySelected) Color(0xFFE8F5E9) else Color(0xFFF1F3F8)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (paySelected) Color(0xFF81C784) else Color.Transparent,
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable {
                                                            viewModel.toggleFamilyPayment(summary.weekKey, family.id, true)
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = if (paySelected) Color(0xFF2E7D32) else Color.Gray,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = "دفع فوراً",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (paySelected) Color(0xFF2E7D32) else Color.Gray
                                                        )
                                                    }
                                                }

                                                // Save button (حفظ للرصيد)
                                                val saveSelected = !isPaid
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (saveSelected) Color(0xFFFFF3E0) else Color(0xFFF1F3F8)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (saveSelected) Color(0xFFFFB74D) else Color.Transparent,
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable {
                                                            viewModel.toggleFamilyPayment(summary.weekKey, family.id, false)
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Save,
                                                            contentDescription = null,
                                                            tint = if (saveSelected) Color(0xFFE65100) else Color.Gray,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = "حفظ للرصيد",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (saveSelected) Color(0xFFE65100) else Color.Gray
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Save and Generate Report Button
                                Button(
                                    onClick = {
                                        viewModel.generateWeeklyDistributionReport(summary)
                                        Toast.makeText(context, "تم حفظ توزيع الأسبوع وتوليد تقرير إحصائي للمستندات!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0061A4),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("save_and_report_btn_${summary.weekKey}")
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("حفظ وعمل التقرير النهائي للأسبوع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // If reported, we show a clean summary of what happened
                            Text(
                                text = "حالة الاستلام والتوزيع المسجلة:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            val currentPaid = paidFamiliesMap[summary.weekKey] ?: emptySet()
                            val activeFamilies = families.filter { it.portion > 0 }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                activeFamilies.forEach { family ->
                                    val isPaid = currentPaid.contains(family.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = family.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = if (isPaid) Color(0xFF2E7D32) else Color(0xFFE65100),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (isPaid) "تم الاستلام نقدًا" else "تم الترحيل للرصيد المحفوظ",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPaid) Color(0xFF2E7D32) else Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Amount Claim Dialog
    selectedFamilyForPay?.let { family ->
        val balance = savedBalances[family.id] ?: 0.0
        AlertDialog(
            onDismissRequest = { selectedFamilyForPay = null },
            title = {
                Text(
                    text = "صرف جزء من رصيد ${family.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "الرصيد المحفوظ المتوفر: ${String.format(Locale.US, "%,.2f SDG", balance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("المبلغ المراد صرفه") },
                        placeholder = { Text("مثال: 5000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmount.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            Toast.makeText(context, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                        } else if (amount > balance) {
                            Toast.makeText(context, "المبلغ المدخل أكبر من الرصيد المتوفر!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.payAmountFromSavedBalance(family.id, amount)
                            Toast.makeText(context, "تم صرف $amount SDG بنجاح من رصيد ${family.name}", Toast.LENGTH_SHORT).show()
                            selectedFamilyForPay = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                ) {
                    Text("تأكيد الصرف")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFamilyForPay = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomRevenuesScreen(
    viewModel: AppViewModel,
    rooms: List<com.example.data.Room>,
    monthlyRoomSummaries: List<AppViewModel.RoomMonthlySummary>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current

    // Form fields
    var selectedRoom by remember { mutableStateOf<com.example.data.Room?>(null) }
    var roomAmount by remember { mutableStateOf("") }
    var roomNotes by remember { mutableStateOf("") }
    var roomDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var roomScreenshotUri by remember { mutableStateOf<Uri?>(null) }

    // Dialog state
    var showRoomsEditDialog by remember { mutableStateOf(false) }

    // Setup photo picker
    val roomPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) roomScreenshotUri = uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // FORM CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = Color(0xFF006874),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "تسجيل إيراد إيجار الغرف شهرياً",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006874)
                            )
                        }

                        // Rooms management settings button (just like family settings!)
                        Button(
                            onClick = { showRoomsEditDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0F7FA),
                                contentColor = Color(0xFF004D40)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("manage_rooms_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إدارة الغرف", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Room Selection Dropdown
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedRoom?.let { "${it.name} - ${it.tenantName}" } ?: "اختر الغرفة...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الغرفة") },
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("room_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            if (rooms.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("لا توجد غرف مسجلة، أضف غرف أولاً") },
                                    onClick = { dropdownExpanded = false }
                                )
                            } else {
                                rooms.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text("${r.name} - ${r.tenantName}") },
                                        onClick = {
                                            selectedRoom = r
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Amount
                    OutlinedTextField(
                        value = roomAmount,
                        onValueChange = { roomAmount = it },
                        label = { Text("المبلغ المستلم (SDG)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("room_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Notes
                    OutlinedTextField(
                        value = roomNotes,
                        onValueChange = { roomNotes = it },
                        label = { Text("ملاحظات (تفاصيل الدفع، الشهر...)") },
                        modifier = Modifier.fillMaxWidth().testTag("room_notes_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Screenshot Selection
                    ScreenshotPickerSection(
                        selectedUri = roomScreenshotUri,
                        onSelectRealPhoto = {
                            roomPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            roomScreenshotUri = MockImageGenerator.generate(context)
                        },
                        onClearPhoto = { roomScreenshotUri = null }
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            val amt = roomAmount.toDoubleOrNull()
                            if (selectedRoom == null) {
                                Toast.makeText(context, "الرجاء اختيار الغرفة", Toast.LENGTH_SHORT).show()
                            } else if (amt == null || amt <= 0) {
                                Toast.makeText(context, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addRoomRevenue(
                                    roomName = "${selectedRoom!!.name} - ${selectedRoom!!.tenantName}",
                                    amount = amt,
                                    date = roomDate,
                                    notes = roomNotes,
                                    screenshotUri = roomScreenshotUri
                                )
                                Toast.makeText(context, "تم حفظ إيراد الغرفة بنجاح وتوليد التقرير تلقائياً", Toast.LENGTH_SHORT).show()
                                // Reset fields
                                roomAmount = ""
                                roomNotes = ""
                                roomScreenshotUri = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006874)),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_room_revenue"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ إيراد الغرفة وتحصيلها", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // SECTION HEADER
        item {
            Text(
                text = "سجل الإيرادات المسجلة للغرف",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // LIST OF ENTRIES
        if (monthlyRoomSummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد إيرادات غرف مسجلة حتى الآن.",
                    icon = Icons.Default.ReceiptLong
                )
            }
        } else {
            items(monthlyRoomSummaries) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "شهر ${summary.monthKey}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF006874)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE0F7FA), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "المجموع: ${String.format(Locale.US, "%,.0f", summary.totalRevenue)} SDG",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF006874)
                                )
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            summary.revenues.forEach { rev ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = rev.roomName,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color(0xFF1B1B1F)
                                            )
                                            Text(
                                                text = "المبلغ: ${String.format(Locale.US, "%,.2f", rev.amount)} SDG",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF386A20)
                                            )
                                            if (rev.notes.isNotBlank()) {
                                                Text(
                                                    text = "ملاحظات: ${rev.notes}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF44474E)
                                                )
                                            }
                                            Text(
                                                text = "التاريخ: ${AppViewModel.formatSimpleDate(rev.date)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF44474E).copy(alpha = 0.6f)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (rev.screenshotPath != null) {
                                                IconButton(
                                                    onClick = { onViewImage(rev.screenshotPath) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Image,
                                                        contentDescription = "عرض السند",
                                                        tint = Color(0xFF0061A4)
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteRoomRevenue(rev)
                                                    Toast.makeText(context, "تم حذف الإيراد بنجاح", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف الإيراد",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRoomsEditDialog) {
        RoomsEditDialog(
            rooms = rooms,
            onDismiss = { showRoomsEditDialog = false },
            onAddRoom = { name, tenant -> viewModel.addRoom(name, tenant) },
            onUpdateRoom = { r -> viewModel.updateRoom(r) },
            onDeleteRoom = { r -> viewModel.deleteRoom(r) }
        )
    }
}

