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

data class AuthUiState(
    val currentUser: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QualityRepository = QualityRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val users: StateFlow<List<RaneenUser>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }

    fun login(username: String, passwordInput: String) {
        val cleanUsername = username.trim()
        if (cleanUsername.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "يرجى إدخال اسم المستخدم أو الرقم الوظيفي") }
            return
        }
        if (passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "يرجى إدخال كلمة المرور") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val existingUser = repository.getUserByUsername(cleanUsername)
                if (existingUser == null) {
                    val msg = "اسم المستخدم ($cleanUsername) غير مسجل في قاعدة البيانات."
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                    _uiMessage.emit(msg)
                    return@launch
                }

                val isPasswordValid = existingUser.passwordHash == passwordInput ||
                        (existingUser.passwordHash == "123" && (passwordInput == "123" || passwordInput == "123456"))
                if (!isPasswordValid) {
                    val msg = "كلمة المرور غير صحيحة. يرجى التأكد وإعادة المحاولة."
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                    _uiMessage.emit(msg)
                    return@launch
                }

                val finalUser = existingUser.toProfile()
                _uiState.update { it.copy(isLoading = false, currentUser = finalUser, errorMessage = null) }
                _uiMessage.emit("مرحباً بك، ${finalUser.fullName} (${finalUser.role.title})")
            } catch (e: Exception) {
                val err = "حدث خطأ أثناء الاتصال بقاعدة البيانات: ${e.localizedMessage ?: "غير معروف"}"
                _uiState.update { it.copy(isLoading = false, errorMessage = err) }
                _uiMessage.emit(err)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun logout() {
        _uiState.value = AuthUiState()
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
