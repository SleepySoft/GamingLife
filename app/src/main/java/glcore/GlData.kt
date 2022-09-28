package glcore


class GlData(private val mDatabase: GlDatabase) {

    fun init() {
        groupDataFromDatabase()
    }

    // ---------------------------------------- Task Group -----------------------------------------

    // ------------------- Data, Load and Write -------------------

    var mTaskGroupTop: MutableMap< String, String > = mutableMapOf()         // id: name
    var mTaskGroupSub: MutableMap< String, String > = mutableMapOf()         // id: name
    var mTaskGroupLink: MutableMap< String, String > = mutableMapOf()        // sub id: top id

    private fun groupDataFromDatabase() {
        mTaskGroupTop = mDatabase.metaData.getDictStr(META_TASK_GROUP_TOP) ?:
                                                    TASK_GROUP_TOP_PRESET.toMutableMap()
        mTaskGroupSub = mDatabase.metaData.getDictStr(META_TASK_GROUP_SUB) ?: mutableMapOf()
        mTaskGroupLink = mDatabase.metaData.getDictStr(META_TASK_GROUP_LINK) ?: mutableMapOf()
    }

    private fun groupDataToDatabase() {
        mDatabase.metaData.put(META_TASK_GROUP_TOP, mTaskGroupTop)
        mDatabase.metaData.put(META_TASK_GROUP_SUB, mTaskGroupSub)
        mDatabase.metaData.put(META_TASK_GROUP_LINK, mTaskGroupLink)
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
}