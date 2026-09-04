package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.InspectionItemEvaluation
import com.example.data.model.InspectionVisit
import com.example.data.model.QualityProject
import com.example.data.model.QualityReportItem
import com.example.data.model.QualityRule
import com.example.data.model.RaneenUser
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.QualityRepository
import com.example.util.JsonUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class InspectionFormState(
    val visitNumber: String = "",
    val visitDate: String = "",
    val inspectorName: String = "",
    val projectName: String = "",
    val location: String = "",
    val generalNotes: String = "",
    val evaluations: Map<String, InspectionItemEvaluation> = emptyMap()
) {
    val totalEvaluated: Int
        get() = evaluations.values.count { it.state == "مطابق" || it.state == "غير مطابق" }

    val compliantCount: Int
        get() = evaluations.values.count { it.state == "مطابق" }

    val nonCompliantCount: Int
        get() = evaluations.values.count { it.state == "غير مطابق" }

    val scorePercentage: Int
        get() = if (totalEvaluated > 0) {
            ((compliantCount.toDouble() / totalEvaluated.toDouble()) * 100.0).toInt()
        } else {
            0
        }
}

class QualityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QualityRepository = QualityRepository(AppDatabase.getInstance(application))

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    val projects: StateFlow<List<QualityProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<RaneenUser>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                repository.ensureDefaultData()
                initNewInspection()
            } catch (e: Exception) {
                _uiMessage.emit("خطأ أثناء تهيئة البيانات: ${e.localizedMessage ?: "غير معروف"}")
            }
        }
    }

    fun login(
        username: String,
        passwordInput: String = "123",
        role: UserRole,
        branch: String = "",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val cleanUsername = username.trim()
        if (cleanUsername.isEmpty()) {
            onResult(false, "يرجى إدخال اسم المستخدم أو الرقم الوظيفي")
            return
        }

        if (passwordInput.isBlank()) {
            onResult(false, "يرجى إدخال كلمة المرور")
            return
        }

        viewModelScope.launch {
            try {
                val existingUser = repository.getUserByUsername(cleanUsername)
                if (existingUser == null) {
                    val errorMsg = "اسم المستخدم ($cleanUsername) غير مسجل في قاعدة البيانات."
                    _uiMessage.emit(errorMsg)
                    onResult(false, errorMsg)
                    return@launch
                }

                val isPasswordValid = existingUser.passwordHash == passwordInput || 
                                     (existingUser.passwordHash == "123" && (passwordInput == "123" || passwordInput == "123456"))
                if (!isPasswordValid) {
                    val errorMsg = "كلمة المرور غير صحيحة. يرجى التأكد وإعادة المحاولة."
                    _uiMessage.emit(errorMsg)
                    onResult(false, errorMsg)
                    return@launch
                }

                if (existingUser.role != role) {
                    val errorMsg = "الدور المحدد (${role.title}) لا يتطابق مع دور الحساب المسجل (${existingUser.role.title})."
                    _uiMessage.emit(errorMsg)
                    onResult(false, errorMsg)
                    return@launch
                }

                val finalUser = existingUser.toProfile()
                _currentUser.value = finalUser
                _formState.update { it.copy(inspectorName = finalUser.fullName) }
                _uiMessage.emit("مرحباً بك، ${finalUser.fullName} (${finalUser.role.title})")
                onResult(true, "تم تسجيل الدخول بنجاح")
            } catch (e: Exception) {
                val err = "حدث خطأ أثناء الاتصال بقاعدة البيانات: ${e.localizedMessage ?: "غير معروف"}"
                _uiMessage.emit(err)
                onResult(false, err)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        viewModelScope.launch {
            _uiMessage.emit("تم تسجيل الخروج بنجاح")
        }
    }

    val rules: StateFlow<List<QualityRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<QualityReportItem>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visits: StateFlow<List<InspectionVisit>> = repository.allVisits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow(InspectionFormState())
    val formState: StateFlow<InspectionFormState> = _formState.asStateFlow()

    private val _lastSavedVisit = MutableStateFlow<Pair<InspectionVisit, List<InspectionItemEvaluation>>?>(null)
    val lastSavedVisit: StateFlow<Pair<InspectionVisit, List<InspectionItemEvaluation>>?> = _lastSavedVisit.asStateFlow()

    private val _reportSearchQuery = MutableStateFlow("")
    val reportSearchQuery: StateFlow<String> = _reportSearchQuery.asStateFlow()

    private val _reportStatusFilter = MutableStateFlow<String?>("الكل")
    val reportStatusFilter: StateFlow<String?> = _reportStatusFilter.asStateFlow()

    private val _reportSeverityFilter = MutableStateFlow<String?>("الكل")
    val reportSeverityFilter: StateFlow<String?> = _reportSeverityFilter.asStateFlow()

    private val _rulesSearchQuery = MutableStateFlow("")
    val rulesSearchQuery: StateFlow<String> = _rulesSearchQuery.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    fun updateReportSearch(query: String) {
        _reportSearchQuery.value = query
    }

    fun updateReportStatusFilter(status: String?) {
        _reportStatusFilter.value = status
    }

    fun updateReportSeverityFilter(severity: String?) {
        _reportSeverityFilter.value = severity
    }

    fun updateRulesSearch(query: String) {
        _rulesSearchQuery.value = query
    }

    fun initNewInspection() {
        viewModelScope.launch {
            val nextNo = repository.getNextVisitNumber()
            val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val currentRules = repository.getAllRulesDirect()

            val initialEvaluations = currentRules.associate { rule ->
                rule.code to InspectionItemEvaluation(
                    code = rule.code,
                    section = rule.section,
                    name = rule.name,
                    state = "",
                    severity = "متوسطة",
                    note = "",
                    action = "",
                    photoUri = ""
                )
            }

            _formState.value = InspectionFormState(
                visitNumber = nextNo,
                visitDate = nowStr,
                inspectorName = _formState.value.inspectorName, // retain inspector name for convenience
                projectName = "",
                location = "",
                generalNotes = "",
                evaluations = initialEvaluations
            )
        }
    }

    fun setInspectorName(name: String) {
        _formState.update { it.copy(inspectorName = name) }
    }

    fun setProjectName(name: String) {
        _formState.update { it.copy(projectName = name) }
    }

    fun setLocation(loc: String) {
        _formState.update { it.copy(location = loc) }
    }

    fun setGeneralNotes(notes: String) {
        _formState.update { it.copy(generalNotes = notes) }
    }

    fun setVisitDate(date: String) {
        _formState.update { it.copy(visitDate = date) }
    }

    fun setItemState(code: String, state: String) {
        _formState.update { current ->
            val existing = current.evaluations[code] ?: return@update current
            val updated = existing.copy(state = state)
            val newMap = current.evaluations.toMutableMap()
            newMap[code] = updated
            current.copy(evaluations = newMap)
        }
    }

    fun setItemSeverity(code: String, severity: String) {
        _formState.update { current ->
            val existing = current.evaluations[code] ?: return@update current
            val updated = existing.copy(severity = severity)
            val newMap = current.evaluations.toMutableMap()
            newMap[code] = updated
            current.copy(evaluations = newMap)
        }
    }

    fun setItemNote(code: String, note: String) {
        _formState.update { current ->
            val existing = current.evaluations[code] ?: return@update current
            val updated = existing.copy(note = note)
            val newMap = current.evaluations.toMutableMap()
            newMap[code] = updated
            current.copy(evaluations = newMap)
        }
    }

    fun setItemAction(code: String, action: String) {
        _formState.update { current ->
            val existing = current.evaluations[code] ?: return@update current
            val updated = existing.copy(action = action)
            val newMap = current.evaluations.toMutableMap()
            newMap[code] = updated
            current.copy(evaluations = newMap)
        }
    }

    fun setItemPhoto(code: String, photoUri: String) {
        _formState.update { current ->
            val existing = current.evaluations[code] ?: return@update current
            val updated = existing.copy(photoUri = photoUri)
            val newMap = current.evaluations.toMutableMap()
            newMap[code] = updated
            current.copy(evaluations = newMap)
        }
    }

    fun saveInspection(onSuccess: (InspectionVisit, List<InspectionItemEvaluation>) -> Unit) {
        val form = _formState.value
        if (form.inspectorName.isBlank() || form.projectName.isBlank()) {
            viewModelScope.launch {
                _uiMessage.emit("يرجى إدخال اسم المفتش واسم المشروع / الفرع.")
            }
            return
        }

        viewModelScope.launch {
            val allRulesList = repository.getAllRulesDirect()
            val evaluationItems = allRulesList.map { rule ->
                form.evaluations[rule.code] ?: InspectionItemEvaluation(
                    code = rule.code,
                    section = rule.section,
                    name = rule.name,
                    state = ""
                )
            }

            val itemsJson = JsonUtil.serializeEvaluationList(evaluationItems)
            val visit = InspectionVisit(
                visitNumber = form.visitNumber.ifBlank { repository.getNextVisitNumber() },
                dateFormatted = form.visitDate,
                timestamp = System.currentTimeMillis(),
                inspectorName = form.inspectorName,
                projectName = form.projectName,
                location = form.location,
                notes = form.generalNotes,
                scorePercentage = form.scorePercentage,
                totalEvaluated = form.totalEvaluated,
                compliantCount = form.compliantCount,
                nonCompliantCount = form.nonCompliantCount,
                itemsJson = itemsJson
            )

            val nonCompliantReports = evaluationItems.filter { it.state == "غير مطابق" }.map { item ->
                QualityReportItem(
                    visitNumber = visit.visitNumber,
                    projectName = visit.projectName,
                    location = visit.location,
                    ruleCode = item.code,
                    ruleName = item.name,
                    ruleSection = item.section,
                    note = item.note,
                    action = item.action,
                    severity = item.severity.ifBlank { "متوسطة" },
                    photoUri = item.photoUri,
                    status = "جديدة",
                    dateFormatted = visit.dateFormatted,
                    createdAt = System.currentTimeMillis()
                )
            }

            val visitId = repository.saveInspection(visit, nonCompliantReports)
            val savedVisit = visit.copy(id = visitId)
            _lastSavedVisit.value = Pair(savedVisit, evaluationItems)

            _uiMessage.emit("✅ تم حفظ زيارة التفتيش (${savedVisit.visitNumber}) وإنشاء ${nonCompliantReports.size} مخالفة.")
            onSuccess(savedVisit, evaluationItems)
            initNewInspection()
        }
    }

    fun cycleReportStatus(report: QualityReportItem) {
        viewModelScope.launch {
            repository.cycleReportStatus(report.id, report.status)
        }
    }

    fun deleteReport(report: QualityReportItem) {
        viewModelScope.launch {
            repository.deleteReport(report)
            _uiMessage.emit("تم حذف الملاحظة.")
        }
    }

    fun deleteVisit(visit: InspectionVisit) {
        viewModelScope.launch {
            repository.deleteVisit(visit)
            _uiMessage.emit("تم حذف الزيارة ${visit.visitNumber}.")
        }
    }

    fun addNewRule(code: String, section: String, name: String, onComplete: (Boolean) -> Unit) {
        if (section.isBlank() || name.isBlank()) {
            viewModelScope.launch {
                _uiMessage.emit("يرجى كتابة القسم واسم الضابط.")
            }
            onComplete(false)
            return
        }

        viewModelScope.launch {
            val all = repository.getAllRulesDirect()
            val finalCode = code.trim().ifBlank {
                "Q-" + (all.size + 1).toString().padStart(3, '0')
            }

            if (all.any { it.code.equals(finalCode, ignoreCase = true) }) {
                _uiMessage.emit("كود الضابط ($finalCode) موجود بالفعل.")
                onComplete(false)
                return@launch
            }

            val newRule = QualityRule(
                code = finalCode,
                section = section.trim(),
                name = name.trim(),
                isCustom = true,
                orderIndex = all.size + 1
            )
            repository.addRule(newRule)
            _uiMessage.emit("✅ تمت إضافة الضابط ($finalCode) بنجاح.")
            onComplete(true)
        }
    }

    fun deleteRule(code: String) {
        viewModelScope.launch {
            val res = repository.deleteRule(code)
            res.onSuccess {
                _uiMessage.emit("✅ تم حذف الضابط بنجاح.")
            }.onFailure { err ->
                _uiMessage.emit("⚠️ ${err.message}")
            }
        }
    }
}
