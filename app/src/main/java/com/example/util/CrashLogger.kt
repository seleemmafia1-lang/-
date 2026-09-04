package com.example.util

import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter

/**
 * بيسجل أي Exception غير ملتقط (حتى لو حصل جوه كوروتين/باك جراوند)
 * في ملف على الجهاز، عشان لو التطبيق قفل فجأة، تقدر تفتحه تاني وتشوف
 * السبب الحقيقي مكتوب على الشاشة بدل ما يقفل بصمت.
 */
object CrashLogger {
    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                saveCrash(appContext, throwable)
            } catch (_: Throwable) {
                // متعملش حاجة لو فشل التسجيل نفسه، منزلش نفسنا في حلقة كراش تانية
            }
            // نسيب النظام يقفل التطبيق بشكل طبيعي بعد ما سجلنا الخطأ
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrash(context: Context, throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(sw.toString().toByteArray())
        }
    }

    /** بيرجع نص آخر كراش لو موجود، وبيمسحه بعد القراءة */
    fun consumeLastCrash(context: Context): String? {
        val file = context.getFileStreamPath(FILE_NAME)
        if (!file.exists()) return null
        val text = file.readText()
        file.delete()
        return text
    }
}
