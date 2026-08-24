package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InspectionVisit
import com.example.ui.components.VisitDetailDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.QualityViewModel
import com.example.util.JsonUtil
import com.example.util.ReportExporter

@Composable
fun VisitsHistoryScreen(
    viewModel: QualityViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visits by viewModel.visits.collectAsState()

    var selectedVisit by remember { mutableStateOf<InspectionVisit?>(null) }
    var visitToDelete by remember { mutableStateOf<InspectionVisit?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "سجل الزيارات والتقارير الميدانية",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = RaneenNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "إجمالي الزيارات المسجلة: ${visits.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = RaneenOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        if (visits.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentLate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لم يتم حفظ أي زيارة تفتيش حتى الآن.",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "انتقل إلى تبويب «تقييم جديد» لبدء أول تقييم ميداني.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            items(visits, key = { it.id }) { visit ->
                VisitCardItem(
                    visit = visit,
                    onClick = { selectedVisit = visit },
                    onExportPdf = {
                        val items = JsonUtil.deserializeEvaluationList(visit.itemsJson)
                        ReportExporter.exportVisitPdfAndShare(context, visit, items)
                    },
                    onPrintPdf = {
                        val items = JsonUtil.deserializeEvaluationList(visit.itemsJson)
                        ReportExporter.printOrSaveVisitPdf(context, visit, items)
                    },
                    onShareText = {
                        val items = JsonUtil.deserializeEvaluationList(visit.itemsJson)
                        ReportExporter.shareReportSummary(context, visit, items)
                    },
                    onDelete = { visitToDelete = visit }
                )
            }
        }
    }

    // Detail Dialog
    selectedVisit?.let { v ->
        VisitDetailDialog(
            visit = v,
            onDismiss = { selectedVisit = null }
        )
    }

    // Delete Confirm Dialog
    visitToDelete?.let { v ->
        AlertDialog(
            onDismissRequest = { visitToDelete = null },
            title = { Text("حذف تقرير الزيارة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف زيارة التفتيش (${v.visitNumber}) لمشروع ${v.projectName}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVisit(v)
                        visitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { visitToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun VisitCardItem(
    visit: InspectionVisit,
    onClick: () -> Unit,
    onExportPdf: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scoreColor = when {
        visit.scorePercentage >= 85 -> ColorOkText
        visit.scorePercentage >= 65 -> ColorWarningText
        else -> ColorBadText
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("visit_card_${visit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = visit.visitNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = visit.dateFormatted,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Project & Inspector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visit.projectName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaneenNavy
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "المفتش: ${visit.inspectorName}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (visit.location.isNotBlank()) {
                        Text(
                            text = "الموقع: ${visit.location}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Score Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scoreColor.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor.copy(alpha = 0.25f)),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${visit.scorePercentage}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = scoreColor
                        )
                        Text(
                            text = "النتيجة",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Chip Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ColorOkBg.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorOkBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "مطابق: ${visit.compliantCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorOkText,
                        modifier = Modifier.padding(vertical = 5.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ColorBadBg.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorBadBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "غير مطابق: ${visit.nonCompliantCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorBadText,
                        modifier = Modifier.padding(vertical = 5.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "المجموع: ${visit.totalEvaluated}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(vertical = 5.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = RaneenNavy,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onPrintPdf,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة / حفظ", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    IconButton(
                        onClick = onShareText,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "مشاركة نصية",
                            tint = RaneenOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف الزيارة",
                        tint = ColorBadText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
