package glcore

import android.os.Build
import androidx.annotation.RequiresApi
import glenv.GlKeyPair
import java.security.MessageDigest
import java.util.*


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
            try {
                val localKeyPair = GlKeyPair().apply {
                    if (!loadLocalKeyPair(LOCAL_KEYPAIR_MAIN_NAME)) {
                        generateLocalKeyPair(LOCAL_KEYPAIR_MAIN_NAME)
                    }
                }

                val privateKeyBytes = Base64.getDecoder().decode(value)

                val md = MessageDigest.getInstance("SHA-256")
                val privateKeyHash = md.digest(privateKeyBytes)
                val privateKeyHashBase64 = Base64.getEncoder().encodeToString(privateKeyHash)

                val privateKeyEncrypted = localKeyPair.publicKeyEncrypt(privateKeyBytes)
                val privateKeyEncryptedBase64 = Base64.getEncoder().encodeToString(privateKeyEncrypted)

                systemConfig.set(PATH_SYSTEM_PRIVATE_KEY, privateKeyEncryptedBase64)
                systemConfig.set(PATH_SYSTEM_PRIVATE_KEY_HASH, privateKeyHashBase64)
            } catch (e: Exception) {
                GlLog.e("Private Key storage FAIL.")
                GlLog.e(e.stackTraceToString())
            } finally {

            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            return try {
                val localKeyPair = GlKeyPair()
                if (!localKeyPair.loadLocalKeyPair(LOCAL_KEYPAIR_MAIN_NAME)) {
                    throw java.lang.Exception("No Local Key Pair. The Private Key cannot be decrypted.")
                }

                val privateKeyHashBase64 = (systemConfig.get(PATH_SYSTEM_PRIVATE_KEY_HASH) as String?) ?: ""
                val privateKeyEncryptedBase64 = (systemConfig.get(PATH_SYSTEM_PRIVATE_KEY) as String?) ?: ""

                val privateKeyHash = Base64.getDecoder().decode(privateKeyHashBase64)
                val privateKeyEncrypted = Base64.getDecoder().decode(privateKeyEncryptedBase64)

                val privateKeyBytes = localKeyPair.privateKeyDecrypt(privateKeyEncrypted)
                val md = MessageDigest.getInstance("SHA-256")
                val privateKeyBytesHash = md.digest(privateKeyBytes)

                if (!privateKeyHash.equals(privateKeyBytesHash)) {
                    throw java.lang.Exception("Decrypted Private Key Hash Error.")
                }

                Base64.getEncoder().encodeToString(privateKeyBytes)
            } catch (e: Exception) {
                GlLog.e("Private Key base64 FAIL.")
                GlLog.e(e.stackTraceToString())
                ""
            } finally {

            }
        }
}