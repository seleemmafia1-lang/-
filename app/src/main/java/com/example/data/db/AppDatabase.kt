package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.DefaultData
import com.example.data.model.InspectionVisit
import com.example.data.model.QualityProject
import com.example.data.model.QualityReportItem
import com.example.data.model.QualityRule
import com.example.data.model.RaneenUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RaneenUser::class,
        QualityProject::class,
        QualityRule::class,
        InspectionVisit::class,
        QualityReportItem::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun ruleDao(): RuleDao
    abstract fun visitDao(): VisitDao
    abstract fun reportItemDao(): ReportItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "raneen_quality_database.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                database.userDao().insertUsers(DefaultData.INITIAL_USERS)
                                database.projectDao().insertProjects(DefaultData.INITIAL_PROJECTS)
                                database.ruleDao().insertRules(DefaultData.INITIAL_RULES)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
