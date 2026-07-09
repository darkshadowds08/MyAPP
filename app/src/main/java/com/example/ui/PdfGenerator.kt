package com.example.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object PdfGenerator {

    fun generatePdfBytes(context: Context, title: String, content: String): ByteArray {
        val pdfDocument = PdfDocument()
        
        // Setup text paint for content
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 14f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        
        // Setup title paint
        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#0061A4") // Beautiful matching blue
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        // Setup subtitle paint (date)
        val subtitlePaint = TextPaint().apply {
            color = Color.GRAY
            textSize = 12f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        // PDF Page size: A4 size at 72 DPI (595 x 842 points)
        val pageWidth = 595
        val pageHeight = 842
        val margin = 50f
        val rightMargin = pageWidth - margin // 545f
        val usableWidth = (pageWidth - 2 * margin).toInt() // 495

        // Create a single StaticLayout to paginate the lines correctly
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(content, 0, content.length, textPaint, usableWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                content,
                textPaint,
                usableWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.2f,
                0f,
                true
            )
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas

        // Header decoration: Draw a nice top accent line
        val accentPaint = Paint().apply {
            color = Color.parseColor("#0061A4")
            strokeWidth = 4f
        }
        canvas.drawLine(margin, 30f, rightMargin, 30f, accentPaint)

        // Draw Title
        var y = 70f
        canvas.drawText(title, rightMargin, y, titlePaint)
        y += 25f

        // Draw Date/Subtitle if needed
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(java.util.Date())
        canvas.drawText("تاريخ التصدير: $dateStr", rightMargin, y, subtitlePaint)
        y += 35f

        // Draw content separator line
        val separatorPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(margin, y - 10f, rightMargin, y - 10f, separatorPaint)

        // Draw body lines using the StaticLayout's measured lines
        val lineCount = staticLayout.lineCount
        for (i in 0 until lineCount) {
            val lineStart = staticLayout.getLineStart(i)
            val lineEnd = staticLayout.getLineEnd(i)
            val lineText = content.substring(lineStart, lineEnd).trimEnd('\n')

            // If we run out of vertical space on the current page, finish it and start a new one
            if (y > pageHeight - 60f) {
                pdfDocument.finishPage(currentPage)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                
                // Draw a simple top line on subsequent pages
                canvas.drawLine(margin, 30f, rightMargin, 30f, accentPaint)
                y = 60f
            }

            // Draw line text aligned to right for beautiful Arabic layout
            canvas.drawText(lineText, rightMargin, y, textPaint)
            
            // Adjust line height based on font metrics
            y += staticLayout.getLineBottom(i) - staticLayout.getLineTop(i)
        }

        // Draw Footer line & Page number on the last page
        val footerPaint = TextPaint().apply {
            color = Color.LTGRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawLine(margin, pageHeight - 40f, rightMargin, pageHeight - 40f, separatorPaint)
        canvas.drawText("صفحة $pageNumber", pageWidth / 2f, pageHeight - 25f, footerPaint)

        pdfDocument.finishPage(currentPage)
        
        val outputStream = ByteArrayOutputStream()
        try {
            pdfDocument.writeTo(outputStream)
        } finally {
            pdfDocument.close()
        }
        return outputStream.toByteArray()
    }

    fun saveReportToPdf(context: Context, title: String, content: String, weekName: String) {
        val cleanWeekName = weekName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val fileName = "تقرير_$cleanWeekName.pdf"

        try {
            val pdfBytes = generatePdfBytes(context, title, content)
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                @Suppress("DEPRECATION")
                resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            }

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pdfBytes)
                }
                Toast.makeText(context, "تم حفظ ملف الـ PDF بنجاح في مجلد التنزيلات (Downloads) باسم: $fileName", Toast.LENGTH_LONG).show()
            } else {
                // Fallback if MediaStore fails or on older versions
                val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val file = File(docsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(pdfBytes)
                }
                Toast.makeText(context, "تم حفظ الملف في مستندات التطبيق باسم: $fileName", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطأ أثناء حفظ ملف الـ PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
