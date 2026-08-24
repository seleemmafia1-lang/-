package com.example.util

import com.example.data.model.InspectionItemEvaluation
import org.json.JSONArray
import org.json.JSONObject

object JsonUtil {
    fun serializeEvaluationList(items: List<InspectionItemEvaluation>): String {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("code", item.code)
            obj.put("section", item.section)
            obj.put("name", item.name)
            obj.put("state", item.state)
            obj.put("severity", item.severity)
            obj.put("note", item.note)
            obj.put("action", item.action)
            obj.put("photoUri", item.photoUri)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    fun deserializeEvaluationList(jsonStr: String?): List<InspectionItemEvaluation> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        val list = mutableListOf<InspectionItemEvaluation>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    InspectionItemEvaluation(
                        code = obj.optString("code", ""),
                        section = obj.optString("section", ""),
                        name = obj.optString("name", ""),
                        state = obj.optString("state", ""),
                        severity = obj.optString("severity", "متوسطة"),
                        note = obj.optString("note", ""),
                        action = obj.optString("action", ""),
                        photoUri = obj.optString("photoUri", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
