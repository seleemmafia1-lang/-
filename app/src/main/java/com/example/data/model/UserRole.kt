package com.example.data.model

enum class UserRole(val title: String, val displayName: String) {
    INSPECTOR("مفتش", "مفتش الجودة الميداني"),
    BRANCH_MANAGER("مدير فرع", "مدير الفرع والموارد"),
    SUPERVISOR("مراقب", "مراقب الجودة والتقييمات"),
    QUALITY_MANAGER("مدير جودة", "مدير نظام الجودة"),
    ADMIN("مسؤول النظام", "إدارة النظام الكاملة")
}
