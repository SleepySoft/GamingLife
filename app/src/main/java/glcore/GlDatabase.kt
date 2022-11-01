package glcore

import java.io.File
import android.content.Context


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
        return  savePathDict(FILE_RUNTIME_DATA, runtimeData) and
                savePathDict(FILE_DAILY_RECORD, dailyRecord) and
                savePathDict(FILE_SYSTEM_CONFIG, systemConfig) and
                savePathDict(FILE_ENVIRONMENT, environment)
    }

    private fun load(): Boolean {
        return  loadPathDict(FILE_RUNTIME_DATA, runtimeData) and
                loadPathDict(FILE_DAILY_RECORD, dailyRecord) and
                loadPathDict(FILE_SYSTEM_CONFIG, systemConfig) and
                loadPathDict(FILE_ENVIRONMENT, environment)
    }

    private fun savePathDict(fileName: String, pathDict: PathDict, force: Boolean = false) : Boolean {
        if (pathDict.hasUpdate || force) {
            val fileContent: String = GlJson.serializeAnyDict(pathDict.rootDict)
            GlFile.saveFile(fileName, fileContent.toByteArray())
            pathDict.hasUpdate = false
        }
        return true
    }

    private fun loadPathDict(fileName: String, pathDict: PathDict) : Boolean {
        val fileContent: String = GlFile.loadFile(fileName).toString()
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


