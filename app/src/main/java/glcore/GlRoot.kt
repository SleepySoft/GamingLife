package glcore

object GlRoot {
    val glContext = GlContext()
    val glDatabase = GlDatabase()
    val glData = GlData(glDatabase)
    val glTimeModule = GlTimeModule()

    fun init() {
        glDatabase.init()
        glData.init()
        
        glTimeModule.init(glContext)
    }
}