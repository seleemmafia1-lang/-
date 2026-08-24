package com.example.data.db

import androidx.room.*
import com.example.data.model.InspectionVisit
import com.example.data.model.QualityProject
import com.example.data.model.QualityReportItem
import com.example.data.model.QualityRule
import com.example.data.model.RaneenUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun getAllUsers(): Flow<List<RaneenUser>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): RaneenUser?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): RaneenUser?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<RaneenUser>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: RaneenUser)

    @Update
    suspend fun updateUser(user: RaneenUser)

    @Delete
    suspend fun deleteUser(user: RaneenUser)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM quality_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<QualityProject>>

    @Query("SELECT * FROM quality_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): QualityProject?

    @Query("SELECT COUNT(*) FROM quality_projects")
    suspend fun getProjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<QualityProject>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: QualityProject): Long

    @Update
    suspend fun updateProject(project: QualityProject)

    @Delete
    suspend fun deleteProject(project: QualityProject)
}

@Dao
interface RuleDao {
    @Query("SELECT * FROM quality_rules ORDER BY orderIndex ASC, code ASC")
    fun getAllRules(): Flow<List<QualityRule>>

    @Query("SELECT * FROM quality_rules ORDER BY orderIndex ASC, code ASC")
    suspend fun getAllRulesDirect(): List<QualityRule>

    @Query("SELECT COUNT(*) FROM quality_rules")
    suspend fun getRuleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<QualityRule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: QualityRule)

    @Delete
    suspend fun deleteRule(rule: QualityRule)

    @Query("DELETE FROM quality_rules WHERE code = :code")
    suspend fun deleteRuleByCode(code: String)
}

@Dao
interface VisitDao {
    @Query("SELECT * FROM inspection_visits ORDER BY timestamp DESC")
    fun getAllVisits(): Flow<List<InspectionVisit>>

    @Query("SELECT * FROM inspection_visits WHERE id = :id")
    suspend fun getVisitById(id: Long): InspectionVisit?

    @Query("SELECT * FROM inspection_visits ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestVisit(): InspectionVisit?

    @Query("SELECT COUNT(*) FROM inspection_visits")
    suspend fun getVisitsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: InspectionVisit): Long

    @Delete
    suspend fun deleteVisit(visit: InspectionVisit)
}

@Dao
interface ReportItemDao {
    @Query("SELECT * FROM quality_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<QualityReportItem>>

    @Query("SELECT * FROM quality_reports WHERE visitNumber = :visitNumber ORDER BY createdAt DESC")
    fun getReportsForVisit(visitNumber: String): Flow<List<QualityReportItem>>

    @Query("SELECT COUNT(*) FROM quality_reports WHERE ruleCode = :code")
    suspend fun countReportsByRuleCode(code: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<QualityReportItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: QualityReportItem): Long

    @Update
    suspend fun updateReport(report: QualityReportItem)

    @Query("UPDATE quality_reports SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Delete
    suspend fun deleteReport(report: QualityReportItem)
}
