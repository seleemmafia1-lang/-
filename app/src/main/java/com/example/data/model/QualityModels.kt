package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "quality_rules")
@JsonClass(generateAdapter = true)
data class QualityRule(
    @PrimaryKey val code: String,
    val section: String,
    val name: String,
    val isCustom: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "quality_projects")
@JsonClass(generateAdapter = true)
data class QualityProject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val branchCode: String,
    val city: String,
    val managerName: String,
    val description: String = "",
    val targetScore: Int = 90,
    val status: String = "نشط",
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class InspectionItemEvaluation(
    val code: String,
    val section: String,
    val name: String,
    val state: String = "", // "مطابق", "غير مطابق", "لا ينطبق", or "" (unanswered)
    val severity: String = "متوسطة", // "منخفضة", "متوسطة", "عالية"
    val note: String = "",
    val action: String = "",
    val photoUri: String = ""
)

@Entity(tableName = "inspection_visits")
@JsonClass(generateAdapter = true)
data class InspectionVisit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val visitNumber: String,
    val dateFormatted: String,
    val timestamp: Long,
    val inspectorName: String,
    val projectName: String,
    val location: String,
    val notes: String,
    val scorePercentage: Int,
    val totalEvaluated: Int,
    val compliantCount: Int,
    val nonCompliantCount: Int,
    val itemsJson: String // Serialized List<InspectionItemEvaluation>
)

@Entity(tableName = "quality_reports")
@JsonClass(generateAdapter = true)
data class QualityReportItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val visitId: Long = 0,
    val visitNumber: String,
    val projectName: String,
    val location: String,
    val ruleCode: String,
    val ruleName: String,
    val ruleSection: String,
    val note: String,
    val action: String,
    val severity: String, // "منخفضة", "متوسطة", "عالية"
    val photoUri: String = "",
    val status: String = "جديدة", // "جديدة", "قيد المعالجة", "مغلقة"
    val dateFormatted: String,
    val createdAt: Long = System.currentTimeMillis()
)
