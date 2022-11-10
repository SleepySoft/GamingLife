package glcore

import glenv.GlEnv
import java.io.File
import java.util.*


object GlRoot {
    lateinit var env: GlEnv
    val glDatabase = GlDatabase()
    val glTaskModule = GlTaskModule(glDatabase)

    fun init(glEnv: GlEnv) {
        env = glEnv
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

    fun archiveTemporaryFileToDailyFolder(tempFileName: String, archiveDate: Date) {
        val srcFile = File(GlFile.glRoot(), tempFileName)
        val desFileName = GlFile.joinPaths(GlFile.glRoot(),
            getDailyFolderName(archiveDate), "${getFileNameTs()}.${srcFile.extension}")
        GlFile.copyFileAbsolute(srcFile.absolutePath, desFileName)
    }

    fun archiveTemporaryFileToDailyFolder(tempFileName: String, offsetDays: Int = 0) =
        archiveTemporaryFileToDailyFolder(tempFileName, GlDateTime.datetime(offsetDays = offsetDays))

    fun archiveJsonFilesToDailyFolder(archiveDate: Date) {
        for (fileName in GL_FILES) {
            archiveTemporaryFileToDailyFolder(fileName, archiveDate)
        }
    }
}


