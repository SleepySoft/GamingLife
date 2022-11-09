package glcore

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

    fun getFileNameTs(date: Date? = null) : String =
        GlDateTime.formatToMSec(date ?: GlDateTime.datetime())

    fun getFileNameTs(offsetDays: Int) : String =
        getFileNameTs(GlDateTime.datetime(offsetDays = offsetDays))

    fun getDailyFolderName(date: Date) : String =
        DAILY_FOLDER_TEMPLATE.format(GlDateTime.formatToDay(date))

    fun getDailyFolderName(offsetDays: Int) =
        getDailyFolderName(GlDateTime.datetime(offsetDays = offsetDays))

    fun saveContentToDailyFolder(content: ByteArray, fileSuffix: String, dayOffset: Int = 0) {
        val desFileName = GlFile.joinPaths(
            getDailyFolderName(dayOffset), "${getFileNameTs()}.$fileSuffix")
        GlFile.saveFile(desFileName, content)
    }

    fun loadContentFromDailyFolder() {

    }

    fun archiveTemporaryFileToDailyFolder(tempFileName: String, dayOffset: Int = 0) {
        val srcFile = File(GlFile.glRoot(), tempFileName)
        val desFileName = GlFile.joinPaths(GlFile.glRoot(),
            getDailyFolderName(dayOffset), "${getFileNameTs()}.${srcFile.extension}")
        GlFile.copyFileAbsolute(srcFile.absolutePath, desFileName)
    }
}


