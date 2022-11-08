package glcore

import java.io.File


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

    fun getDailyFolderName(offset: Int)  =
        DAILY_FOLDER_TEMPLATE.format(
            GlDateTime.formatToDay(GlDateTime.datetime(offsetDays = offset)))

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


