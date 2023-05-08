package glcore

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


fun isJSONValid(test: String): Boolean {
    try {
        JSONObject(test)
    } catch (ex: JSONException) {
        try {
            JSONArray(test)
        } catch (ex1: JSONException) {
            return false
        }
    }
    return true
}


fun verifyJsonText(text: String) {
    System.out.println(text)
    assert(isJSONValid(text))
}


fun generateSystemGeneralPathDict() : PathDict {
    return PathDict().apply {
        this.set(PATH_SYSTEM_TASK_GROUP_TOP_LEVEL, TASK_GROUP_TOP_PRESET)
        this.set(PATH_RUNTIME_CURRENT_TASK, TASK_RECORD_TEMPLATE)
        this.set(
            PATH_DAILY_TASK_RECORD, listOf(
                TASK_RECORD_TEMPLATE,
                TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_ENJOY },
                TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_LIFE },
                TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_WORK },
                TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_PROMOTE },
                TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_CREATE },
            )
        )
    }
}










