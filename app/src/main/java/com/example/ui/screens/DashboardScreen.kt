package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.QualityNavTab
import com.example.data.model.InspectionVisit
import com.example.data.model.QualityProject
import com.example.data.model.QualityReportItem
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.QualityViewModel
import java.text.SimpleDateFormat
import java.util.*

fun getRoleIcon(role: UserRole): ImageVector = when (role) {
    UserRole.INSPECTOR -> Icons.Filled.Engineering
    UserRole.QUALITY_MANAGER -> Icons.Filled.AssignmentTurnedIn
    UserRole.BRANCH_MANAGER -> Icons.Filled.Business
    UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
}

@Composable
fun DashboardScreen(
    viewModel: QualityViewModel,
    currentUser: UserProfile,
    onNavigateToTab: (QualityNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val visits by viewModel.visits.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val rules by viewModel.rules.collectAsState()
    val projects by viewModel.projects.collectAsState()

    // Calculated Statistics
    val avgScore = remember(visits) {
        if (visits.isEmpty()) 0 else visits.map { it.scorePercentage }.average().toInt()
    }
    val newIssuesCount = remember(reports) { reports.count { it.status == "جديدة" } }
    val resolvedIssuesCount = remember(reports) { reports.count { it.status == "مغلقة" || it.status == "تم الحل" } }
    val totalVisitsCount = visits.size
    val totalRulesCount = rules.size

    val recentVisits = remember(visits) { visits.take(3) }
    val pendingReports = remember(reports) { reports.filter { it.status != "مغلقة" && it.status != "تم الحل" }.take(3) }

    val currentDateStr = remember {
        SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar")).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // 1. Hero Welcome & Role Card
        item {
            DashboardWelcomeHeader(
                user = currentUser,
                currentDate = currentDateStr,
                avgScore = avgScore,
                totalVisits = totalVisitsCount,
                pendingIssues = newIssuesCount
            )
        }

        // 2. Quick Role-Specific Options & Actions
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الخيارات والخدمات المتاحة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaneenNavy
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RaneenNavy.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "دور: ${currentUser.role.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RaneenNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                RoleSpecificActionGrid(
                    role = currentUser.role,
                    onNavigateToTab = onNavigateToTab,
                    onStartNewInspection = {
                        viewModel.initNewInspection()
                        onNavigateToTab(QualityNavTab.INSPECTION)
                    }
                )
            }
        }

        // 3. Stats & Overview Cards
        item {
            DashboardKPISection(
                role = currentUser.role,
                avgScore = avgScore,
                totalVisits = totalVisitsCount,
                newIssuesCount = newIssuesCount,
                resolvedIssuesCount = resolvedIssuesCount,
                totalRules = totalRulesCount,
                projectsCount = projects.size
            )
        }

        // 4. Role-Specific Dynamic Content Section
        when (currentUser.role) {
            UserRole.INSPECTOR -> {
                item {
                    SectionHeader(
                        title = "أحدث الزيارات الميدانية المسجلة",
                        actionTitle = "عرض السجل الكامل",
                        onActionClick = { onNavigateToTab(QualityNavTab.VISITS) }
                    )
                }
                if (recentVisits.isEmpty()) {
                    item { EmptyDashboardState(message = "لا توجد زيارات تفتيش مسجلة حتى الآن. اضغط على 'بدء تقييم جودة ميداني' للبدء.") }
                } else {
                    items(recentVisits) { visit ->
                        RecentVisitCard(visit = visit, onClick = { onNavigateToTab(QualityNavTab.VISITS) })
                    }
                }
            }

            UserRole.QUALITY_MANAGER -> {
                item {
                    SectionHeader(
                        title = "ملاحظات عدم المطابقة قيد المتابعة",
                        actionTitle = "عرض كافة التقارير",
                        onActionClick = { onNavigateToTab(QualityNavTab.REPORTS) }
                    )
                }
                if (pendingReports.isEmpty()) {
                    item { EmptyDashboardState(message = "ممتاز! لا توجد ملاحظات أو مخالفات معلقة حالياً في الفروع.") }
                } else {
                    items(pendingReports) { report ->
                        PendingReportItemCard(report = report, onClick = { onNavigateToTab(QualityNavTab.REPORTS) })
                    }
                }
            }

            UserRole.BRANCH_MANAGER -> {
                item {
                    SectionHeader(
                        title = "حالة المطابقة وإجراءات الفرع التصحيحية",
                        actionTitle = "متابعة الملاحظات",
                        onActionClick = { onNavigateToTab(QualityNavTab.REPORTS) }
                    )
                }
                if (pendingReports.isEmpty()) {
                    item { EmptyDashboardState(message = "جميع بنود الجودة مطابقة بنجاح في الفرع.") }
                } else {
                    items(pendingReports) { report ->
                        PendingReportItemCard(report = report, onClick = { onNavigateToTab(QualityNavTab.REPORTS) })
                    }
                }
            }

            UserRole.ADMIN -> {
                item {
                    SectionHeader(
                        title = "فروع ومشاريع رنين المسجلة",
                        actionTitle = "إدارة المعايير",
                        onActionClick = { onNavigateToTab(QualityNavTab.RULES) }
                    )
                }
                items(projects.take(3)) { project ->
                    ProjectOverviewCard(project = project)
                }
            }
        }
    }
}

