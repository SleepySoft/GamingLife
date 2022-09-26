package glcore


val PRESET_TOP_CLASS = mapOf(
    "5084b76d-4e75-4c44-9786-bdf94075f94d" to "放松",
    "f9fb401a-dc28-463f-92a6-0d30bd8730bb" to "玩乐",
    "3e9fd903-9c51-4301-b610-715205983573" to "生活",
    "11000041-0376-4876-9efa-8a6a7028140d" to "工作",
    "1841978a-3adc-413a-a9ae-a34e019205f8" to "学习",
    "fa94a546-beeb-4570-b266-c066a4a31233" to "创作",
)


const val SEPERATOR = "/"
const val META_ROOT = "GL_META_ROOT"
const val DAILY_ROOT = "GL_DAILY_ROOT"
const val CONFIG_ROOT = "GL_CONFIG_ROOT"


class GlDatabase: GlObject() {

    val mMetaData = PathDict().apply {
        this.separator = SEPERATOR
    }

    val mDailyRecord = PathDict().apply {
        this.separator = SEPERATOR
    }

    val mGlobalConfig = PathDict().apply {
        this.separator = SEPERATOR
    }

    // ----------------------------------------------------------

    fun init(): Boolean {
        return true
    }

    fun save(): Boolean {
        return true
    }

    fun load(): Boolean {
        return true
    }

    // ----------------------------------------------------------

    fun getMetaData(path: String): Any? {
        return mMetaData.get(path)
    }

    fun setMetaData(path: String, data: Any) {
        mMetaData.put(path, data)
    }

    fun updateMetaData(path: String, data: MutableMap<String, Any>) {
        mMetaData.put(path, data, true)
    }

    // ----------------------------------------------------------

    fun getDailyRecord(path: String): Any? {
        return mDailyRecord.get(path)
    }

    fun setDailyRecord(path: String, data: Any) {
        mDailyRecord.put(path, data)
    }

    fun updateDailyRecord(path: String, data: MutableMap<String, Any>) {
        mDailyRecord.put(path, data, true)
    }

    // ----------------------------------------------------------

    fun getGlobalConfig(path: String): Any? {
        return mGlobalConfig.get(path)
    }

    fun setGlobalConfig(path: String, data: Any) {
        mGlobalConfig.put(path, data)
    }

    fun updateGlobalConfig(path: String, data: MutableMap<String, Any>) {
        mGlobalConfig.put(path, data, true)
    }
}


