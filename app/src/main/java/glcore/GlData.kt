package glcore


class GlData(private val mDatabase: GlDatabase) {

    fun init() {
        groupDataFromDatabase()
        checkInitRuntimeData()
    }

    // ---------------------------------------- Task Group -----------------------------------------

    // ------------------- Data, Load and Write -------------------

    var mTaskGroupTop: MutableMap< String, String > = mutableMapOf()         // id: name
    var mTaskGroupSub: MutableMap< String, String > = mutableMapOf()         // id: name
    var mTaskGroupLink: MutableMap< String, String > = mutableMapOf()        // sub id: top id

    private fun groupDataFromDatabase() {
/*        mTaskGroupTop = mDatabase.systemConfig.getDictStr(
            PATH_TASK_GROUP_TOP) ?: TASK_GROUP_TOP_PRESET.toMutableMap()
        mTaskGroupSub = mDatabase.systemConfig.getDictStr(
            PATH_TASK_GROUP_SUB) ?: mutableMapOf()
        mTaskGroupLink = mDatabase.systemConfig.getDictStr(
            PATH_TASK_GROUP_LINK) ?: mutableMapOf()*/
    }

    private fun groupDataToDatabase() {
        mDatabase.systemConfig.put(PATH_TASK_GROUP_TOP, mTaskGroupTop)
        mDatabase.systemConfig.put(PATH_TASK_GROUP_SUB, mTaskGroupSub)
        mDatabase.systemConfig.put(PATH_TASK_GROUP_LINK, mTaskGroupLink)
    }

    // --------------------------- Gets ---------------------------

    fun getTaskGroupTop(): Map< String, String > = mTaskGroupTop
    fun getTaskGroupSub(): Map< String, String > = mTaskGroupSub

    fun nameOfTask(glId: String): String = (mTaskGroupTop[glId] ?: mTaskGroupSub[glId]) ?: ""

    fun subTaskOfRoot(glId: String): List< String > {
        val subTasks = mutableListOf< String >()
        if (mTaskGroupTop.containsKey(glId))
        {
            for ((k, v) in mTaskGroupLink) {
                if (v == glId) {
                    subTasks.add(k)
                }
            }
        }
        return subTasks
    }

/*    fun getGroupColor(glId: String) : Long {
        val color = mDatabase.systemConfig.get(PATH_TASK_GROUP_COLOR + "/${glId}")
        return if (color is Long) color else (TASK_GROUP_COLOR_PRESET[glId] ?: 0x00FFFFFF)
    }*/

    fun getCurrentTaskData() : Map< String , Any > {
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
                    return currentTaskData
                }
            }
        }
        catch (e: Exception) {
            val currentTaskData = TASK_RECORD_TEMPLATE.toMutableMap().apply {
                this["startTime"] = System.currentTimeMillis()
            }
            mDatabase.runtimeData.put(PATH_CURRENT_TASK, currentTaskData, true)
            return currentTaskData
        }
    }

    // --------------------------- Sets ---------------------------

    fun renameTaskRootGroup(glId: String, newName: String) {
        if (mTaskGroupTop.containsKey(glId)) {
            mTaskGroupTop[glId] = newName
            groupDataToDatabase()
        }
    }

    fun updateTaskSubGroup(glId: String, subName: String, belongsRootGroupId: String) {
        if (belongsRootGroupId.trim().isEmpty()) {
            if (mTaskGroupSub.containsKey(glId)) {
                mTaskGroupSub.remove(glId)
            }
            if (mTaskGroupLink.containsKey(glId)) {
                mTaskGroupLink.remove(glId)
            }
        }
        else if (mTaskGroupTop.containsKey(belongsRootGroupId))
        {
            mTaskGroupSub[glId] = subName
            mTaskGroupLink[glId] = belongsRootGroupId
        }
        groupDataToDatabase()
    }

    // ------------------------------------------ Others -------------------------------------------

    private fun checkInitRuntimeData() {
        checkInitCurrentTask()
    }

    private fun checkInitCurrentTask() {
    }
}