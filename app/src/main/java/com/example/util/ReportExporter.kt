package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.InspectionItemEvaluation
import com.example.data.model.InspectionVisit
import com.example.data.model.QualityReportItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private const val PAGE_WIDTH = 595 // A4 standard width (points)
    private const val PAGE_HEIGHT = 842 // A4 standard height (points)
    private const val MARGIN = 36f // 0.5 inch margin

    // ==========================================
    // 1. Direct PDF Generation using native PdfDocument
    // ==========================================
    fun generateVisitPdfFile(context: Context, visit: InspectionVisit, items: List<InspectionItemEvaluation>): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val safeNum = visit.visitNumber.replace(Regex("[^a-zA-Z0-9_\\-\\u0600-\\u06FF]"), "_")
        val pdfFile = File(reportsDir, "تقرير_جودة_${safeNum}_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.DEFAULT
        }
        val boldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(23, 54, 93) // Raneen Navy
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.rgb(226, 232, 240)
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = MARGIN

        // Helper to draw Header Banner
        fun drawHeader() {
            // Header rectangle
            fillPaint.color = Color.rgb(23, 54, 93) // #17365D
            fillPaint.style = Paint.Style.FILL
            val bannerRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 54f)
            canvas.drawRoundRect(bannerRect, 8f, 8f, fillPaint)

            // Orange accent line
            fillPaint.color = Color.rgb(224, 83, 38) // #E05326
            canvas.drawRect(MARGIN, currentY + 50f, PAGE_WIDTH - MARGIN, currentY + 54f, fillPaint)

            // Header Titles
            boldPaint.color = Color.WHITE
            boldPaint.textSize = 15f
            canvas.drawText("شركة رنين — إدارة وتوكيد الجودة", MARGIN + 16f, currentY + 24f, boldPaint)

            textPaint.color = Color.rgb(203, 213, 225)
            textPaint.textSize = 10f
            canvas.drawText("تقرير التدقيق والتقييم الميداني", MARGIN + 16f, currentY + 40f, textPaint)

            // Visit Badge
            val badgeText = "رقم: ${visit.visitNumber}"
            boldPaint.textSize = 11f
            boldPaint.color = Color.rgb(254, 215, 170)
            val badgeWidth = boldPaint.measureText(badgeText)
            canvas.drawText(badgeText, PAGE_WIDTH - MARGIN - badgeWidth - 16f, currentY + 32f, boldPaint)

            currentY += 66f
        }

        // Helper to draw Footer
        fun drawFooter(pageNum: Int) {
            strokePaint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(MARGIN, PAGE_HEIGHT - 30f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 30f, strokePaint)

            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textSize = 8f
            canvas.drawText("نظام إدارة الجودة - شركة رنين • تم التصدير: ${visit.dateFormatted}", MARGIN, PAGE_HEIGHT - 18f, textPaint)

            val pageStr = "صفحة $pageNum"
            val pWidth = textPaint.measureText(pageStr)
            canvas.drawText(pageStr, PAGE_WIDTH - MARGIN - pWidth, PAGE_HEIGHT - 18f, textPaint)
        }

        // Draw First Page Header
        drawHeader()

        // Metadata Box
        fillPaint.color = Color.rgb(248, 250, 252)
        val metaRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 48f)
        canvas.drawRoundRect(metaRect, 6f, 6f, fillPaint)
        strokePaint.color = Color.rgb(203, 213, 225)
        canvas.drawRoundRect(metaRect, 6f, 6f, strokePaint)

        boldPaint.color = Color.rgb(23, 54, 93)
        boldPaint.textSize = 9.5f
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 9.5f

        canvas.drawText("الفرع / المشروع:", MARGIN + 12f, currentY + 18f, boldPaint)
        canvas.drawText(visit.projectName, MARGIN + 90f, currentY + 18f, textPaint)

        canvas.drawText("الموقع:", MARGIN + 12f, currentY + 36f, boldPaint)
        canvas.drawText(if (visit.location.isNotBlank()) visit.location else "الفرع الرئيسي", MARGIN + 90f, currentY + 36f, textPaint)

        canvas.drawText("المفتش:", PAGE_WIDTH / 2f + 10f, currentY + 18f, boldPaint)
        canvas.drawText(visit.inspectorName, PAGE_WIDTH / 2f + 70f, currentY + 18f, textPaint)

        canvas.drawText("تاريخ الفحص:", PAGE_WIDTH / 2f + 10f, currentY + 36f, boldPaint)
        canvas.drawText(visit.dateFormatted, PAGE_WIDTH / 2f + 70f, currentY + 36f, textPaint)

        currentY += 58f

        // KPI Summary Strip
        fillPaint.color = Color.rgb(240, 249, 255)
        val kpiRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 44f)
        canvas.drawRoundRect(kpiRect, 6f, 6f, fillPaint)
        strokePaint.color = Color.rgb(186, 230, 253)
        canvas.drawRoundRect(kpiRect, 6f, 6f, strokePaint)

        val scoreColor = when {
            visit.scorePercentage >= 85 -> Color.rgb(22, 101, 52)
            visit.scorePercentage >= 65 -> Color.rgb(180, 83, 9)
            else -> Color.rgb(185, 28, 28)
        }
        boldPaint.color = scoreColor
        boldPaint.textSize = 18f
        val scoreText = "${visit.scorePercentage}%"
        canvas.drawText(scoreText, MARGIN + 18f, currentY + 28f, boldPaint)

        textPaint.color = Color.rgb(71, 85, 105)
        textPaint.textSize = 8f
        canvas.drawText("نسبة الامتثال", MARGIN + 18f, currentY + 38f, textPaint)

        val colW = (PAGE_WIDTH - 2 * MARGIN - 80f) / 4f
        var startX = MARGIN + 80f

        fun drawKpiItem(title: String, value: String, valColor: Int) {
            boldPaint.color = valColor
            boldPaint.textSize = 12f
            canvas.drawText(value, startX + 10f, currentY + 22f, boldPaint)
            textPaint.color = Color.rgb(71, 85, 105)
            textPaint.textSize = 8f
            canvas.drawText(title, startX + 10f, currentY + 36f, textPaint)
            startX += colW
        }

        drawKpiItem("المقيمة", "${visit.totalEvaluated}/${items.size}", Color.rgb(15, 23, 42))
        drawKpiItem("مطابق ✅", "${visit.compliantCount}", Color.rgb(22, 101, 52))
        drawKpiItem("غير مطابق ❌", "${visit.nonCompliantCount}", Color.rgb(185, 28, 28))
        drawKpiItem("لا ينطبق", "${items.count { it.state == "لا ينطبق" }}", Color.rgb(100, 116, 139))

        currentY += 56f

        // Table Header
        fun drawTableHeader() {
            fillPaint.color = Color.rgb(23, 54, 93)
            val thRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 20f)
            canvas.drawRect(thRect, fillPaint)

            boldPaint.color = Color.WHITE
            boldPaint.textSize = 8.5f

            canvas.drawText("#", MARGIN + 4f, currentY + 14f, boldPaint)
            canvas.drawText("الكود", MARGIN + 22f, currentY + 14f, boldPaint)
            canvas.drawText("القسم والمعيار", MARGIN + 62f, currentY + 14f, boldPaint)
            canvas.drawText("التقييم", MARGIN + 270f, currentY + 14f, boldPaint)
            canvas.drawText("الملاحظة والإجراء التصحيحي", MARGIN + 335f, currentY + 14f, boldPaint)

            currentY += 20f
        }

        drawTableHeader()

        // Table Rows
        items.forEachIndexed { idx, item ->
            val rowHeight = 24f

            // Check if page overflow
            if (currentY + rowHeight > PAGE_HEIGHT - 60f) {
                drawFooter(pageNumber)
                document.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN

                drawHeader()
                drawTableHeader()
            }

            // Zebra background
            if (idx % 2 == 0) {
                fillPaint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, fillPaint)
            }

            // Row border
            strokePaint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, strokePaint)

            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textSize = 8f
            canvas.drawText("${idx + 1}", MARGIN + 4f, currentY + 15f, textPaint)

            boldPaint.color = Color.rgb(23, 54, 93)
            boldPaint.textSize = 8f
            canvas.drawText(item.code, MARGIN + 20f, currentY + 15f, boldPaint)

            textPaint.color = Color.rgb(15, 23, 42)
            textPaint.textSize = 8f
            val label = "[${item.section}] ${item.name}".take(38)
            canvas.drawText(label, MARGIN + 62f, currentY + 15f, textPaint)

            // State Badge
            val (stColor, stText) = when (item.state) {
                "مطابق" -> Pair(Color.rgb(22, 101, 52), "مطابق")
                "غير مطابق" -> Pair(Color.rgb(185, 28, 28), "غير مطابق (${item.severity})")
                "لا ينطبق" -> Pair(Color.rgb(100, 116, 139), "لا ينطبق")
                else -> Pair(Color.rgb(148, 163, 184), "-")
            }
            boldPaint.color = stColor
            boldPaint.textSize = 8f
            canvas.drawText(stText, MARGIN + 270f, currentY + 15f, boldPaint)

            // Note/Action
            val noteActionText = if (item.note.isNotBlank() || item.action.isNotBlank()) {
                "${item.note}${if (item.action.isNotBlank()) " | إجراء: ${item.action}" else ""}".take(34)
            } else {
                "-"
            }
            textPaint.color = Color.rgb(71, 85, 105)
            textPaint.textSize = 7.5f
            canvas.drawText(noteActionText, MARGIN + 335f, currentY + 15f, textPaint)

            currentY += rowHeight
        }

        // Notes & Signatures section
        if (currentY + 70f > PAGE_HEIGHT - 60f) {
            drawFooter(pageNumber)
            document.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            currentY = MARGIN
            drawHeader()
        }

        if (visit.notes.isNotBlank()) {
            currentY += 10f
            fillPaint.color = Color.rgb(254, 243, 199)
            val noteBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 34f)
            canvas.drawRoundRect(noteBox, 4f, 4f, fillPaint)
            boldPaint.color = Color.rgb(146, 64, 14)
            boldPaint.textSize = 8f
            canvas.drawText("ملاحظات عامة: ${visit.notes.take(90)}", MARGIN + 10f, currentY + 20f, boldPaint)
            currentY += 40f
        } else {
            currentY += 14f
        }

        // Signatures
        val sigY = currentY
        strokePaint.color = Color.rgb(148, 163, 184)
        strokePaint.strokeWidth = 1f

        // Inspector signature
        canvas.drawLine(MARGIN + 20f, sigY + 30f, MARGIN + 160f, sigY + 30f, strokePaint)
        boldPaint.color = Color.rgb(23, 54, 93)
        boldPaint.textSize = 8.5f
        canvas.drawText("توقيع مفتش الجودة (${visit.inspectorName})", MARGIN + 20f, sigY + 42f, boldPaint)

        // Branch Manager signature
        canvas.drawLine(PAGE_WIDTH - MARGIN - 160f, sigY + 30f, PAGE_WIDTH - MARGIN - 20f, sigY + 30f, strokePaint)
        canvas.drawText("توقيع واستلام مدير الفرع", PAGE_WIDTH - MARGIN - 150f, sigY + 42f, boldPaint)

        drawFooter(pageNumber)
        document.finishPage(page)

        // Write to file
        val outputStream = FileOutputStream(pdfFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return pdfFile
    }

    // ==========================================
    // 2. Direct PDF for Non-Compliance Reports List
    // ==========================================
    fun generateReportsListPdfFile(context: Context, reports: List<QualityReportItem>, title: String): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val pdfFile = File(reportsDir, "تقرير_الملاحظات_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.DEFAULT
        }
        val boldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(23, 54, 93)
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.rgb(226, 232, 240)
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = MARGIN

        fun drawHeader() {
            fillPaint.color = Color.rgb(23, 54, 93)
            val bannerRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 50f)
            canvas.drawRoundRect(bannerRect, 8f, 8f, fillPaint)

            fillPaint.color = Color.rgb(224, 83, 38)
            canvas.drawRect(MARGIN, currentY + 46f, PAGE_WIDTH - MARGIN, currentY + 50f, fillPaint)

            boldPaint.color = Color.WHITE
            boldPaint.textSize = 14f
            canvas.drawText("شركة رنين — تقرير متابعة الملاحظات والمخالفات", MARGIN + 16f, currentY + 22f, boldPaint)

            textPaint.color = Color.rgb(203, 213, 225)
            textPaint.textSize = 9.5f
            canvas.drawText("$title • إجمالي البنود: ${reports.size}", MARGIN + 16f, currentY + 38f, textPaint)

            currentY += 60f
        }

        fun drawFooter(pageNum: Int) {
            strokePaint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(MARGIN, PAGE_HEIGHT - 30f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 30f, strokePaint)

            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textSize = 8f
            canvas.drawText("نظام إدارة الجودة - شركة رنين • تم التصدير: ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar")).format(Date())}", MARGIN, PAGE_HEIGHT - 18f, textPaint)

            val pageStr = "صفحة $pageNum"
            val pWidth = textPaint.measureText(pageStr)
            canvas.drawText(pageStr, PAGE_WIDTH - MARGIN - pWidth, PAGE_HEIGHT - 18f, textPaint)
        }

        fun drawTableHeader() {
            fillPaint.color = Color.rgb(23, 54, 93)
            val thRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 20f)
            canvas.drawRect(thRect, fillPaint)

            boldPaint.color = Color.WHITE
            boldPaint.textSize = 8f

            canvas.drawText("#", MARGIN + 4f, currentY + 14f, boldPaint)
            canvas.drawText("رقم الزيارة", MARGIN + 20f, currentY + 14f, boldPaint)
            canvas.drawText("الفرع", MARGIN + 70f, currentY + 14f, boldPaint)
            canvas.drawText("المعيار المخالف", MARGIN + 160f, currentY + 14f, boldPaint)
            canvas.drawText("الخطورة", MARGIN + 290f, currentY + 14f, boldPaint)
            canvas.drawText("الحالة", MARGIN + 330f, currentY + 14f, boldPaint)
            canvas.drawText("الملاحظة والإجراء", MARGIN + 380f, currentY + 14f, boldPaint)

            currentY += 20f
        }

        drawHeader()
        drawTableHeader()

        reports.forEachIndexed { idx, r ->
            val rowHeight = 24f

            if (currentY + rowHeight > PAGE_HEIGHT - 50f) {
                drawFooter(pageNumber)
                document.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN

                drawHeader()
                drawTableHeader()
            }

            if (idx % 2 == 0) {
                fillPaint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, fillPaint)
            }

            strokePaint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, strokePaint)

            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textSize = 8f
            canvas.drawText("${idx + 1}", MARGIN + 4f, currentY + 15f, textPaint)

            boldPaint.color = Color.rgb(23, 54, 93)
            boldPaint.textSize = 7.5f
            canvas.drawText(r.visitNumber, MARGIN + 20f, currentY + 15f, boldPaint)

            textPaint.color = Color.rgb(15, 23, 42)
            canvas.drawText(r.projectName.take(16), MARGIN + 70f, currentY + 15f, textPaint)

            val ruleText = "[${r.ruleCode}] ${r.ruleName}".take(24)
            canvas.drawText(ruleText, MARGIN + 160f, currentY + 15f, textPaint)

            val sevColor = when (r.severity) {
                "عالية" -> Color.rgb(239, 68, 68)
                "متوسطة" -> Color.rgb(245, 158, 11)
                else -> Color.rgb(59, 130, 246)
            }
            boldPaint.color = sevColor
            boldPaint.textSize = 7.5f
            canvas.drawText(r.severity, MARGIN + 290f, currentY + 15f, boldPaint)

            val statusColor = when (r.status) {
                "مغلقة", "تم الحل" -> Color.rgb(22, 163, 74)
                "قيد المعالجة" -> Color.rgb(2, 132, 199)
                else -> Color.rgb(180, 83, 9)
            }
            boldPaint.color = statusColor
            boldPaint.textSize = 7.5f
            canvas.drawText(r.status, MARGIN + 330f, currentY + 15f, boldPaint)

            val noteSummary = if (r.note.isNotBlank()) r.note.take(24) else "-"
            textPaint.color = Color.rgb(71, 85, 105)
            textPaint.textSize = 7f
            canvas.drawText(noteSummary, MARGIN + 380f, currentY + 15f, textPaint)

            currentY += rowHeight
        }

        drawFooter(pageNumber)
        document.finishPage(page)

        val outputStream = FileOutputStream(pdfFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return pdfFile
    }

    // ==========================================
    // 3. User Facing Action: Export & Share PDF File
    // ==========================================
    fun exportVisitPdfAndShare(context: Context, visit: InspectionVisit, items: List<InspectionItemEvaluation>) {
        try {
            val pdfFile = generateVisitPdfFile(context, visit, items)
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "تقرير جودة رنين - ${visit.projectName} (${visit.visitNumber})")
                putExtra(Intent.EXTRA_TEXT, "مرفق تقرير تقييم الجودة الميداني للفرع: ${visit.projectName} - نسبة الجودة: ${visit.scorePercentage}%")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "مشاركة ملف PDF عبر...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(chooser)
            Toast.makeText(context, "تم استخراج ملف PDF بنجاح!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر تصدير PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportReportsListPdfAndShare(context: Context, reports: List<QualityReportItem>, title: String = "تقرير الملاحظات والمخالفات") {
        if (reports.isEmpty()) {
            Toast.makeText(context, "لا توجد ملاحظات أو مخالفات للتصدير.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val pdfFile = generateReportsListPdfFile(context, reports, title)
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "تقرير ملاحظات الجودة - شركة رنين")
                putExtra(Intent.EXTRA_TEXT, "$title - إجمالي الملاحظات: ${reports.size}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "مشاركة تقرير PDF عبر...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(chooser)
            Toast.makeText(context, "تم تجهيز ملف PDF بنجاح!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر تصدير PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // 4. HTML Generation for WebView & System Print
    // ==========================================
    fun generateVisitHtmlReport(visit: InspectionVisit, items: List<InspectionItemEvaluation>): String {
        val evaluated = items.filter { it.state == "مطابق" || it.state == "غير مطابق" }
        val matched = evaluated.count { it.state == "مطابق" }
        val nonMatched = evaluated.count { it.state == "غير مطابق" }
        val notApplicable = items.count { it.state == "لا ينطبق" }

        val scoreBadgeClass = when {
            visit.scorePercentage >= 85 -> "badge-good"
            visit.scorePercentage >= 65 -> "badge-warning"
            else -> "badge-danger"
        }

        val rows = items.mapIndexed { idx, i ->
            val (stateClass, stateLabel) = when (i.state) {
                "مطابق" -> Pair("state-ok", "مطابق ✅")
                "غير مطابق" -> Pair("state-bad", "غير مطابق ❌")
                "لا ينطبق" -> Pair("state-na", "لا ينطبق —")
                else -> Pair("state-empty", "لم يقيّم")
            }

            val severityHtml = if (i.state == "غير مطابق" && i.severity.isNotBlank()) {
                val sevClass = when (i.severity) {
                    "عالية" -> "sev-high"
                    "متوسطة" -> "sev-med"
                    else -> "sev-low"
                }
                "<span class='sev-badge $sevClass'>${i.severity}</span>"
            } else {
                "<span style='color:#94a3b8;'>-</span>"
            }

            """
            <tr>
              <td class="text-center">${idx + 1}</td>
              <td class="text-center"><b>${i.code}</b></td>
              <td>${i.section}</td>
              <td><b>${i.name}</b></td>
              <td class="text-center"><span class='state-badge $stateClass'>$stateLabel</span></td>
              <td class="text-center">$severityHtml</td>
              <td>${if (i.note.isNotBlank()) i.note else "<span style='color:#94a3b8;'>-</span>"}</td>
              <td>${if (i.action.isNotBlank()) "<b style='color:#0284c7;'>${i.action}</b>" else "<span style='color:#94a3b8;'>-</span>"}</td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        return """
        <!DOCTYPE html>
        <html lang="ar" dir="rtl">
        <head>
          <meta charset="UTF-8">
          <title>تقرير تقييم الجودة - ${visit.visitNumber}</title>
          <style>
            * { box-sizing: border-box; font-family: 'Cairo', 'Segoe UI', Tahoma, Arial, sans-serif; }
            body { margin: 0; padding: 24px; color: #0f172a; background: #fff; line-height: 1.5; direction: rtl; text-align: right; }
            
            .header-banner {
              background: linear-gradient(135deg, #0d223a 0%, #17365D 60%, #254d7e 100%);
              color: #ffffff;
              padding: 24px;
              border-radius: 14px;
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 20px;
              border-bottom: 4px solid #E05326;
            }
            .header-title h1 { margin: 0 0 6px 0; font-size: 24px; font-weight: 800; }
            .header-title p { margin: 0; font-size: 14px; color: #cbd5e1; }
            .header-tag {
              background: rgba(224, 83, 38, 0.2);
              border: 1px solid #E05326;
              color: #ffedd5;
              padding: 6px 14px;
              border-radius: 20px;
              font-size: 13px;
              font-weight: 700;
            }

            .info-grid {
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 12px;
              margin-bottom: 20px;
            }
            .info-card {
              background: #f8fafc;
              border: 1px solid #e2e8f0;
              border-radius: 10px;
              padding: 12px 16px;
              font-size: 13px;
            }
            .info-card b { color: #17365D; }

            .kpi-section {
              background: #f0f9ff;
              border: 1px solid #bae6fd;
              border-radius: 12px;
              padding: 16px;
              display: flex;
              justify-content: space-around;
              align-items: center;
              margin-bottom: 24px;
              text-align: center;
            }
            .score-circle {
              font-size: 36px;
              font-weight: 800;
              line-height: 1;
            }
            .badge-good { color: #15803d; }
            .badge-warning { color: #b45309; }
            .badge-danger { color: #b91c1c; }

            .kpi-item { display: flex; flex-direction: column; gap: 4px; }
            .kpi-label { font-size: 12px; color: #475569; }
            .kpi-val { font-size: 16px; font-weight: 700; color: #0f172a; }

            table.data-table {
              width: 100%;
              border-collapse: collapse;
              margin-top: 10px;
              font-size: 12px;
              border: 1px solid #cbd5e1;
              border-radius: 8px;
              overflow: hidden;
            }
            table.data-table th {
              background: #17365D;
              color: #ffffff;
              padding: 10px 8px;
              font-weight: 700;
              text-align: right;
              border: 1px solid #1e40af;
            }
            table.data-table td {
              border: 1px solid #e2e8f0;
              padding: 8px 10px;
              vertical-align: top;
            }
            table.data-table tr:nth-child(even) { background: #f8fafc; }
            .text-center { text-align: center !important; }

            .state-badge {
              display: inline-block;
              padding: 3px 8px;
              border-radius: 6px;
              font-size: 11px;
              font-weight: 700;
            }
            .state-ok { background: #dcfce7; color: #166534; border: 1px solid #86efac; }
            .state-bad { background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; }
            .state-na { background: #f1f5f9; color: #475569; }
            .state-empty { background: #f8fafc; color: #94a3b8; }

            .sev-badge {
              display: inline-block;
              padding: 2px 6px;
              border-radius: 4px;
              font-size: 10px;
              font-weight: 700;
            }
            .sev-high { background: #ef4444; color: #fff; }
            .sev-med { background: #f59e0b; color: #fff; }
            .sev-low { background: #3b82f6; color: #fff; }

            .notes-box {
              margin-top: 22px;
              background: #fffbeb;
              border: 1px solid #fde68a;
              border-radius: 10px;
              padding: 14px 18px;
              font-size: 13px;
            }
            .notes-box h4 { margin: 0 0 6px 0; color: #92400e; font-size: 14px; }

            .signatures-grid {
              margin-top: 30px;
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 20px;
              border-top: 1px dashed #cbd5e1;
              padding-top: 20px;
            }
            .sig-box {
              border: 1px solid #e2e8f0;
              border-radius: 8px;
              padding: 12px;
              text-align: center;
              background: #fafafa;
            }
            .sig-title { font-weight: 700; font-size: 13px; color: #17365D; margin-bottom: 40px; }
            .sig-line { border-bottom: 1px solid #94a3b8; width: 80%; margin: 0 auto; }

            .footer {
              margin-top: 24px;
              text-align: center;
              font-size: 11px;
              color: #64748b;
              border-top: 1px solid #e2e8f0;
              padding-top: 12px;
            }
          </style>
        </head>
        <body>
          <div class="header-banner">
            <div class="header-title">
              <h1>شركة رنين — إدارة وتوكيد الجودة</h1>
              <p>تقرير الزيارة والتدقيق الميداني الرسمي</p>
            </div>
            <div class="header-tag">
              رقم الزيارة: ${visit.visitNumber}
            </div>
          </div>

          <div class="info-grid">
            <div class="info-card">
              <div><b>اسم الفرع / المشروع:</b> ${visit.projectName}</div>
              <div style="margin-top: 6px;"><b>الموقع:</b> ${if (visit.location.isNotBlank()) visit.location else "الفرع الرئيسي"}</div>
            </div>
            <div class="info-card">
              <div><b>المفتش المسؤول:</b> ${visit.inspectorName}</div>
              <div style="margin-top: 6px;"><b>تاريخ وتوقيت الفحص:</b> ${visit.dateFormatted}</div>
            </div>
          </div>

          <div class="kpi-section">
            <div class="kpi-item">
              <span class="kpi-label">درجة الجودة والامتثال الكلية</span>
              <span class="score-circle $scoreBadgeClass">${visit.scorePercentage}%</span>
            </div>
            <div class="kpi-item">
              <span class="kpi-label">إجمالي البنود المقيمة</span>
              <span class="kpi-val">${evaluated.size} من أصل ${items.size}</span>
            </div>
            <div class="kpi-item">
              <span class="kpi-label">البنود المطابقة ✅</span>
              <span class="kpi-val badge-good">$matched</span>
            </div>
            <div class="kpi-item">
              <span class="kpi-label">الملاحظات والمخالفات ❌</span>
              <span class="kpi-val badge-danger">$nonMatched</span>
            </div>
            <div class="kpi-item">
              <span class="kpi-label">لا تنطبق —</span>
              <span class="kpi-val">$notApplicable</span>
            </div>
          </div>

          <h3 style="color: #17365D; margin: 0 0 10px 0; font-size: 15px;">جدول تدقيق معايير الجودة والعمليات:</h3>
          <table class="data-table">
            <thead>
              <tr>
                <th style="width: 4%;">#</th>
                <th style="width: 8%;">الكود</th>
                <th style="width: 14%;">القسم</th>
                <th style="width: 22%;">المعيار</th>
                <th style="width: 12%;">النتيجة</th>
                <th style="width: 8%;">الخطورة</th>
                <th style="width: 16%;">الملاحظة الفنية</th>
                <th style="width: 16%;">الإجراء التصحيحي المطلوب</th>
              </tr>
            </thead>
            <tbody>
              $rows
            </tbody>
          </table>

          ${if (visit.notes.isNotBlank()) """
          <div class="notes-box">
            <h4>📝 الملاحظات والتوجيهات العامة من المفتش:</h4>
            <p style="margin: 0;">${visit.notes}</p>
          </div>
          """ else ""}

          <div class="signatures-grid">
            <div class="sig-box">
              <div class="sig-title">توقيع مفتش الجودة</div>
              <div class="sig-line"></div>
              <div style="font-size: 11px; color: #64748b; margin-top: 4px;">${visit.inspectorName}</div>
            </div>
            <div class="sig-box">
              <div class="sig-title">توقيع واستلام مدير الفرع</div>
              <div class="sig-line"></div>
              <div style="font-size: 11px; color: #64748b; margin-top: 4px;">إدارة فرع ${visit.projectName}</div>
            </div>
          </div>

          <div class="footer">
            تم استخراج هذا التقرير رسمياً بواسطة تطبيق نظام إدارة الجودة - شركة رنين • تم التصدير بتاريخ: ${SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale("ar")).format(Date())}
          </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun printOrSaveVisitPdf(context: Context, visit: InspectionVisit, items: List<InspectionItemEvaluation>) {
        try {
            val htmlContent = generateVisitHtmlReport(visit, items)
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    if (printManager != null) {
                        val printAdapter = webView.createPrintDocumentAdapter("تقرير_جودة_${visit.visitNumber}")
                        val jobName = "تقرير جودة رنين - ${visit.visitNumber}"
                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build()
                        printManager.print(jobName, printAdapter, printAttributes)
                    } else {
                        Toast.makeText(context, "خدمة الطباعة غير متوفرة", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=utf-8", "UTF-8", null)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حدث خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun printOrSaveReportsListPdf(context: Context, reports: List<QualityReportItem>, title: String) {
        try {
            val total = reports.size
            val rows = reports.mapIndexed { idx, r ->
                """
                <tr>
                  <td style="text-align:center;">${idx + 1}</td>
                  <td style="text-align:center;"><b>${r.visitNumber}</b></td>
                  <td><b>${r.projectName}</b></td>
                  <td>[${r.ruleCode}] ${r.ruleName}</td>
                  <td style="text-align:center;">${r.severity}</td>
                  <td style="text-align:center;">${r.status}</td>
                  <td>${r.note}</td>
                  <td>${r.action}</td>
                  <td style="text-align:center;">${r.dateFormatted}</td>
                </tr>
                """.trimIndent()
            }.joinToString("\n")

            val htmlContent = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
              <meta charset="UTF-8">
              <title>$title</title>
              <style>
                body { font-family: 'Segoe UI', Tahoma, sans-serif; padding: 20px; direction: rtl; text-align: right; }
                h1 { color: #17365D; border-bottom: 2px solid #E05326; padding-bottom: 8px; font-size: 20px; }
                table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 11px; }
                th { background: #17365D; color: white; padding: 8px; text-align: right; }
                td { border: 1px solid #ddd; padding: 6px 8px; }
                tr:nth-child(even) { background: #f9f9f9; }
              </style>
            </head>
            <body>
              <h1>شركة رنين — $title (إجمالي: $total)</h1>
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>رقم الزيارة</th>
                    <th>الفرع</th>
                    <th>المعيار</th>
                    <th>الخطورة</th>
                    <th>الحالة</th>
                    <th>الملاحظة</th>
                    <th>الإجراء التصحيحي</th>
                    <th>التاريخ</th>
                  </tr>
                </thead>
                <tbody>
                  $rows
                </tbody>
              </table>
            </body>
            </html>
            """.trimIndent()

            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    if (printManager != null) {
                        val printAdapter = webView.createPrintDocumentAdapter("تقرير_الملاحظات")
                        val jobName = "تقرير ملاحظات رنين - $title"
                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build()
                        printManager.print(jobName, printAdapter, printAttributes)
                    }
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=utf-8", "UTF-8", null)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حدث خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun printOrExportPdf(context: Context, visit: InspectionVisit, items: List<InspectionItemEvaluation>) {
        printOrSaveVisitPdf(context, visit, items)
    }

    fun shareReportSummary(context: Context, visit: InspectionVisit, items: List<InspectionItemEvaluation>) {
        val nonCompliant = items.filter { it.state == "غير مطابق" }
        val sb = StringBuilder()
        sb.appendLine("📋 *تقرير تقييم الجودة - شركة رنين*")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("• رقم الزيارة: ${visit.visitNumber}")
        sb.appendLine("• التاريخ: ${visit.dateFormatted}")
        sb.appendLine("• المفتش: ${visit.inspectorName}")
        sb.appendLine("• المشروع / الفرع: ${visit.projectName}")
        if (visit.location.isNotBlank()) sb.appendLine("• الموقع: ${visit.location}")
        sb.appendLine("• نسبة الجودة: ${visit.scorePercentage}%")
        sb.appendLine("• مطابق: ${visit.compliantCount} | غير مطابق: ${visit.nonCompliantCount}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━")

        if (nonCompliant.isNotEmpty()) {
            sb.appendLine("\n🚨 *الملاحظات والمخالفات المرصودة (${nonCompliant.size}):*")
            nonCompliant.forEachIndexed { i, item ->
                sb.appendLine("\n${i + 1}. [${item.code}] ${item.name}")
                sb.appendLine("   - القسم: ${item.section}")
                sb.appendLine("   - درجة الخطورة: ${item.severity}")
                if (item.note.isNotBlank()) sb.appendLine("   - الملاحظة: ${item.note}")
                if (item.action.isNotBlank()) sb.appendLine("   - الإجراء المطلوب: ${item.action}")
            }
        } else {
            sb.appendLine("\n✅ ممتاز! لا توجد مخالفات مسجلة في هذه الزيارة.")
        }

        if (visit.notes.isNotBlank()) {
            sb.appendLine("\n📝 *ملاحظات عامة:*")
            sb.appendLine(visit.notes)
        }

        sb.appendLine("\n_نظام إدارة الجودة - شركة رنين_")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة ملخص التقرير")
        context.startActivity(shareIntent)
    }
}
