package glcore

import java.util.Date


class GlTaskModule(private val mDatabase: GlDatabase) {

    fun init() {

    }

    // ---------------------------------------- Task Group -----------------------------------------

    // ------------------- Data, Load and Write -------------------

    @Suppress("UNCHECKED_CAST")
    val mTaskGroupTop: GlStrStructDict                      // id: name
        get() = (mDatabase.systemConfig.getDictAny(PATH_SYSTEM_TASK_GROUP_TOP) ?:
                    TASK_GROUP_TOP_PRESET.toMutableMap()) as GlStrStructDict

    @Suppress("UNCHECKED_CAST")
    val mTaskGroupSub: GlStrStructDict                      // id: name
        get() =  (mDatabase.systemConfig.getDictAny(PATH_SYSTEM_TASK_GROUP_SUB) ?:
                    mutableMapOf()) as GlStrStructDict

    @Suppress("UNCHECKED_CAST")
    val mTaskGroupLink: MutableMap< String , String >       // sub id: top id
        get() = (mDatabase.systemConfig.getDictStr(PATH_SYSTEM_TASK_GROUP_LINK) ?:
                    mutableMapOf())

/*    private fun groupDataFromDatabase() {
        @Suppress("UNCHECKED_CAST")
        mTaskGroupTop = (mDatabase.systemConfig.getDictAny(PATH_SYSTEM_TASK_GROUP_TOP) ?:
                        TASK_GROUP_TOP_PRESET.toMutableMap()) as GlStrStructDict

        @Suppress("UNCHECKED_CAST")
        mTaskGroupSub = (mDatabase.systemConfig.getDictAny(PATH_SYSTEM_TASK_GROUP_SUB) ?:
                        mutableMapOf()) as GlStrStructDict

        @Suppress("UNCHECKED_CAST")
        mTaskGroupLink = (mDatabase.systemConfig.getDictStr(PATH_SYSTEM_TASK_GROUP_LINK) ?:
                mutableMapOf< String , String >()
    }

    private fun groupDataToDatabase() {
        mDatabase.systemConfig.put(PATH_SYSTEM_TASK_GROUP_TOP, mTaskGroupTop, forceWrite = true)
        mDatabase.systemConfig.put(PATH_SYSTEM_TASK_GROUP_SUB, mTaskGroupSub, forceWrite = true)
        mDatabase.systemConfig.put(PATH_SYSTEM_TASK_GROUP_LINK, mTaskGroupLink, forceWrite = true)
    }*/

    // --------------------------- Gets ---------------------------

    fun getTaskData(glId: String) : GlStrStruct? {
        return mTaskGroupTop[glId] ?: mTaskGroupSub[glId]
    }

