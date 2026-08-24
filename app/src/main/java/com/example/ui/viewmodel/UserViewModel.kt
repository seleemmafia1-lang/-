package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.RaneenUser
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.QualityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val user: UserProfile) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QualityRepository = QualityRepository(AppDatabase.getInstance(application))

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    val users: StateFlow<List<RaneenUser>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }

    fun login(
        username: String,
        passwordInput: String,
        role: UserRole,
        branch: String = "",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val cleanUsername = username.trim()
        if (cleanUsername.isEmpty()) {
            val msg = "يرجى إدخال اسم المستخدم أو الرقم الوظيفي"
            _loginState.value = LoginUiState.Error(msg)
            onResult(false, msg)
            return
        }

        if (passwordInput.isBlank()) {
            val msg = "يرجى إدخال كلمة المرور"
            _loginState.value = LoginUiState.Error(msg)
            onResult(false, msg)
            return
        }

        _loginState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val existingUser = repository.getUserByUsername(cleanUsername)
                if (existingUser == null) {
                    val errorMsg = "اسم المستخدم ($cleanUsername) غير مسجل في قاعدة البيانات."
                    _loginState.value = LoginUiState.Error(errorMsg)
                    _uiMessage.emit(errorMsg)
                    onResult(false, errorMsg)
                    return@launch
                }

                // Verify password
                val isPasswordValid = existingUser.passwordHash == passwordInput || 
                                     (existingUser.passwordHash == "123" && (passwordInput == "123" || passwordInput == "123456"))
                if (!isPasswordValid) {
                    val errorMsg = "كلمة المرور غير صحيحة. يرجى التأكد وإعادة المحاولة."
                    _loginState.value = LoginUiState.Error(errorMsg)
                    _uiMessage.emit(errorMsg)
                    onResult(false, errorMsg)
                    return@launch
                }

                // Verify Role
                if (existingUser.role != role) {
                    val errorMsg = "الدور المحدد (${role.title}) لا يتطابق مع دور الحساب المسجل (${existingUser.role.title})."
                    _loginState.value = LoginUiState.Error(errorMsg)
                    _uiMessage.emit(errorMsg)
                    onResult(false, errorMsg)
                    return@launch
                }

                val finalUser = existingUser.toProfile()
                _currentUser.value = finalUser
                _loginState.value = LoginUiState.Success(finalUser)
                _uiMessage.emit("مرحباً بك، ${finalUser.fullName} (${finalUser.role.title})")
                onResult(true, "تم تسجيل الدخول بنجاح")
            } catch (e: Exception) {
                val err = "حدث خطأ أثناء الاتصال بقاعدة البيانات: ${e.localizedMessage ?: "غير معروف"}"
                _loginState.value = LoginUiState.Error(err)
                _uiMessage.emit(err)
                onResult(false, err)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginState.value = LoginUiState.Idle
        viewModelScope.launch {
            _uiMessage.emit("تم تسجيل الخروج بنجاح")
        }
    }

    fun registerNewUser(
        username: String,
        fullName: String,
        role: UserRole,
        branchName: String,
        passwordHash: String = "123",
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existing = repository.getUserByUsername(username.trim())
                if (existing != null) {
                    onComplete(false, "اسم المستخدم موجود مسبقاً")
                    return@launch
                }

                val newUser = RaneenUser(
                    username = username.trim(),
                    fullName = fullName.trim(),
                    role = role,
                    branchName = branchName.trim(),
                    passwordHash = passwordHash
                )
                repository.insertUser(newUser)
                _uiMessage.emit("تم إضافة المستخدم ${newUser.fullName} بنجاح")
                onComplete(true, "تمت الإضافة بنجاح")
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "حدث خطأ غير متوقع")
            }
        }
    }

    fun deleteUser(user: RaneenUser) {
        viewModelScope.launch {
            repository.deleteUser(user)
            _uiMessage.emit("تم حذف المستخدم ${user.fullName}")
        }
    }
}