@Composable
private fun DashboardWelcomeHeader(
    user: UserProfile,
    currentDate: String,
    avgScore: Int,
    totalVisits: Int,
    pendingIssues: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(RaneenNavyDark, RaneenNavy, RaneenNavyLight)
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.5.dp, RaneenOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getRoleIcon(user.role),
                            contentDescription = user.role.title,
                            tint = RaneenOrange,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "مرحباً، ${user.fullName}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = RaneenOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = user.branchName.ifBlank { "الإدارة العامة" },
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = user.role.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = currentDate,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick High-Level Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniStatItem(
                    label = "متوسط الجودة",
                    value = if (totalVisits > 0) "$avgScore%" else "—",
                    color = when {
                        avgScore >= 85 -> ColorOkText
                        avgScore >= 70 -> ColorWarningText
                        else -> ColorBadText
                    }
                )
                VerticalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.height(28.dp))
                MiniStatItem(
                    label = "إجمالي التقييمات",
                    value = totalVisits.toString(),
                    color = Color.White
                )
                VerticalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.height(28.dp))
                MiniStatItem(
                    label = "ملاحظات جديدة",
                    value = pendingIssues.toString(),
                    color = if (pendingIssues > 0) ColorBadText else ColorOkText
                )
            }
        }
    }
}

