package glcore


const val SEPARATOR = "/"


class GlDatabase: GlObject() {

    val metaData = PathDict().apply {
        this.separator = SEPARATOR
    }

    val dailyRecord = PathDict().apply {
        this.separator = SEPARATOR
    }

    val globalConfig = PathDict().apply {
        this.separator = SEPARATOR
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
}


