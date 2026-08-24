package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.QualityReportItem
import com.example.ui.components.PhotoPreviewDialog
import com.example.ui.components.SeverityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.QualityViewModel
import com.example.util.ReportExporter

@Composable
fun ReportsScreen(
    viewModel: QualityViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reports by viewModel.reports.collectAsState()
    val searchQuery by viewModel.reportSearchQuery.collectAsState()
    val statusFilter by viewModel.reportStatusFilter.collectAsState()
    val severityFilter by viewModel.reportSeverityFilter.collectAsState()
    val lastSavedVisit by viewModel.lastSavedVisit.collectAsState()
    val visits by viewModel.visits.collectAsState()

    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var reportToDelete by remember { mutableStateOf<QualityReportItem?>(null) }

    // Filter reports
    val filteredReports = remember(reports, searchQuery, statusFilter, severityFilter) {
        reports.filter { r ->
            val matchesQuery = searchQuery.isBlank() ||
                    r.projectName.contains(searchQuery, ignoreCase = true) ||
                    r.ruleName.contains(searchQuery, ignoreCase = true) ||
                    r.ruleCode.contains(searchQuery, ignoreCase = true) ||
                    r.note.contains(searchQuery, ignoreCase = true) ||
                    r.visitNumber.contains(searchQuery, ignoreCase = true) ||
                    r.location.contains(searchQuery, ignoreCase = true)

            val matchesStatus = statusFilter == null || statusFilter == "الكل" || r.status == statusFilter
            val matchesSeverity = severityFilter == null || severityFilter == "الكل" || r.severity == severityFilter

            matchesQuery && matchesStatus && matchesSeverity
        }
    }

    val totalCount = reports.size
    val newCount = reports.count { it.status == "جديدة" }
    val inProgressCount = reports.count { it.status == "قيد المعالجة" }
    val closedCount = reports.count { it.status == "مغلقة" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary & Metrics Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل الملاحظات والمخالفات",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = RaneenNavy
                        )

                        var showExportMenu by remember { mutableStateOf(false) }

                        Box {
                            FilledTonalButton(
                                onClick = { showExportMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = RaneenNavy,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "تصدير PDF ▾", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = showExportMenu,
                                onDismissRequest = { showExportMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("📄 مشاركة تقرير الملاحظات كملف PDF", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = RaneenNavy) },
                                    onClick = {
                                        showExportMenu = false
                                        val title = if (statusFilter != null && statusFilter != "الكل") "تقرير الملاحظات ($statusFilter)" else "تقرير الملاحظات والمخالفات الميدانية"
                                        ReportExporter.exportReportsListPdfAndShare(context, filteredReports, title)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🖨️ طباعة / حفظ كـ PDF للملاحظات", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, tint = RaneenOrange) },
                                    onClick = {
                                        showExportMenu = false
                                        val title = if (statusFilter != null && statusFilter != "الكل") "تقرير الملاحظات ($statusFilter)" else "تقرير الملاحظات والمخالفات الميدانية"
                                        ReportExporter.printOrSaveReportsListPdf(context, filteredReports, title)
                                    }
                                )
                                if (visits.isNotEmpty()) {
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("📋 مشاركة PDF لآخر زيارة تقييم", fontSize = 13.sp) },
                                        leadingIcon = { Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                        onClick = {
                                            showExportMenu = false
                                            val latest = visits.first()
                                            val items = com.example.util.JsonUtil.deserializeEvaluationList(latest.itemsJson)
                                            ReportExporter.exportVisitPdfAndShare(context, latest, items)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(title = "الإجمالي", count = totalCount, color = RaneenNavy, modifier = Modifier.weight(1f))
                        StatCard(title = "جديدة", count = newCount, color = ColorWarningText, modifier = Modifier.weight(1f))
                        StatCard(title = "معالجة", count = inProgressCount, color = ColorInfoText, modifier = Modifier.weight(1f))
                        StatCard(title = "مغلقة", count = closedCount, color = ColorOkText, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Search and Filters
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateReportSearch(it) },
                        placeholder = { Text("🔎 بحث في الملاحظات، الفروع، الأكواد...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.updateReportSearch("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Filters
                    Text(text = "حسب الحالة:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf("الكل", "جديدة", "قيد المعالجة", "مغلقة")) { status ->
                            FilterChip(
                                selected = (statusFilter == status || (statusFilter == null && status == "الكل")),
                                onClick = { viewModel.updateReportStatusFilter(if (status == "الكل") null else status) },
                                label = { Text(status, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Severity Filters
                    Text(text = "حسب درجة الخطورة:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf("الكل", "عالية", "متوسطة", "منخفضة")) { sev ->
                            FilterChip(
                                selected = (severityFilter == sev || (severityFilter == null && sev == "الكل")),
                                onClick = { viewModel.updateReportSeverityFilter(if (sev == "الكل") null else sev) },
                                label = { Text(sev, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Empty state
        if (filteredReports.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = ColorOkText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (reports.isEmpty()) "لا توجد ملاحظات أو مخالفات مسجلة حالياً." else "لا توجد نتائج تطابق معايير البحث والفلترة.",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Reports List
            items(filteredReports, key = { it.id }) { report ->
                ReportCardItem(
                    report = report,
                    onStatusClick = { viewModel.cycleReportStatus(report) },
                    onPhotoClick = { selectedPhotoUri = report.photoUri },
                    onDeleteClick = { reportToDelete = report }
                )
            }
        }
    }

    // Photo Dialog
    selectedPhotoUri?.let { uri ->
        PhotoPreviewDialog(
            photoUri = uri,
            onDismiss = { selectedPhotoUri = null }
        )
    }

    // Delete Confirm Dialog
    reportToDelete?.let { rep ->
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            title = { Text("حذف الملاحظة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف مخالفة [${rep.ruleCode}] ${rep.ruleName}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReport(rep)
                        reportToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun ReportCardItem(
    report: QualityReportItem,
    onStatusClick: () -> Unit,
    onPhotoClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when {
        report.status == "مغلقة" -> ColorOkText
        report.severity == "عالية" -> ColorBadText
        report.severity == "متوسطة" -> ColorWarningText
        else -> Color(0xFF64748B)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("report_item_${report.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Start Accent Stripe (Sleek indicator)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(indicatorColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                // Top Row: Visit No & Date & Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = report.visitNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = report.dateFormatted, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SeverityBadge(severity = report.severity)
                        StatusBadge(status = report.status)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Project & Location
                Text(
                    text = "${report.projectName}${if (report.location.isNotBlank()) " — " + report.location else ""}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaneenNavy
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rule Info
                Text(
                    text = "[${report.ruleCode}] ${report.ruleName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (report.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ColorBadBg.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBadBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "الملاحظة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorBadText)
                            Text(text = report.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (report.action.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ColorInfoBg.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorInfoBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "الإجراء التصحيحي:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorInfoText)
                            Text(text = report.action, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Photo Thumbnail if available
                if (report.photoUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onPhotoClick)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = report.photoUri),
                                contentDescription = "صورة المخالفة",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "عرض الصورة المرفقة 🔍", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RaneenNavy)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Action Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onStatusClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RaneenOrange
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "تغيير الحالة (${report.status})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف",
                            tint = ColorBadText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
