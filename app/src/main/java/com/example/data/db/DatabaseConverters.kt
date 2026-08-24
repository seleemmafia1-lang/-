package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.UserRole

class DatabaseConverters {
    @TypeConverter
    fun fromUserRole(role: UserRole?): String? {
        return role?.name
    }

    @TypeConverter
    fun toUserRole(value: String?): UserRole? {
        return value?.let {
            try {
                UserRole.valueOf(it)
            } catch (e: Exception) {
                UserRole.INSPECTOR
            }
        }
    }
}
