package com.example

import android.graphics.Color
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

        try {
            setContent {
                var crashError by remember { mutableStateOf<String?>(null) }

                if (crashError != null) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Text(
                            text = "❌ حدث خطأ أثناء تشغيل التطبيق:\n\n$crashError",
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    try {
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
                    } catch (e: Throwable) {
                        crashError = e.stackTraceToString()
                    }
                }
            }
        } catch (t: Throwable) {
            // إذا فشلت واجهة Compose، يتم عرض الخطأ مباشرة كنص أندرويد عادي لمنع خروج التطبيق
            val tv = TextView(this).apply {
                text = "❌ خطأ أثناء الإقلاع:\n\n" + t.stackTraceToString()
                setPadding(40, 80, 40, 40)
                textSize = 13f
                setTextColor(Color.RED)
            }
            val scroll = ScrollView(this).apply { addView(tv) }
            setContentView(scroll)
        }
    }
}
