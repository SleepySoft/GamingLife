package glcore

import android.os.Build
import androidx.annotation.RequiresApi
import glenv.GlKeyPair


class GlSystemConfig() {
    private val systemConfig: PathDict = PathDict()

    private var taskTop: MutableList< TaskData > = mutableListOf()
    private var taskSub: MutableList< TaskData > = mutableListOf()
    private var taskLink: MutableMap< String , String > = mutableMapOf()        // sub id: top id

    // ---------------------------------------------------------------------------------------------

    fun loadSystemConfig() : Boolean =
        loadPathDict(GL_FILE_SYSTEM_CONFIG, systemConfig) && parseSystemConfig()

    fun saveSystemConfig() : Boolean = savePathDict(GL_FILE_SYSTEM_CONFIG, systemConfig)

    fun rebuildSystemConfig() : Boolean {
        GlLog.i("Rebuild System Config")
        systemConfig.clear()
        systemConfig.set(PATH_SYSTEM_TASK_GROUP_TOP, TASK_GROUP_TOP_PRESET.toMutableList())
        systemConfig.set(PATH_SYSTEM_TASK_GROUP_SUB, mutableListOf< TaskData >())
        systemConfig.set(PATH_SYSTEM_TASK_GROUP_LINK, mutableMapOf< String, String >())
        systemConfig.set(PATH_SYSTEM_TASK_RECORD_THRESHOLD, TIME_DEFAULT_TASK_RECORD_THRESHOLD)
        return saveSystemConfig()
    }

    private fun parseSystemConfig(): Boolean {
        return try {
            val taskTopList = systemConfig.get(PATH_SYSTEM_TASK_GROUP_TOP) as List< * >
            taskTop = TaskData.fromAnyStructList(taskTopList).toMutableList()

            val taskSubList = systemConfig.get(PATH_SYSTEM_TASK_GROUP_SUB) as List< * >
            taskSub = TaskData.fromAnyStructList(taskSubList).toMutableList()

            val taskLinkDict = systemConfig.get(PATH_SYSTEM_TASK_GROUP_LINK) as MutableMap< *, * >
            taskLink = toStrStruct(taskLinkDict)

            true

        } catch (e: Exception) {
            GlLog.e("Parse System Config FAIL.")
            GlLog.e(e.stackTraceToString())
            false
        } finally {

        }
    }

    // -------------------------------------- Simple Property --------------------------------------

    var taskRecordThreshold: Long
        set(value) = systemConfig.set(PATH_SYSTEM_TASK_RECORD_THRESHOLD, value).let {}
        get() = (systemConfig.get(PATH_SYSTEM_TASK_RECORD_THRESHOLD) as? Long) ?: TIME_DEFAULT_TASK_RECORD_THRESHOLD.toLong()


    // ------------------------------------------- Task --------------------------------------------

    // ========================== Gets ==========================

    fun getTopTasks(): List< TaskData > = taskTop

    fun getSubTasks(): List< TaskData > = taskSub

    fun getTaskData(glId: String) : TaskData? = getTopTaskData(glId) ?: getSubTaskData(glId)

    fun getTopTaskData(glId: String) : TaskData? {
        val taskData = taskTop.filter { it.id == glId }
        return if (taskData.isNotEmpty()) taskData[0] else null
    }

    fun getSubTaskData(glId: String) : TaskData? {
        val taskData = taskTop.filter { it.id == glId }
        return if (taskData.isNotEmpty()) taskData[0] else null
    }

    fun getTopGroupOfTask(glId: String): String =
        getTopTaskData(glId)?.id ?: taskLink[glId] ?: GROUP_ID_IDLE

    fun getTaskInTopGroup(topTaskId: String): List< TaskData > {
        val taskData = mutableListOf< TaskData >()
        if (getTopTaskData(topTaskId) != null) {
            for ((k, v) in taskLink) {
                if (v == topTaskId) {
                    getSubTaskData(k)?.run {
                        taskData.add(this)
                    }
                }
            }
        }
        return taskData
    }

    // ========================== Sets ==========================

/*    @RequiresApi(Build.VERSION_CODES.N)
    fun removeSubTask(glId: String) {
        taskSub.removeIf { it.id == glId }
    }

    fun updateTaskData(taskData: TaskData) {
        val existsTaskData = getTaskData(taskData.id)
        if (existsTaskData == null) {
            taskSub.add(taskData)
        } else {
            taskData.run {
                this.name = taskData.name
                this.color = taskData.color
            }
        }
    }

    fun setTaskSubGroup(glId: String, groupId: String) {
        if ((getSubTaskData(glId) != null) &&
            (getTopTaskData(groupId) != null)) {
            taskLink[glId] = groupId
        }
    }*/

    // ------------------------------------------- GLID --------------------------------------------

    var GLID: String
        set(value) = systemConfig.set(PATH_SYSTEM_GLID, value).let {  }
        get() = (systemConfig.get(PATH_SYSTEM_GLID) as String?) ?: ""

    var publicKey: String
        set(value) = systemConfig.set(PATH_SYSTEM_PUBLIC_KEY, value).let {  }
        get() = (systemConfig.get(PATH_SYSTEM_PUBLIC_KEY) as String?) ?: ""

    var privateKey: String
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            val localKeyPair = GlKeyPair().apply {
                if (!loadLocalKeyPair(LOCAL_KEYPAIR_MAIN_NAME)) {
                    generateLocalKeyPair(LOCAL_KEYPAIR_MAIN_NAME)
                }
            }
            val privateKeyEncrypted = localKeyPair.privateKeyEncrypt(value.toByteArray())
            systemConfig.set(PATH_SYSTEM_PUBLIC_KEY, privateKeyEncrypted)
        }
        get() = (systemConfig.get(PATH_SYSTEM_PUBLIC_KEY) as String?) ?: ""
}