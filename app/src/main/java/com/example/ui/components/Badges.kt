package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, textCol, borderCol) = when (status) {
        "مغلقة" -> Triple(ColorOkBg, ColorOkText, ColorOkBorder)
        "قيد المعالجة" -> Triple(ColorInfoBg, ColorInfoText, ColorInfoBorder)
        else -> Triple(ColorWarningBg, ColorWarningText, ColorWarningBorder) // "جديدة"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = textCol,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SeverityBadge(
    severity: String,
    modifier: Modifier = Modifier
) {
    val (bg, textCol, borderCol) = when (severity) {
        "عالية" -> Triple(ColorBadBg, ColorBadText, ColorBadBorder)
        "متوسطة" -> Triple(ColorWarningBg, ColorWarningText, ColorWarningBorder)
        else -> Triple(ColorOkBg, ColorOkText, ColorOkBorder) // "منخفضة"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = severity,
            color = textCol,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EvaluationStateBadge(
    state: String,
    modifier: Modifier = Modifier
) {
    val (bg, textCol, borderCol) = when (state) {
        "مطابق" -> Triple(ColorOkBg, ColorOkText, ColorOkBorder)
        "غير مطابق" -> Triple(ColorBadBg, ColorBadText, ColorBadBorder)
        "لا ينطبق" -> Triple(ColorNaBg, ColorNaText, ColorNaBorder)
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.outline)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (state.isBlank()) "غير مقيّم" else state,
            color = textCol,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

