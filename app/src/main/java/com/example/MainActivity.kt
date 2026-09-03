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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QualityViewModel
import com.example.ui.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userViewModel: UserViewModel = viewModel()
                    val qualityViewModel: QualityViewModel = viewModel()

                    val authUiState by userViewModel.uiState.collectAsState()

                    Crossfade(
                        targetState = authUiState.currentUser,
                        label = "AuthCrossfade"
                    ) { user ->
                        if (user != null) {
                            MainAppScaffold(
                                currentUser = user,
                                qualityViewModel = qualityViewModel,
                                userViewModel = userViewModel
                            )
                        } else {
                            LoginScreen(
                                uiState = authUiState,
                                onLogin = { username, password ->
                                    userViewModel.login(username, password)
                                },
                                onClearError = {
                                    userViewModel.clearError()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
