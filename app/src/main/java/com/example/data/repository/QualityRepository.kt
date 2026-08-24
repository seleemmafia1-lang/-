package com.example.data.repository

import com.example.data.DefaultData
import com.example.data.db.AppDatabase
import com.example.data.model.InspectionVisit
import com.example.data.model.QualityProject
import com.example.data.model.QualityReportItem
import com.example.data.model.QualityRule
import com.example.data.model.RaneenUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class QualityRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val projectDao = database.projectDao()
    private val ruleDao = database.ruleDao()
    private val visitDao = database.visitDao()
    private val reportItemDao = database.reportItemDao()

    val allUsers: Flow<List<RaneenUser>> = userDao.getAllUsers()
    val allProjects: Flow<List<QualityProject>> = projectDao.getAllProjects()
    val allRules: Flow<List<QualityRule>> = ruleDao.getAllRules()
    val allVisits: Flow<List<InspectionVisit>> = visitDao.getAllVisits()
    val allReports: Flow<List<QualityReportItem>> = reportItemDao.getAllReports()

    suspend fun ensureDefaultData() = withContext(Dispatchers.IO) {
        if (userDao.getUserCount() == 0) {
            userDao.insertUsers(DefaultData.INITIAL_USERS)
        }
        if (projectDao.getProjectCount() == 0) {
            projectDao.insertProjects(DefaultData.INITIAL_PROJECTS)
        }
        if (ruleDao.getRuleCount() == 0) {
            ruleDao.insertRules(DefaultData.INITIAL_RULES)
        }
    }

    suspend fun getUserByUsername(username: String): RaneenUser? = withContext(Dispatchers.IO) {
        userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: RaneenUser) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: RaneenUser) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: RaneenUser) = withContext(Dispatchers.IO) {
        userDao.deleteUser(user)
    }

    suspend fun addProject(project: QualityProject): Long = withContext(Dispatchers.IO) {
        projectDao.insertProject(project)
    }

    suspend fun updateProject(project: QualityProject) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: QualityProject) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
    }

    suspend fun ensureDefaultRules() = withContext(Dispatchers.IO) {
        val count = ruleDao.getRuleCount()
        if (count == 0) {
            ruleDao.insertRules(DefaultData.INITIAL_RULES)
        }
    }

    suspend fun getAllRulesDirect(): List<QualityRule> = withContext(Dispatchers.IO) {
        val list = ruleDao.getAllRulesDirect()
        if (list.isEmpty()) {
            ruleDao.insertRules(DefaultData.INITIAL_RULES)
            ruleDao.getAllRulesDirect()
        } else {
            list
        }
    }

    suspend fun addRule(rule: QualityRule) = withContext(Dispatchers.IO) {
        ruleDao.insertRule(rule)
    }

    suspend fun deleteRule(code: String): Result<Unit> = withContext(Dispatchers.IO) {
        val usageCount = reportItemDao.countReportsByRuleCode(code)
        if (usageCount > 0) {
            Result.failure(IllegalStateException("لا يمكن حذف ضابط مستخدم في مخالفة أو تقرير سابق ($usageCount ملاحظة)."))
        } else {
            ruleDao.deleteRuleByCode(code)
            Result.success(Unit)
        }
    }

    suspend fun getNextVisitNumber(): String = withContext(Dispatchers.IO) {
        val count = visitDao.getVisitsCount()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val seq = (count + 1).toString().padStart(4, '0')
        "V-$year-$seq"
    }

    suspend fun saveInspection(
        visit: InspectionVisit,
        reports: List<QualityReportItem>
    ): Long = withContext(Dispatchers.IO) {
        val visitId = visitDao.insertVisit(visit)
        if (reports.isNotEmpty()) {
            val updatedReports = reports.map { it.copy(visitId = visitId) }
            reportItemDao.insertReports(updatedReports)
        }
        visitId
    }

    suspend fun getLatestVisit(): InspectionVisit? = withContext(Dispatchers.IO) {
        visitDao.getLatestVisit()
    }

    suspend fun getVisitById(id: Long): InspectionVisit? = withContext(Dispatchers.IO) {
        visitDao.getVisitById(id)
    }

    suspend fun cycleReportStatus(reportId: Long, currentStatus: String) = withContext(Dispatchers.IO) {
        val nextStatus = when (currentStatus) {
            "جديدة" -> "قيد المعالجة"
            "قيد المعالجة" -> "مغلقة"
            else -> "جديدة"
        }
        reportItemDao.updateStatus(reportId, nextStatus)
    }

    suspend fun deleteReport(report: QualityReportItem) = withContext(Dispatchers.IO) {
        reportItemDao.deleteReport(report)
    }

    suspend fun deleteVisit(visit: InspectionVisit) = withContext(Dispatchers.IO) {
        visitDao.deleteVisit(visit)
    }
}
