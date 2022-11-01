package glcore

import android.content.Context
import android.os.Environment
import java.io.File
import java.util.*


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

    fun getDailyFolderName(offset: Int) : String {
        return DAILY_FOLDER_TEMPLATE.format(
            GlDateTime.formatToDay(GlDateTime.datetime(offsetDays = offset)))
    }
}