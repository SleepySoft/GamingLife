package glcore


const val SEPARATOR = "/"


class GlDatabase {

    val runtimeData = PathDict().apply {
        this.separator = SEPARATOR
    }

    val dailyRecord = PathDict().apply {
        this.separator = SEPARATOR
    }

    val systemConfig = PathDict().apply {
        this.separator = SEPARATOR
    }

    val environmentContext = PathDict().apply {
        this.separator = SEPARATOR
    }

    // ----------------------------------------------------------

    fun init(): Boolean {
        return load()
    }

    fun save(): Boolean {
        return  savePathDict(GL_FILE_RUNTIME_DATA, runtimeData) and
                savePathDict(GL_FILE_DAILY_RECORD, dailyRecord) and
                savePathDict(GL_FILE_SYSTEM_CONFIG, systemConfig) and
                savePathDict(GL_FILE_ENVIRONMENT_CONTEXT, environmentContext)
    }

    private fun load(): Boolean {
        return  (trueOrNull(loadPathDict(GL_FILE_RUNTIME_DATA, runtimeData)) ?: initRuntimeData()) and
                (trueOrNull(loadPathDict(GL_FILE_DAILY_RECORD, dailyRecord)) ?: initDailyRecord()) and
                (trueOrNull(loadPathDict(GL_FILE_SYSTEM_CONFIG, systemConfig)) ?: initSystemConfig()) and
                (trueOrNull(loadPathDict(GL_FILE_ENVIRONMENT_CONTEXT, environmentContext)) ?: initEnvironmentContext())
    }

    fun savePathDict(fileName: String, pathDict: PathDict, force: Boolean = false) : Boolean {
        var ret = false
        if (pathDict.hasUpdate || force) {
            val fileContent: String = GlJson.serializeAnyDict(pathDict.rootDict)
            ret = GlFile.saveFile(fileName, fileContent.toByteArray(Charsets.UTF_8))
            pathDict.hasUpdate = !ret
        }
        return ret
    }

    fun loadPathDict(fileName: String, pathDict: PathDict) : Boolean {
        val fileContent: String = GlFile.loadFile(fileName).toString(Charsets.UTF_8)
        pathDict.attach(GlJson.deserializeAnyDict(fileContent))
        pathDict.hasUpdate = false
        return fileContent.isNotEmpty()
    }

    // ------------------------------------------------------------

    private fun initRuntimeData() : Boolean {
        GlLog.i("Init RuntimeData")
        runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/taskID", GROUP_ID_IDLE)
        runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/groupID", GROUP_ID_IDLE)
        runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/startTime", GlDateTime.datetime())
        // return savePathDict(GL_FILE_RUNTIME_DATA, runtimeData)
        return true
    }

    private fun initDailyRecord() : Boolean {
        GlLog.i("Init Daily Record")
        dailyRecord.set(PATH_DAILY_START_TS, GlDateTime.dayStartTimeStamp())
        dailyRecord.set(PATH_DAILY_TASK_RECORD, mutableListOf< GlAnyDict >())
        // return savePathDict(GL_FILE_DAILY_RECORD, dailyRecord)
        return true
    }

    private fun initSystemConfig() : Boolean {
        GlLog.i("Init System Config")
        systemConfig.set(PATH_SYSTEM_TASK_GROUP_TOP, TASK_GROUP_TOP_PRESET.toMutableMap())
        systemConfig.set(PATH_SYSTEM_TASK_GROUP_SUB, mutableMapOf< String, GlAnyStruct >())
        systemConfig.set(PATH_SYSTEM_TASK_GROUP_LINK, mutableMapOf< String, GlAnyStruct >())
        // savePathDict(GL_FILE_SYSTEM_CONFIG, systemConfig)
        return true
    }

    private fun initEnvironmentContext() : Boolean {
        GlLog.i("Init Environment Context")
        // savePathDict(GL_FILE_ENVIRONMENT_CONTEXT, environmentContext)
        return true
    }
}


