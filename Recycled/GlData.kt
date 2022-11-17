package glcore


interface IGlDeclare {
    var dataValid : Boolean
    fun fromAnyStruct(data: Any?) : Boolean
    fun toAnyStruct() : GlAnyStruct
}


class TaskRecord() : IGlDeclare {
    var taskID: String = ""
    var groupID: String = ""
    var startTime: Long = 0

    var extEndTime: Long = 0        // Extra, not storage

    override var dataValid: Boolean = false

    override fun fromAnyStruct(data: Any?): Boolean {
        val anyStruct = toAnyStruct(data)
        dataValid = if (checkStruct(anyStruct, STRUCT_DEC_TASK_RECORD)) {
            taskID = anyStruct.get("taskID") as String
            groupID = anyStruct.get("groupID") as String
            startTime = anyStruct.get("startTime") as Long
            true
        }
        else {
            false
        }
        return dataValid
    }

    override fun toAnyStruct(): GlAnyStruct {
        return mutableMapOf(
            "taskID" to taskID,
            "groupID" to groupID,
            "startTime" to startTime,
        )
    }
}


