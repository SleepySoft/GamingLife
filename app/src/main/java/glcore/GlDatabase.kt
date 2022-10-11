package glcore


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
}


