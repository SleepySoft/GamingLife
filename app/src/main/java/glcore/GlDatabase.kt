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

    // ----------------------------------------------------------

    fun init(): Boolean {
        return true
    }

    fun save(): Boolean {
        if (runtimeData.hasUpdate) {
            doSave(FILE_RUNTIME_DATA, GlJson.serializeAnyDict(runtimeData.rootDict))
            runtimeData.hasUpdate = false

        }

        if (dailyRecord.hasUpdate) {
            doSave(FILE_DAILY_RECORD, GlJson.serializeAnyDict(dailyRecord.rootDict))
            dailyRecord.hasUpdate = false
        }

        if (systemConfig.hasUpdate) {
            doSave(FILE_SYSTEM_CONFIG, GlJson.serializeAnyDict(systemConfig.rootDict))
            systemConfig.hasUpdate = false
        }

        return true
    }

    fun load(): Boolean {
        runtimeData.attach(GlJson.deserializeAnyDict(doLoad(FILE_RUNTIME_DATA)))
        dailyRecord.attach(GlJson.deserializeAnyDict(doLoad(FILE_DAILY_RECORD)))
        systemConfig.attach(GlJson.deserializeAnyDict(doLoad(FILE_SYSTEM_CONFIG)))
        return true
    }

    private fun doSave(fileName: String, fileContent: String) {
        val ctx: Context = GlApplication.applicationContext()
        File(ctx.filesDir.absolutePath, fileName).writeText(fileContent)
    }

    private fun doLoad(fileName: String) : String {
        val ctx: Context = GlApplication.applicationContext()
        val file = File(ctx.filesDir.absolutePath, fileName)
        return file.readText()
    }

    // ------------------------------------------------------------

/*    private fun toJson(dict: GlAnyStruct) {
        val json: String = Json.encodeToString< GlAnyStruct >(dict)
    }

    private fun persistsJson() {

    }*/
}


