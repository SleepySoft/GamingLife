package glcore


object GlRoot {
    val glContext = GlContext()
    val glDatabase = GlDatabase()
    val glTaskModule = GlTaskModule(glDatabase)

    fun init() {
        GlAudioRecorder.init()
        glDatabase.init()
        glTaskModule.init()
    }

    fun getFileNameTs() : String  {
        return GlDateTime.formatToMSec(GlDateTime.datetime())
    }

    fun getDailyFolder(offset: Int) : String {
        return DAILY_FOLDER_TEMPLATE.format(
            GlDateTime.formatToDay(GlDateTime.datetime(offsetDays = offset)))
    }
}