package glcore

import android.os.Environment
import glenv.GlEnv
import java.io.File
import java.util.*


object GlRoot {
    const val ERROR_NONE = 0
    const val ERROR_INITED = 1
    const val ERROR_INVALID_ENV = 2

    const val ERROR_FILE_PERMISSION = 16
    const val ERROR_SYSTEM_CONFIG = 32
    const val ERROR_DAILY_RECORD = 64

    private var mInited = false

    lateinit var env: GlEnv

    val glService = GlService()
    val dailyRecord = GlDailyRecord()
    val systemConfig = GlSystemConfig()

    fun init(glEnv: GlEnv) : Int {
        if (mInited) {
            return ERROR_INITED
        }
        if (!glEnv.valid) {
            return ERROR_INVALID_ENV
        }

        env = glEnv                     // GlFile can be used after here.

        if (!ensureStorage()) {
            return ERROR_FILE_PERMISSION
        }

        var errorCode = ERROR_NONE

        if (!(systemConfig.loadSystemConfig() || systemConfig.rebuildSystemConfig())) {
            errorCode = errorCode and ERROR_SYSTEM_CONFIG
        }

        if (!(dailyRecord.loadDailyRecord(0) || dailyRecord.newDailyRecord())) {
            errorCode = errorCode and ERROR_DAILY_RECORD
        }

        if (errorCode == ERROR_NONE) {
            GlLog.e("System init successful.")
            mInited = true
        } else {
            GlLog.e("System init fail.")
        }

        return errorCode
    }

    /**
     * Format the DateTime as GL's standard timestamp style file name.
     *
     * @param date The datetime you specified or null to use current datetime for.
     * @return The timestamp style filename which is format to second. Not includes the full path and suffix.
     */
    fun getFileNameTs(date: Date? = null) : String =
        GlDateTime.formatToMSec(date ?: GlDateTime.datetime())

    /**
     * Get the daily folder name by the date. Only name, not the full path.
     *
     * @param date The date to specify the daily folder.
     * @return The daily folder's name.
     */
    fun getDailyFolderName(date: Date) : String =
        DAILY_FOLDER_TEMPLATE.format(GlDateTime.formatToDay(date))

    /**
     * Get the daily folder name by the day offset of today. Only name, not the full path.
     *
     * @param offsetDays The date that specified by day offset of today to specify the daily folder.
     * @return The daily folder's name.
     */
    fun getDailyFolderName(offsetDays: Int) : String =
        getDailyFolderName(GlDateTime.datetime(offsetDays = offsetDays))

    /**
     * Write byte array to daily folder, with the name by timestamp.
     *
     * @param content The content that you want to write into file.
     * @param fileSuffix The file name suffix, will concat to the timestamp file name.
     * @param offsetDays The date that specified by day offset of today to specify the daily folder.
     */
    fun saveContentToDailyFolder(content: ByteArray, fileSuffix: String, offsetDays: Int = 0) {
        val desFileName = GlFile.joinPaths(
            getDailyFolderName(offsetDays), "${getFileNameTs()}.$fileSuffix")
        GlFile.saveFile(desFileName, content)
    }

    /**
     * Copy all GL json files to specify daily folder and keep their names
     *
     * @param archiveDate The date to specify the daily folder.
     */
    fun archiveJsonFilesToDailyFolder(archiveDate: Date) {
        for (fileName in GL_FILES) {
            GlFile.copyFileAbsolute(
                GlFile.absPath(fileName),
                GlFile.absPath(getDailyFolderName(archiveDate), fileName))
        }
    }

    /**
     * Copy a file in GL root path to specify daily folder, with renaming by timestamp.
     *
     * @param fileName The relative file name that you want to copy
     * @param archiveDate The date to specify the daily folder.
     */
    fun archiveRootPathFileToDailyFolder(fileName: String, archiveDate: Date) {
        val srcFile = File(GlFile.glRoot(), fileName)
        val desFileName = GlFile.joinPaths(GlFile.glRoot(),
            getDailyFolderName(archiveDate), "${getFileNameTs()}.${srcFile.extension}")
        GlFile.copyFileAbsolute(srcFile.absolutePath, desFileName)
    }

    /**
     * Copy a file in GL root path to specify daily folder, with renaming by timestamp.
     *
     * @param fileName The relative file name that you want to copy
     * @param offsetDays The date that specified by day offset of today to specify the daily folder.
     */
    fun archiveRootPathFileToDailyFolder(fileName: String, offsetDays: Int = 0) =
        archiveRootPathFileToDailyFolder(fileName, GlDateTime.datetime(offsetDays = offsetDays))

    private fun ensureStorage() : Boolean {
        val file = File(GlFile.glRoot())
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return false
            }
        }
        return file.isDirectory && file.canRead() && file.canWrite()
    }
}


