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

    val environment = PathDict().apply {
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
                savePathDict(GL_FILE_ENVIRONMENT, environment)
    }

    private fun load(): Boolean {
        return  loadPathDict(GL_FILE_RUNTIME_DATA, runtimeData) and
                loadPathDict(GL_FILE_DAILY_RECORD, dailyRecord) and
                loadPathDict(GL_FILE_SYSTEM_CONFIG, systemConfig) and
                loadPathDict(GL_FILE_ENVIRONMENT, environment)
    }

    private fun savePathDict(fileName: String, pathDict: PathDict, force: Boolean = false) : Boolean {
        var ret = false
        if (pathDict.hasUpdate || force) {
            val fileContent: String = GlJson.serializeAnyDict(pathDict.rootDict)
            ret = GlFile.saveFile(fileName, fileContent.toByteArray(Charsets.UTF_8))
            pathDict.hasUpdate = !ret
        }
        return ret
    }

    private fun loadPathDict(fileName: String, pathDict: PathDict) : Boolean {
        val fileContent: String = GlFile.loadFile(fileName).toString(Charsets.UTF_8)
        pathDict.attach(GlJson.deserializeAnyDict(fileContent))
        pathDict.hasUpdate = false
        return true
    }

    // ------------------------------------------------------------

/*    private fun toJson(dict: GlAnyStruct) {
        val json: String = Json.encodeToString< GlAnyStruct >(dict)
    }

    private fun persistsJson() {

    }*/
}


