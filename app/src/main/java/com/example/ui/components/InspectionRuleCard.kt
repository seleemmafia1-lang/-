package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
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
import com.example.data.model.InspectionItemEvaluation
import com.example.ui.theme.*

@Composable
fun InspectionRuleCard(
    item: InspectionItemEvaluation,
    onStateChange: (String) -> Unit,
    onSeverityChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onActionChange: (String) -> Unit,
    onPhotoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isNonCompliant = item.state == "غير مطابق"
    val context = LocalContext.current

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPhotoChange(it.toString()) }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rule_card_${item.code}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            when (item.state) {
                "مطابق" -> ColorOkBorder
                "غير مطابق" -> ColorBadBorder
                else -> MaterialTheme.colorScheme.outline
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RaneenNavy.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = item.code,
                        color = RaneenNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = item.section,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rule Title / Description
            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Evaluation Options Segmented Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OptionChip(
                    label = "مطابق",
                    selected = item.state == "مطابق",
                    selectedBg = ColorOkBg,
                    selectedTextColor = ColorOkText,
                    selectedBorder = ColorOkBorder,
                    onClick = { onStateChange("مطابق") },
                    modifier = Modifier.weight(1f)
                )
                OptionChip(
                    label = "غير مطابق",
                    selected = item.state == "غير مطابق",
                    selectedBg = ColorBadBg,
                    selectedTextColor = ColorBadText,
                    selectedBorder = ColorBadBorder,
                    onClick = { onStateChange("غير مطابق") },
                    modifier = Modifier.weight(1f)
                )
                OptionChip(
                    label = "لا ينطبق",
                    selected = item.state == "لا ينطبق",
                    selectedBg = ColorNaBg,
                    selectedTextColor = ColorNaText,
                    selectedBorder = ColorNaBorder,
                    onClick = { onStateChange("لا ينطبق") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Non-compliant extra details
            AnimatedVisibility(
                visible = isNonCompliant,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .background(
                            ColorBadBg.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(1.dp, ColorBadBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "تفاصيل المخالفة والإجراء التصحيحي",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorBadText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Severity Selector
                    Text(
                        text = "درجة الخطورة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("منخفضة", "متوسطة", "عالية").forEach { sev ->
                            val isSelected = item.severity == sev
                            val (sevColor, sevBg, sevBorder) = when (sev) {
                                "عالية" -> Triple(ColorBadText, ColorBadBg, ColorBadBorder)
                                "متوسطة" -> Triple(ColorWarningText, ColorWarningBg, ColorWarningBorder)
                                else -> Triple(ColorOkText, ColorOkBg, ColorOkBorder)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) sevColor else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) sevColor else sevBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSeverityChange(sev) }
                            ) {
                                Text(
                                    text = sev,
                                    color = if (isSelected) Color.White else sevColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Violation Note
                    OutlinedTextField(
                        value = item.note,
                        onValueChange = onNoteChange,
                        label = { Text("الملاحظة (وصف المخالفة)") },
                        placeholder = { Text("مثال: وجود أتربة أو تسعير غير صحيح...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_${item.code}"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Corrective Action
                    OutlinedTextField(
                        value = item.action,
                        onValueChange = onActionChange,
                        label = { Text("الإجراء التصحيحي المطلوب") },
                        placeholder = { Text("مثال: تنظيف الرف فوراً وتعديل الملصق...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("action_input_${item.code}"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Photo Attachment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("attach_photo_${item.code}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (item.photoUri.isBlank()) "إرفاق صورة" else "تغيير الصورة", fontSize = 12.sp)
                        }

                        if (item.photoUri.isNotBlank()) {
                            IconButton(onClick = { onPhotoChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف الصورة",
                                    tint = ColorBadText
                                )
                            }
                        }
                    }

                    // Photo Thumbnail Preview
                    if (item.photoUri.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = item.photoUri),
                                contentDescription = "صورة المخالفة",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionChip(
    label: String,
    selected: Boolean,
    selectedBg: Color,
    selectedTextColor: Color,
    selectedBorder: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) selectedBg else Color.White,
        border = BorderStroke(
            1.dp,
            if (selected) selectedBorder else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = selectedTextColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = if (selected) selectedTextColor else MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
