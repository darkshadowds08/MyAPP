package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isUploading by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var cloudUrlResult by remember { mutableStateOf<String?>(null) }
    var restoreUrlInput by remember { mutableStateOf("") }
    var showCloudResultDialog by remember { mutableStateOf(false) }

    // Launcher to export data to a local file
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup { jsonString ->
                if (jsonString != null) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(jsonString.toByteArray())
                        }
                        Toast.makeText(context, "تم تصدير البيانات بنجاح ✅", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "فشل تصدير البيانات ❌", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "خطأ في إنشاء ملف النسخ الاحتياطي", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Launcher to import data from a local file
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    viewModel.importBackup(jsonString) { success ->
                        if (success) {
                            Toast.makeText(context, "تم استيراد البيانات بنجاح وتحديث التطبيق ✅", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "ملف غير صالح أو تالف ❌", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل قراءة الملف ❌", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "النسخ الاحتياطي والربط",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061A4)
                        )
                        Text(
                            text = "أمان البيانات ونقلها السحابي والربط مع تليجرام",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF44474E)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة للخلف",
                            tint = Color(0xFF0061A4)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF6F8FC)
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = Color(0xFFC4C6CF).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(0.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FF))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFD1E4FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF0061A4),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "حماية بياناتك هي أولويتنا",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001D36)
                        )
                        Text(
                            text = "قم بنسخ بياناتك احتياطياً بشكل دوري لمنع ضياعها، أو نقلها إلى هاتف آخر بسهولة تامة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF001D36).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // 1. Local Backup & Restore Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backup_local_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color(0xFF0061A4),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "النسخ الاحتياطي المحلي (ملفات دون إنترنت)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061A4)
                        )
                    }

                    Text(
                        text = "يقوم هذا الخيار بإنشاء ملف نسخة احتياطية مشفر بصيغة JSON وحفظه على جهازك، أو استيراد ملف محفوظ مسبقاً لتحديث بيانات التطبيق الحالية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF44474E)
                    )

                    HorizontalDivider(color = Color(0xFFC4C6CF).copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val fileName = "Group_Revenues_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
                                exportFileLauncher.launch(fileName)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0061A4),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير كملف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                importFileLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0E2EC),
                                contentColor = Color(0xFF1B1B1F)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استيراد ملف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Cloud Backup & Restore Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backup_cloud_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = Color(0xFF0061A4),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "النسخ الاحتياطي السحابي (أونلاين)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061A4)
                        )
                    }

                    Text(
                        text = "ارفع بياناتك بنقرة واحدة واحصل على رابط استعادة آمن لنقل بياناتك لأي جهاز متصل بالإنترنت فوراً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF44474E)
                    )

                    HorizontalDivider(color = Color(0xFFC4C6CF).copy(alpha = 0.3f))

                    if (isUploading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF0061A4))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("جاري إنشاء ورفع النسخة الاحتياطية سحابياً...", fontSize = 12.sp, color = Color(0xFF0061A4))
                        }
                    } else {
                        Button(
                            onClick = {
                                isUploading = true
                                viewModel.exportBackup { jsonString ->
                                    if (jsonString != null) {
                                        viewModel.uploadBackupToCloud(jsonString) { url ->
                                            isUploading = false
                                            if (url != null) {
                                                cloudUrlResult = url
                                                showCloudResultDialog = true
                                            } else {
                                                Toast.makeText(context, "فشل رفع النسخة السحابية. يرجى التحقق من اتصال الإنترنت ❌", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        isUploading = false
                                        Toast.makeText(context, "خطأ في إنشاء ملف النسخ الاحتياطي", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD1E4FF),
                                contentColor = Color(0xFF001D36)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("رفع نسخة احتياطية سحابية جديدة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = restoreUrlInput,
                            onValueChange = { restoreUrlInput = it },
                            label = { Text("أدخل رابط أو كود الاستعادة السحابية", fontSize = 11.sp) },
                            placeholder = { Text("مثال: https://file.io/XXXXXX", fontSize = 10.sp) },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(56.dp)
                                .testTag("cloud_restore_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0061A4),
                                unfocusedBorderColor = Color(0xFFC4C6CF)
                            )
                        )

                        if (isDownloading) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF0061A4))
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (restoreUrlInput.isBlank()) {
                                        Toast.makeText(context, "يرجى إدخال رابط أو كود استعادة صالح ⚠️", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    
                                    val cleanInput = restoreUrlInput.trim()
                                    val downloadUrl = if (cleanInput.startsWith("http://") || cleanInput.startsWith("https://")) {
                                        cleanInput
                                    } else {
                                        "https://file.io/$cleanInput"
                                    }

                                    isDownloading = true
                                    viewModel.downloadAndImportBackupFromCloud(downloadUrl) { success ->
                                        isDownloading = false
                                        if (success) {
                                            restoreUrlInput = ""
                                            Toast.makeText(context, "تم استعادة البيانات سحابياً وتحديث التطبيق بنجاح ✅", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "فشل تحميل البيانات. تأكد من صحة الرابط والاتصال بالإنترنت ❌", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE0E2EC),
                                    contentColor = Color(0xFF1B1B1F)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استعادة سحابية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Telegram Reports setup Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backup_telegram_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color(0xFF0088CC),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "إرسال التقارير أسبوعياً إلى تليجرام (PDF)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0088CC)
                        )
                    }

                    Text(
                        text = "يمكنك ربط التطبيق بحسابك الشخصي على تليجرام أو مجموعة خاصة، لإرسال التقرير الأسبوعي تلقائياً بصيغة PDF فور اعتماده وحفظه.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF44474E)
                    )

                    HorizontalDivider(color = Color(0xFFC4C6CF).copy(alpha = 0.3f))

                    var botTokenInput by remember { mutableStateOf(viewModel.getTelegramToken()) }
                    var chatIdInput by remember { mutableStateOf(viewModel.getTelegramChatId()) }
                    var isAutoSendEnabled by remember { mutableStateOf(viewModel.getTelegramAutoSend()) }

                    OutlinedTextField(
                        value = botTokenInput,
                        onValueChange = { 
                            botTokenInput = it
                            viewModel.saveTelegramConfig(it, chatIdInput, isAutoSendEnabled)
                        },
                        label = { Text("رمز توكن البوت (Bot Token)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: 123456:ABC-def123...", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(56.dp).testTag("telegram_token_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0088CC),
                            unfocusedBorderColor = Color(0xFFC4C6CF)
                        )
                    )

                    OutlinedTextField(
                        value = chatIdInput,
                        onValueChange = { 
                            chatIdInput = it
                            viewModel.saveTelegramConfig(botTokenInput, it, isAutoSendEnabled)
                        },
                        label = { Text("معرّف الدردشة أو حسابك (Chat ID)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: 987654321", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(56.dp).testTag("telegram_chat_id_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0088CC),
                            unfocusedBorderColor = Color(0xFFC4C6CF)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Switch(
                                checked = isAutoSendEnabled,
                                onCheckedChange = { 
                                    isAutoSendEnabled = it
                                    viewModel.saveTelegramConfig(botTokenInput, chatIdInput, it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF0088CC)
                                )
                            )
                            Text(
                                text = "إرسال أسبوعياً تلقائياً عند الحفظ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B1B1F)
                            )
                        }

                        // Test Telegram Button
                        var isTestingConnection by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                if (botTokenInput.isBlank() || chatIdInput.isBlank()) {
                                    Toast.makeText(context, "الرجاء إدخال التوكن ومعرّف الدردشة أولاً ⚠️", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isTestingConnection = true
                                viewModel.sendReportToTelegram(
                                    "تحديث تجربة الاتصال 🔌",
                                    "تم ربط تطبيق الإيرادات بحسابك على تليجرام بنجاح! 🎉\nستتلقى الآن التقارير المالية أسبوعياً بصيغة PDF فور حفظها.",
                                    "تجربة الاتصال"
                                ) { success ->
                                    isTestingConnection = false
                                    if (success) {
                                        Toast.makeText(context, "تم إرسال رسالة تجريبية بنجاح! تحقق من تليجرام ✅", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "فشل الاتصال. تأكد من صحة البيانات والاتصال بالإنترنت ❌", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0E2EC),
                                contentColor = Color(0xFF1B1B1F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF1B1B1F), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اختبار الاتصال", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Cloud Result Dialog to show the generated URL
    if (showCloudResultDialog && cloudUrlResult != null) {
        AlertDialog(
            onDismissRequest = { showCloudResultDialog = false },
            confirmButton = {
                Button(
                    onClick = { showCloudResultDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                ) {
                    Text("تم")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Backup URL", cloudUrlResult)
                        clipboardManager.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ رابط الاستعادة بنجاح 📋", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("نسخ الرابط 📋")
                }
            },
            title = {
                Text(
                    text = "اكتمل الرفع السحابي بنجاح! 🎉",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "تم حفظ بياناتك بنجاح على خادم تخزين آمن. انسخ الرابط أدناه واستخدمه لاستيراد البيانات على أي جهاز آخر.",
                        fontSize = 13.sp,
                        color = Color(0xFF44474E),
                        textAlign = TextAlign.Right
                    )
                    
                    OutlinedTextField(
                        value = cloudUrlResult ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E2EC).copy(alpha = 0.5f),
                            unfocusedContainerColor = Color(0xFFE0E2EC).copy(alpha = 0.5f),
                            focusedBorderColor = Color(0xFF0061A4)
                        )
                    )
                    
                    Text(
                        text = "ملاحظة: هذا الرابط صالح للاستخدام لنقل البيانات ويُنصح بالاحتفاظ به للاستعادة.",
                        fontSize = 11.sp,
                        color = Color(0xFFBA1A1A),
                        textAlign = TextAlign.Right,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}
