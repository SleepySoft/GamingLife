package glcore


class GlData(private val mDatabase: GlDatabase) {

    fun init() {
        groupDataFromDatabase()
        checkInitRuntimeData()
    }

    // ---------------------------------------- Task Group -----------------------------------------

    // ------------------- Data, Load and Write -------------------

    var mTaskGroupTop: GlStrStructDict = mutableMapOf()         // id: name
    var mTaskGroupSub: GlStrStructDict = mutableMapOf()         // id: name
    var mTaskGroupLink = mutableMapOf< String , String >()      // sub id: top id

    private fun groupDataFromDatabase() {
        @Suppress("UNCHECKED_CAST")
        mTaskGroupTop = (mDatabase.systemConfig.getDictAny(PATH_TASK_GROUP_TOP) ?:
                        TASK_GROUP_TOP_PRESET.toMutableMap()) as GlStrStructDict

        @Suppress("UNCHECKED_CAST")
        mTaskGroupSub = (mDatabase.systemConfig.getDictAny(PATH_TASK_GROUP_SUB) ?:
                        mutableMapOf()) as GlStrStructDict

        @Suppress("UNCHECKED_CAST")
        mTaskGroupLink = (mDatabase.systemConfig.getDictStr(PATH_TASK_GROUP_LINK) ?:
                          mutableMapOf())
    }

    private fun groupDataToDatabase() {
        mDatabase.systemConfig.put(PATH_TASK_GROUP_TOP, mTaskGroupTop, forceWrite = true)
        mDatabase.systemConfig.put(PATH_TASK_GROUP_SUB, mTaskGroupSub, forceWrite = true)
        mDatabase.systemConfig.put(PATH_TASK_GROUP_LINK, mTaskGroupLink, forceWrite = true)
    }

    // --------------------------- Gets ---------------------------

    fun getTaskData(glId: String) : GlStrStruct? {
        return mTaskGroupTop[glId] ?: mTaskGroupSub[glId]
    }

    fun nameOfTask(glId: String) = getTaskProperty(glId, "name")
    fun colorOfTask(glId: String) = getTaskProperty(glId, "color")

    private fun getTaskProperty(glId: String, key: String): String {
        return getTaskData(glId)?.get(key) ?: ""
    }

    fun getTaskGroupTop(): GlStrStructDict = mTaskGroupTop

    fun subTaskOfRoot(glId: String): GlStrStructList {
        val subTasks = mutableListOf< GlStrStruct >()
        if (mTaskGroupTop.containsKey(glId))
        {
            for ((k, v) in mTaskGroupLink) {
                if (v == glId) {
                    getTaskData(k)?.run {
                        subTasks.add(this)
                    }
                }
            }
        }
        return subTasks
    }

    // --------------------------- Sets ---------------------------

    private fun setTaskProperty(glId: String, key: String, value: String) {
        if (key != "id") {
            getTaskData(glId)?.set(key, value)
        }
    }

    fun setTaskSubGroup(glId: String, groupId: String) {
        if (mTaskGroupTop.containsKey(groupId)) {
            mTaskGroupLink[glId] = groupId
        }
        else {
            if (mTaskGroupSub.containsKey(glId)) {
                mTaskGroupSub.remove(glId)
            }
        }
        mDatabase.systemConfig.put(PATH_TASK_GROUP_LINK, mTaskGroupLink, forceWrite = true)
    }

    // ----------------------- Task Switching -----------------------

    fun switchToTask(taskData: GlStrStruct) {
        @Suppress("UNCHECKED_CAST")
        if (!checkStruct(taskData as GlAnyStruct, STRUCT_DEC_TASK_DATA)) {
            return
        }

        // Put current task into task history

        val currentTask = getCurrentTaskInfo()
        val taskHistory = mDatabase.dailyRecord.get(PATH_TASK_HISTORY)

        if (taskHistory is MutableList< * >) {
            @Suppress("UNCHECKED_CAST")
            (taskHistory as GlAnyStructList).add(currentTask)
        }
        else {
            mDatabase.dailyRecord.set(PATH_TASK_HISTORY, mutableListOf(currentTask))
        }

        // Set new current task

        mDatabase.runtimeData.set("$PATH_CURRENT_TASK/taskID", "")
        mDatabase.runtimeData.set("$PATH_CURRENT_TASK/groupID", taskData["id"] ?: GROUP_ID_RELAX)
        mDatabase.runtimeData.set("$PATH_CURRENT_TASK/startTime", System.currentTimeMillis())
    }

    fun getCurrentTaskInfo() : GlAnyStruct {
        try {
            when(val currentTaskData = mDatabase.runtimeData.getDictAny(PATH_CURRENT_TASK)) {
                null -> {
                    throw java.lang.Exception("No current task data")
                }
                else -> {
                    val taskID = currentTaskData.get("taskID") as String
                    val groupID = currentTaskData.get("groupID") as String
                    val startTime = currentTaskData.get("startTime") as Long

                    if (groupID.isEmpty() || (startTime <= 0)) {
                        throw java.lang.Exception("Current task data invalid")
                    }
                    @Suppress("UNCHECKED_CAST")
                    return currentTaskData
                }
            }
        }
        catch (e: Exception) {
            val currentTaskData = TASK_RECORD_TEMPLATE.toMutableMap().apply {
                this["startTime"] = System.currentTimeMillis()
            }
            mDatabase.runtimeData.put(PATH_CURRENT_TASK, currentTaskData, true)

            @Suppress("UNCHECKED_CAST")
            return currentTaskData
        }
    }

    // ------------------------------------------ Others -------------------------------------------

    private fun checkInitRuntimeData() {
        checkInitCurrentTask()
    }

    private fun checkInitCurrentTask() {
    }
}