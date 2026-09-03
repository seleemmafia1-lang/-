package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.QualityNavTab
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.viewmodel.QualityViewModel
import com.example.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    currentUser: UserProfile,
    qualityViewModel: QualityViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(QualityNavTab.DASHBOARD) }

    val availableTabs = remember(currentUser.role) {
        when (currentUser.role) {
            UserRole.INSPECTOR -> listOf(
                QualityNavTab.DASHBOARD, QualityNavTab.INSPECTION,
                QualityNavTab.VISITS, QualityNavTab.REPORTS
            )
            UserRole.QUALITY_MANAGER -> listOf(
                QualityNavTab.DASHBOARD, QualityNavTab.REPORTS,
                QualityNavTab.VISITS, QualityNavTab.RULES, QualityNavTab.INSPECTION
            )
            UserRole.BRANCH_MANAGER -> listOf(
                QualityNavTab.DASHBOARD, QualityNavTab.REPORTS, QualityNavTab.VISITS
            )
            UserRole.ADMIN -> QualityNavTab.values().toList()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(selectedTab.label) },
                actions = {
                    IconButton(onClick = { userViewModel.logout() }) {
                        Icon(Icons.Filled.Logout, contentDescription = "تسجيل الخروج")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                availableTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                QualityNavTab.DASHBOARD -> DashboardScreen(
                    viewModel = qualityViewModel,
                    currentUser = currentUser,
                    onNavigateToTab = { selectedTab = it }
                )
                QualityNavTab.INSPECTION -> NewInspectionScreen(viewModel = qualityViewModel)
                QualityNavTab.VISITS -> VisitsHistoryScreen(viewModel = qualityViewModel)
                QualityNavTab.REPORTS -> ReportsScreen(viewModel = qualityViewModel)
                QualityNavTab.RULES -> RulesManagementScreen(viewModel = qualityViewModel)
            }
        }
    }
}
