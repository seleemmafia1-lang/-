package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class UserRole(
    val title: String,
    val description: String,
    val iconName: String
) {
    INSPECTOR(
        title = "مفتش جودة",
        description = "إجراء التقييمات الميدانية ورصد الملاحظات",
        iconName = "inspector"
    ),
    QUALITY_MANAGER(
        title = "مدير جودة",
        description = "إشراف شامل وإدارة معايير وضوابط الجودة",
        iconName = "manager"
    ),
    BRANCH_MANAGER(
        title = "مدير فرع",
        description = "متابعة الملاحظات والخطط التصحيحية للفرع",
        iconName = "branch"
    ),
    ADMIN(
        title = "مسؤول النظام",
        description = "إدارة النظام والمعايير الشاملة",
        iconName = "admin"
    )
}

@Entity(tableName = "users")
data class RaneenUser(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val passwordHash: String,
    val role: UserRole,
    val fullName: String = "",
    val branchName: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toProfile(): UserProfile = UserProfile(
        id = id,
        username = username,
        fullName = if (fullName.isNotBlank()) fullName else username,
        role = role,
        branchName = branchName
    )
}

data class UserProfile(
    val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val branchName: String,
    val initials: String = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString(".")
)
