package glcore

import java.lang.Long.max
import java.util.*
import kotlin.math.min


class GlDaily {
    var dailyPath: String = ""
        private set
    var dailyFile: String = ""
        private set
    val dailyData: PathDict = PathDict()

    var dailyExtraFiles = listOf< String >()

    var dailyTs: Long = 0
    var taskRecords: MutableList< TaskRecord > = mutableListOf()
    // val dailyGroupTime: MutableMap< String , Long > = mutableMapOf()

    companion object {
        fun listDailyData() : List< String > {
            val dailyFolders = mutableListOf< String >()
            val rootFolders = GlFile.listFiles("", GlFile.LIST_DIRECTORY)
            for (folder in rootFolders) {
                if (folder.startsWith(DAILY_FOLDER_TEMPLATE.removeSuffix("%s"))) {
                    dailyFolders.add(folder)
                }
            }
            return dailyFolders
        }
    }

    fun addTask(task: TaskRecord) {
        task.startTime = max(task.startTime, dailyTs)
        task.startTime = min(task.startTime, dailyTs + TIMESTAMP_COUNT_IN_DAY - 1)
        taskRecords.add(task)
        taskRecords.sortBy { it.startTime }
    }

    fun lastTask(): TaskRecord? {
        return if (taskRecords.isNotEmpty())  taskRecords.last() else null
    }

    fun loadDailyData(dateTime: Date) : Boolean {
        dailyFile = if (GlDateTime.zeroDateHMS(dateTime).time == GlDateTime.dayStartTimeStamp()) {
            GL_FILE_DAILY_RECORD
        } else {
            dailyPath = GlRoot.getDailyFolderName(dateTime)
            GlFile.joinPaths(dailyPath, GL_FILE_DAILY_RECORD)
        }

        return if (parseDailyFile(dailyFile) and collectDailyExtraFiles()) {
            GlLog.i("GlDailyStatistics - Load daily data $dateTime SUCCESS.")
            true
        } else {
            GlLog.i("GlDailyStatistics - Load daily data $dateTime FAIL.")
            false
        }
    }

    fun loadDailyData(dayOffset: Int) : Boolean {
        return loadDailyData(GlDateTime.datetime(dayOffset))
    }

    fun saveDailyData() {
        val pathDict = IGlDeclare.toAnyStructList(taskRecords)
        dailyData.set(PATH_DAILY_TASK_RECORD, pathDict)

        if (dailyTs == GlDateTime.dayStartTimeStamp()) {
            GlRoot.glDatabase.dailyRecord.set(PATH_DAILY_TASK_RECORD, pathDict)
            GlRoot.glDatabase.save()
        } else {
            // TODO: Save to archived file
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun parseDailyFile(dailyJsonPath: String) : Boolean {
        val dailyFileData: String = GlFile.loadFile(dailyFile).toString(Charsets.UTF_8)

        return if (dailyFileData.isNotEmpty()) {
            dailyData.attach(GlJson.deserializeAnyDict(dailyFileData))
            dailyData.hasUpdate = false
            parseDailyData()
        } else {
            false
        }
    }

    private fun parseDailyData() : Boolean {
        return try {
            dailyTs = (dailyData.get(PATH_DAILY_START_TS) as? Long) ?: 0
            val dailyRecord = dailyData.get(PATH_DAILY_TASK_RECORD) as? List< * >

            if ((dailyTs == 0L) || (dailyRecord == null)) {
                return false
            }

            GlLog.i("Parsing daily data ${Date(dailyTs)} ...")

            taskRecords = TaskRecord.fromAnyStructList(dailyRecord).toMutableList()
            taskRecords.sortBy { it.startTime }
            true
        } catch (e: Exception) {
            GlLog.i("Parse daily data fail - $e")
            false
        }
    }

    private fun collectDailyExtraFiles() : Boolean {
        dailyExtraFiles = GlFile.listFiles(dailyPath, GlFile.LIST_FILE)
        dailyExtraFiles = dailyExtraFiles.filter { !it.lowercase().endsWith(".json") }

        // GlLog.i("Collect daily files: $dailyExtraFiles")

        return true
    }
}