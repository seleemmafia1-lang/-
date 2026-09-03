package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.ui.graphics.vector.ImageVector

enum class QualityNavTab(val label: String, val icon: ImageVector) {
    DASHBOARD("الرئيسية", Icons.Filled.Dashboard),
    INSPECTION("تقييم جديد", Icons.Filled.Assignment),
    VISITS("سجل الزيارات", Icons.Filled.ListAlt),
    REPORTS("الملاحظات", Icons.Filled.WarningAmber),
    RULES("المعايير", Icons.Filled.Rule)
}
