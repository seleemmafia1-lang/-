package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.backup.DatabaseBackupManager
import com.example.data.db.RaneenDatabase
import com.example.data.model.UserEntity
import com.example.data.repository.AuditRepository
import com.example.data.sync.FirestoreSyncManager
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuditViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ManagementViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = RaneenDatabase.getDatabase(applicationContext)
        val repository = AuditRepository(database.appDao())
        val syncManager = FirestoreSyncManager(database.appDao())
        val backupManager = DatabaseBackupManager(applicationContext, database.appDao())
        val authViewModel = AuthViewModel(repository)
        val auditViewModel = AuditViewModel(repository)
        val managementViewModel = ManagementViewModel(repository, syncManager, backupManager)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authUiState by authViewModel.uiState.collectAsState()

                    Crossfade(
                        targetState = authUiState.currentUser,
                        label = "AuthCrossfade"
                    ) { user ->
                        if (user != null) {
                            MainAppScaffold(
                                currentUser = user,
                                authViewModel = authViewModel,
                                auditViewModel = auditViewModel,
                                managementViewModel = managementViewModel
                            )
                        } else {
                            LoginScreen(
                                uiState = authUiState,
                                onLogin = { username, password ->
                                    authViewModel.login(username, password)
                                },
                                onClearError = {
                                    authViewModel.clearError()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
