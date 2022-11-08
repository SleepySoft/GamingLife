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

    fun getFileNameTs(date: Date? = null) : String  {
        return GlDateTime.formatToMSec(date ?: GlDateTime.datetime())
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

    fun checkSettleDailyData() {
        val tsData: Any? = glDatabase.dailyRecord.get(PATH_DAILY_START_TS)
        if (tsData is Long) {
            val dailyStartTs: Long = tsData
            val todayStartTs: Long = GlDateTime.dayStartTimeStamp()
            if (todayStartTs > dailyStartTs) {
                settleDailyData()
            }
        }
        else {
            createNewDayDailyData()
        }
    }

    fun settleDailyData() {

    }

    fun createNewDayDailyData() {
        glDatabase.dailyRecord.clear()
        glDatabase.dailyRecord.set(PATH_DAILY_START_TS, GlDateTime.dayStartTimeStamp())
        glDatabase.dailyRecord.set(PATH_DAILY_TASK_HISTORY, mutableListOf< GlAnyDict >())
        glDatabase.save()
    }
}


