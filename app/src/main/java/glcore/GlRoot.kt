package glcore

object GlRoot {
    val glDatabase = GlDatabase()
    val glData = GlData(glDatabase)
    val glTimeModule = GlTimeModule()

    fun init() {

    }
}