    fun nameOfTask(glId: String): String = getTaskProperty(glId, "name")
    fun colorOfTask(glId: String): String = getTaskProperty(glId, "color")
    fun groupOfTask(glId: String): String =
        if (mTaskGroupTop.containsKey(glId)) glId else mTaskGroupLink[glId] ?: GROUP_ID_IDLE

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
        mDatabase.systemConfig.put(PATH_SYSTEM_TASK_GROUP_LINK, mTaskGroupLink, forceWrite = true)
    }

    // ----------------------- Task Switching -----------------------

    fun switchToTask(taskData: GlStrStruct) {
        checkSettleDailyData()

        @Suppress("UNCHECKED_CAST")
        if (!checkStruct(taskData as GlAnyStruct, STRUCT_DEC_TASK_DATA)) {
            return
        }

        // Put current task into task history
/*
        val currentTask = getCurrentTaskInfo()
        val currentTaskCopy = currentTask.deepCopy()
        val taskHistory = mDatabase.dailyRecord.get(PATH_DAILY_TASK_HISTORY)

        if (taskHistory is MutableList< * >) {
            @Suppress("UNCHECKED_CAST")
            (taskHistory as GlAnyStructList).add(currentTaskCopy)
        }
        else {
            mDatabase.dailyRecord.set(PATH_DAILY_TASK_HISTORY, mutableListOf(currentTaskCopy))
        }*/

        currentTaskToHistory()
        setCurrentTask(taskData["id"])

        // Set new current task

/*        mDatabase.runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/taskID", "")
        mDatabase.runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/groupID", taskData["id"] ?: GROUP_ID_IDLE)
        mDatabase.runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/startTime", System.currentTimeMillis())*/

        mDatabase.save()
    }

    fun getCurrentTaskInfo() : GlAnyStruct {
        try {
            when(val currentTaskData = mDatabase.runtimeData.getDictAny(PATH_RUNTIME_CURRENT_TASK)) {
                null -> {
                    throw java.lang.Exception("No current task data")
                }
                else -> {
                    // val taskID = currentTaskData.get("taskID") as String
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
            mDatabase.runtimeData.put(PATH_RUNTIME_CURRENT_TASK, currentTaskData, true)

            @Suppress("UNCHECKED_CAST")
            return currentTaskData
        }
    }

    // ---------------------------------------- Daily data ----------------------------------------

    fun checkSettleDailyData() {
        GlLog.i("Check settle daily data.")

        /*******************************************************************************************
         *
         * The algorithm of settling daily data:
         * If daily data's timestamp does not match current day's start time, it means we should do
         *      the daily data settling.
         * 1. Duplicate and record the current task data to history data
         * 2. Reset the current task's start time to current day's start time
         * 3. Archive the daily data to it's daily folder (by its time stamp).
         *
         *******************************************************************************************/

        val dataTs: Any? = GlRoot.glDatabase.dailyRecord.get(PATH_DAILY_START_TS)
        if (dataTs is Long) {
            val dailyDataTs: Long = dataTs
            val currentDayTs: Long = GlDateTime.dayStartTimeStamp()
            if (currentDayTs != dailyDataTs) {
                GlLog.i("> Do settling.")
                settleDailyData(dailyDataTs, currentDayTs)
                resetDayDailyData()
            }
            else {
                // The daily data is the current day, everything is OK.
                GlLog.i("> Not need to settle.")
            }
        }
        else {
            GlLog.i("> No daily data.")
            resetDayDailyData()
        }
    }

    // ------------------------------------------ Private ------------------------------------------

    private fun settleDailyData(dailyDataTs: Long, currentDayTs: Long) {
        val currentTask = getCurrentTaskInfo()
        val currentTaskStartTs = currentTask["startTime"] as Long
        if (currentTaskStartTs < currentDayTs) {
            // The task starts before today

            if ((currentTaskStartTs >= dailyDataTs) &&
                (currentTaskStartTs <  dailyDataTs + TIMESTAMP_COUNT_IN_DAY)) {
                // The task starts from the daily data's day
                currentTaskToHistory()
            }

            // Limit the task start time in today
            setCurrentTask(currentTask["id"], currentDayTs)
        }
        else {
            // The task does not cross day, usually in manually update json scenario.
        }
        GlRoot.glDatabase.save()
        GlRoot.archiveJsonFilesToDailyFolder(Date(dailyDataTs))
    }

    private fun resetDayDailyData() {
        GlRoot.glDatabase.dailyRecord.clear()
        GlRoot.glDatabase.dailyRecord.set(PATH_DAILY_START_TS, GlDateTime.dayStartTimeStamp())
        GlRoot.glDatabase.dailyRecord.set(PATH_DAILY_TASK_HISTORY, mutableListOf< GlAnyDict >())
        GlRoot.glDatabase.save()
    }

    private fun currentTaskToHistory() {
        val currentTask = getCurrentTaskInfo()
        val currentTaskCopy = currentTask.deepCopy()
        val taskHistory = mDatabase.dailyRecord.get(PATH_DAILY_TASK_HISTORY)

        if (taskHistory is MutableList< * >) {
            @Suppress("UNCHECKED_CAST")
            (taskHistory as GlAnyStructList).add(currentTaskCopy)
        }
        else {
            mDatabase.dailyRecord.set(PATH_DAILY_TASK_HISTORY, mutableListOf(currentTaskCopy))
        }
    }

    private fun setCurrentTask(taskId: Any?, ts: Long? = null) {
        val taskIdStr: String = (taskId ?: GROUP_ID_IDLE) as String
        mDatabase.runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/taskID", taskIdStr)
        mDatabase.runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/groupID", groupOfTask(taskIdStr))
        mDatabase.runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/startTime", ts ?: System.currentTimeMillis())
    }
}