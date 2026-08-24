package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.AppHeader
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NewInspectionScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.RulesManagementScreen
import com.example.ui.screens.VisitsHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QualityViewModel
import kotlinx.coroutines.flow.collectLatest

enum class QualityNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    DASHBOARD("الرئيسية", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_dashboard"),
    INSPECTION("تقييم جديد", Icons.Filled.EditNote, Icons.Outlined.EditNote, "tab_inspection"),
    REPORTS("الملاحظات", Icons.Filled.AssignmentLate, Icons.Outlined.AssignmentLate, "tab_reports"),
    VISITS("سجل الزيارات", Icons.Filled.History, Icons.Outlined.History, "tab_visits"),
    RULES("معايير الجودة", Icons.Filled.Tune, Icons.Outlined.Tune, "tab_rules")
}

class MainActivity : ComponentActivity() {

    private val viewModel: QualityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val currentUser by viewModel.currentUser.collectAsState()

                    AnimatedContent(
                        targetState = currentUser,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "AuthRootNavigation"
                    ) { user ->
                        if (user == null) {
                            LoginScreen(viewModel = viewModel)
                        } else {
                            QualityApp(viewModel = viewModel, user = user)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QualityApp(
    viewModel: QualityViewModel,
    user: UserProfile
) {
    val availableTabs = remember(user.role) {
        when (user.role) {
            UserRole.INSPECTOR -> listOf(QualityNavTab.DASHBOARD, QualityNavTab.INSPECTION, QualityNavTab.REPORTS, QualityNavTab.VISITS)
            UserRole.BRANCH_MANAGER -> listOf(QualityNavTab.DASHBOARD, QualityNavTab.REPORTS, QualityNavTab.VISITS)
            UserRole.QUALITY_MANAGER -> listOf(QualityNavTab.DASHBOARD, QualityNavTab.REPORTS, QualityNavTab.VISITS, QualityNavTab.INSPECTION, QualityNavTab.RULES)
            UserRole.ADMIN -> listOf(QualityNavTab.DASHBOARD, QualityNavTab.INSPECTION, QualityNavTab.REPORTS, QualityNavTab.VISITS, QualityNavTab.RULES)
        }
    }

    var selectedTab by remember(user.role) { mutableStateOf(availableTabs.first()) }
    val snackbarHostState = remember { SnackbarHostState() }

    val reports by viewModel.reports.collectAsState()
    val newIssuesCount = remember(reports) { reports.count { it.status == "جديدة" } }

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppHeader(
                currentUser = user,
                onLogout = { viewModel.logout() }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                availableTabs.forEach { tab ->
                    val isSelected = selectedTab == tab

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (tab == QualityNavTab.REPORTS && newIssuesCount > 0) {
                                        Badge {
                                            Text(text = newIssuesCount.toString(), fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContent"
            ) { targetTab ->
                when (targetTab) {
                    QualityNavTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        currentUser = user,
                        onNavigateToTab = { selectedTab = it }
                    )
                    QualityNavTab.INSPECTION -> NewInspectionScreen(viewModel = viewModel)
                    QualityNavTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                    QualityNavTab.VISITS -> VisitsHistoryScreen(viewModel = viewModel)
                    QualityNavTab.RULES -> RulesManagementScreen(viewModel = viewModel)
                }
            }
        }
    }
}

