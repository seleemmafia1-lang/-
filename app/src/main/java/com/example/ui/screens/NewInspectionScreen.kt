package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
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
import com.example.data.model.QualityRule
import com.example.ui.components.InspectionRuleCard
import com.example.ui.components.ScoreCard
import com.example.ui.theme.ColorOkText
import com.example.ui.theme.RaneenGold
import com.example.ui.theme.RaneenNavy
import com.example.ui.viewmodel.QualityViewModel
import com.example.util.ReportExporter

@Composable
fun NewInspectionScreen(
    viewModel: QualityViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val rules by viewModel.rules.collectAsState()
    val lastSavedVisit by viewModel.lastSavedVisit.collectAsState()

    var showConfirmSaveDialog by remember { mutableStateOf(false) }
    var savedSuccessVisit by remember { mutableStateOf<Pair<com.example.data.model.InspectionVisit, List<com.example.data.model.InspectionItemEvaluation>>?>(null) }

    val sections = remember(rules) {
        rules.map { it.section }.distinct()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visit Metadata Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "بيانات التقييم الميداني",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaneenNavy
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.visitNumber,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("رقم الزيارة") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("visit_number_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = formState.visitDate,
                            onValueChange = { viewModel.setVisitDate(it) },
                            label = { Text("التاريخ / الوقت") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("visit_date_field"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = formState.inspectorName,
                        onValueChange = { viewModel.setInspectorName(it) },
                        label = { Text("اسم المفتش *") },
                        placeholder = { Text("مثال: م. أحمد عبد الله") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inspector_name_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = formState.projectName,
                        onValueChange = { viewModel.setProjectName(it) },
                        label = { Text("اسم المشروع / الفرع *") },
                        placeholder = { Text("مثال: فرع المهندسين - رئيسي") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = formState.location,
                        onValueChange = { viewModel.setLocation(it) },
                        label = { Text("الموقع التفصيلي") },
                        placeholder = { Text("المبنى / الدور / المنطقة / الممر") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("location_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = formState.generalNotes,
                        onValueChange = { viewModel.setGeneralNotes(it) },
                        label = { Text("ملاحظات عامة") },
                        placeholder = { Text("ملاحظات إضافية حول الزيارة...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("general_notes_field"),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )
                }
            }
        }

        // Realtime Score Card
        item {
            ScoreCard(
                scorePercentage = formState.scorePercentage,
                totalEvaluated = formState.totalEvaluated,
                compliantCount = formState.compliantCount,
                nonCompliantCount = formState.nonCompliantCount,
                branchName = if (formState.projectName.isNotBlank()) formState.projectName else "فرع المهندسين - رئيسي"
            )
        }

        // Quality Rules Section Header
        item {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Text(
                    text = "معايير التقييم والجودة (${rules.size} معيار)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaneenNavy
                )
                Text(
                    text = "قيّم كل معيار، وعند اختيار «غير مطابق» يمكنك تسجيل الملاحظة وإرفاق صورة فورية.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Render Rules grouped by section
        sections.forEach { sectionName ->
            val sectionRules = rules.filter { it.section == sectionName }

            item(key = "header_$sectionName") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF4FA),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE3EA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sectionName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RaneenNavy
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${sectionRules.size} معايير",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            items(sectionRules, key = { it.code }) { rule ->
                val eval = formState.evaluations[rule.code]
                if (eval != null) {
                    InspectionRuleCard(
                        item = eval,
                        onStateChange = { viewModel.setItemState(rule.code, it) },
                        onSeverityChange = { viewModel.setItemSeverity(rule.code, it) },
                        onNoteChange = { viewModel.setItemNote(rule.code, it) },
                        onActionChange = { viewModel.setItemAction(rule.code, it) },
                        onPhotoChange = { viewModel.setItemPhoto(rule.code, it) }
                    )
                }
            }
        }

        // Bottom Action Buttons Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Save Button
                    Button(
                        onClick = {
                            if (formState.inspectorName.isBlank() || formState.projectName.isBlank()) {
                                Toast.makeText(context, "يرجى إدخال اسم المفتش واسم المشروع أولاً.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val unansweredCount = rules.size - formState.totalEvaluated
                            if (unansweredCount > 0) {
                                showConfirmSaveDialog = true
                            } else {
                                viewModel.saveInspection { visit, items ->
                                    savedSuccessVisit = Pair(visit, items)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_inspection_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "💾 حفظ تقرير التفتيش", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    // Reset / New Inspection Button
                    OutlinedButton(
                        onClick = { viewModel.initNewInspection() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_inspection_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "🔄 بدء تقييم جديد فارغ", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    // Export / Share PDF Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val visitPair = lastSavedVisit
                                if (visitPair != null) {
                                    ReportExporter.exportVisitPdfAndShare(context, visitPair.first, visitPair.second)
                                } else {
                                    Toast.makeText(context, "احفظ التقييم أولاً لتصدير ملف PDF.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_pdf_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = RaneenNavy,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "مشاركة PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val visitPair = lastSavedVisit
                                if (visitPair != null) {
                                    ReportExporter.printOrSaveVisitPdf(context, visitPair.first, visitPair.second)
                                } else {
                                    Toast.makeText(context, "احفظ التقييم أولاً للطباعة.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "طباعة / حفظ", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for partial evaluation save
    if (showConfirmSaveDialog) {
        val unansweredCount = rules.size - formState.totalEvaluated
        AlertDialog(
            onDismissRequest = { showConfirmSaveDialog = false },
            title = { Text("تأكيد الحفظ", fontWeight = FontWeight.Bold) },
            text = { Text("يوجد $unansweredCount بند لم يتم تقييمه بعد. هل ترغب في حفظ التقرير بالبنود المقيمة فقط؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSaveDialog = false
                        viewModel.saveInspection { visit, items ->
                            savedSuccessVisit = Pair(visit, items)
                        }
                    }
                ) {
                    Text("نعم، احفظ الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSaveDialog = false }) {
                    Text("إلغاء ومتابعة التقييم")
                }
            }
        )
    }

    // Success Dialog with Direct PDF Export
    savedSuccessVisit?.let { pair ->
        val visit = pair.first
        val items = pair.second
        AlertDialog(
            onDismissRequest = { savedSuccessVisit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✅ تم حفظ تقرير الجودة بنجاح", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("رقم الزيارة: ${visit.visitNumber}", fontWeight = FontWeight.Bold)
                    Text("المشروع / الفرع: ${visit.projectName}")
                    Text("نسبة الجودة والامتثال: ${visit.scorePercentage}%", color = ColorOkText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("هل تود تصدير ومشاركة التقرير بصيغة PDF الآن مع الإدارة أو الفرع؟", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ReportExporter.exportVisitPdfAndShare(context, visit, items)
                        savedSuccessVisit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RaneenNavy)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة ملف PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { savedSuccessVisit = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