@Composable
private fun MiniStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun RoleSpecificActionGrid(
    role: UserRole,
    onNavigateToTab: (QualityNavTab) -> Unit,
    onStartNewInspection: () -> Unit
) {
    when (role) {
        UserRole.INSPECTOR -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryHeroActionCard(
                    title = "بدء تقييم جودة ميداني جديد",
                    subtitle = "تدقيق وفحص بنود النظافة، التسعير، العرض وسلامة المنتجات",
                    icon = Icons.Filled.AddCircle,
                    badgeText = "المهمة الأساسية",
                    onClick = onStartNewInspection
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardOptionCard(
                        title = "سجل زياراتي",
                        subtitle = "متابعة التقييمات السابقة",
                        icon = Icons.Filled.History,
                        iconBg = RaneenNavy,
                        onClick = { onNavigateToTab(QualityNavTab.VISITS) },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardOptionCard(
                        title = "ملاحظات الفروع",
                        subtitle = "المخالفات غير المطابقة",
                        icon = Icons.Filled.AssignmentLate,
                        iconBg = RaneenOrange,
                        onClick = { onNavigateToTab(QualityNavTab.REPORTS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        UserRole.QUALITY_MANAGER -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardOptionCard(
                        title = "تقارير عدم المطابقة",
                        subtitle = "فحص الإجراءات التصحيحية",
                        icon = Icons.Filled.AssignmentLate,
                        iconBg = RaneenOrange,
                        onClick = { onNavigateToTab(QualityNavTab.REPORTS) },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardOptionCard(
                        title = "سجل الزيارات والتدقيق",
                        subtitle = "مراجعة تقارير المفتشين",
                        icon = Icons.Filled.FactCheck,
                        iconBg = RaneenNavy,
                        onClick = { onNavigateToTab(QualityNavTab.VISITS) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardOptionCard(
                        title = "معايير وضوابط الجودة",
                        subtitle = "تخصيص بنود الفحص الـ 25",
                        icon = Icons.Filled.Tune,
                        iconBg = Color(0xFF2E7D32),
                        onClick = { onNavigateToTab(QualityNavTab.RULES) },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardOptionCard(
                        title = "إجراء تقييم تدقيقي",
                        subtitle = "زيارة جودة مفاجئة للفرع",
                        icon = Icons.Filled.EditNote,
                        iconBg = Color(0xFF6A1B9A),
                        onClick = onStartNewInspection,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        UserRole.BRANCH_MANAGER -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryHeroActionCard(
                    title = "عرض الملاحظات والإجراءات التصحيحية",
                    subtitle = "متابعة معالجة المخالفات المسجلة من فريق الجودة",
                    icon = Icons.Filled.AssignmentTurnedIn,
                    badgeText = "مطلوب متابعته",
                    onClick = { onNavigateToTab(QualityNavTab.REPORTS) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardOptionCard(
                        title = "سجل تقييمات الفرع",
                        subtitle = "سجل الزيارات والدرجات",
                        icon = Icons.Filled.History,
                        iconBg = RaneenNavy,
                        onClick = { onNavigateToTab(QualityNavTab.VISITS) },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardOptionCard(
                        title = "تقارير عدم المطابقة",
                        subtitle = "متابعة بنود المعالجة",
                        icon = Icons.Filled.AssignmentLate,
                        iconBg = RaneenOrange,
                        onClick = { onNavigateToTab(QualityNavTab.REPORTS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        UserRole.ADMIN -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardOptionCard(
                        title = "معايير وقواعد الجودة",
                        subtitle = "إدارة البنود والأقسام",
                        icon = Icons.Filled.Tune,
                        iconBg = RaneenNavy,
                        onClick = { onNavigateToTab(QualityNavTab.RULES) },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardOptionCard(
                        title = "كافة الملاحظات والتقارير",
                        subtitle = "سجل المخالفات الكلي",
                        icon = Icons.Filled.AssignmentLate,
                        iconBg = RaneenOrange,
                        onClick = { onNavigateToTab(QualityNavTab.REPORTS) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardOptionCard(
                        title = "سجل الزيارات الميدانية",
                        subtitle = "كافة زيارات الفروع",
                        icon = Icons.Filled.History,
                        iconBg = Color(0xFF2E7D32),
                        onClick = { onNavigateToTab(QualityNavTab.VISITS) },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardOptionCard(
                        title = "بدء فحص فني جديد",
                        subtitle = "إنشاء نموذج تقييم",
                        icon = Icons.Filled.EditNote,
                        iconBg = Color(0xFF6A1B9A),
                        onClick = onStartNewInspection,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryHeroActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("hero_action_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, RaneenOrange.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(RaneenOrange, Color(0xFFFF9800))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaneenNavy
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RaneenOrange.copy(alpha = 0.12f)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaneenOrange,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RaneenNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DashboardKPISection(
    role: UserRole,
    avgScore: Int,
    totalVisits: Int,
    newIssuesCount: Int,
    resolvedIssuesCount: Int,
    totalRules: Int,
    projectsCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "مؤشرات الأداء والجودة (KPIs)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = RaneenNavy
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "نسبة الامتثال",
                value = if (totalVisits > 0) "$avgScore%" else "—",
                caption = "المتوسط التراكمي",
                color = if (avgScore >= 85) ColorOkText else ColorWarningText,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "الزيارات المنجزة",
                value = "$totalVisits",
                caption = "فحص ميداني",
                color = RaneenNavy,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "مخالفات مفتوحة",
                value = "$newIssuesCount",
                caption = "تحتاج معالجة",
                color = if (newIssuesCount > 0) ColorBadText else ColorOkText,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "تمت معالجتها",
                value = "$resolvedIssuesCount",
                caption = "بند تم حله",
                color = ColorOkText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    caption: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = RaneenNavy,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                text = caption,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionTitle: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = RaneenNavy
        )

        TextButton(
            onClick = onActionClick,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = actionTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RaneenOrange
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = RaneenOrange,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun RecentVisitCard(
    visit: InspectionVisit,
    onClick: () -> Unit
) {
    val (scoreColor, scoreBg) = when {
        visit.scorePercentage >= 85 -> Pair(ColorOkText, ColorOkBg)
        visit.scorePercentage >= 65 -> Pair(ColorWarningText, ColorWarningBg)
        else -> Pair(ColorBadText, ColorBadBg)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(scoreBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${visit.scorePercentage}%",
                    color = scoreColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = visit.projectName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaneenNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "رقم الزيارة: ${visit.visitNumber} • ${visit.dateFormatted}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PendingReportItemCard(
    report: QualityReportItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ColorBadBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = ColorBadText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.ruleName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaneenNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${report.projectName} • ${report.ruleSection}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            StatusBadge(status = report.status)
        }
    }
}

@Composable
private fun ProjectOverviewCard(
    project: QualityProject
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RaneenNavy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Storefront,
                    contentDescription = null,
                    tint = RaneenNavy,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaneenNavy
                )
                Text(
                    text = "كود الفرع: ${project.branchCode} • ${project.city} • المدير: ${project.managerName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ColorOkBg
            ) {
                Text(
                    text = "المستهدف: ${project.targetScore}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorOkText,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyDashboardState(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Text(
            text = message,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
