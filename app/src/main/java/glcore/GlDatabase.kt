package glcore
import kotlinx.serialization.*
import kotlinx.serialization.json.*


const val SEPARATOR = "/"


class GlDatabase: GlObject() {

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

        }

        if (dailyRecord.hasUpdate) {

        }

        if (systemConfig.hasUpdate) {

        }

        return true
    }

    fun load(): Boolean {
        return true
    }

    // ------------------------------------------------------------

    private fun toJson(dict: GlAnyStruct) {
        val json: String = Json.encodeToString< GlAnyStruct >(dict)
    }

    private fun persistsJson() {

    }
}


