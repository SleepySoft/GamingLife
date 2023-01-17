package glcore

import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.Long.max
import java.util.*
import kotlin.math.min


class GlDailyRecord {

    companion object {
        fun listArchivedDailyData() : List< String > {
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

    var dailyPath: String = ""
        private set
    var dailyFile: String = ""
        private set
    val dailyRecord: PathDict = PathDict()

    var dailyExtraFiles = listOf< String >()

    var dailyTs: Long = 0
    var taskRecords: MutableList< TaskRecord > = mutableListOf()
    // val dailyGroupTime: MutableMap< String , Long > = mutableMapOf()

    // --------------------------------------- New Save Load ---------------------------------------

    fun newDailyRecord() : Boolean {
        GlLog.i("New daily data.")

        dailyPath = GlRoot.getDailyFolderName(0)
        dailyFile = GL_FILE_DAILY_RECORD

        // Build an empty path for current day. So the daily browse will list today.
        GlFile.buildPath(dailyPath)

        // Init data
        dailyRecord.clear()
        dailyRecord.set(PATH_DAILY_START_TS, GlDateTime.dayStartTimeStamp())
        dailyRecord.set(PATH_DAILY_TASK_RECORD, mutableListOf< GlAnyDict >())

        dailyExtraFiles = listOf()
        dailyTs = GlDateTime.dayStartTimeStamp()
        taskRecords = mutableListOf()

        return saveDailyRecord()
    }

    fun initDailyRecord() {
        dailyPath = ""
        dailyFile = ""
        dailyRecord.clear()
        dailyExtraFiles = listOf()
        dailyTs = 0
        taskRecords = mutableListOf()
    }

    fun loadDailyRecord(dateTime: Date) : Boolean {
        initDailyRecord()

        dailyPath = GlRoot.getDailyFolderName(dateTime)
        dailyFile = if (GlDateTime.zeroDateHMS(dateTime).time == GlDateTime.dayStartTimeStamp()) {
            GL_FILE_DAILY_RECORD
        } else {
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

    fun loadDailyRecord(dayOffset: Int) : Boolean {
        return loadDailyRecord(GlDateTime.datetime(dayOffset))
    }

    fun saveDailyRecord() : Boolean {
        val pathDict = IGlDeclare.toAnyStructList(taskRecords)
        dailyRecord.set(PATH_DAILY_TASK_RECORD, pathDict)
        return savePathDict(dailyFile, dailyRecord)

/*        if (dailyTs == GlDateTime.dayStartTimeStamp()) {
            GlRoot.glDatabase.dailyRecord.set(PATH_DAILY_TASK_RECORD, pathDict)
            GlRoot.glDatabase.save()
        } else {
            // TODO: Save to archived file
        }*/
    }

    // ---------------------------------------- Task Record ----------------------------------------

    fun addTask(task: TaskRecord) {
        task.startTime = max(task.startTime, dailyTs)
        task.startTime = min(task.startTime, dailyTs + TIMESTAMP_COUNT_IN_DAY - 1)
        taskRecords.add(task)
        reshapeTaskRecords()
    }
    
    @RequiresApi(Build.VERSION_CODES.N)
    fun removeTask(uuid: String) {
        taskRecords.removeIf { it.uuid == uuid }
    }

    fun lastTask(): TaskRecord? {
        return if (taskRecords.isNotEmpty())  taskRecords.last() else null
    }

    fun groupStatistics() : Map< String , Long > {
        /*
        * Return each task group lasting time. The key is top level group id. Value is the lasting time in ms.
        */
        return mutableMapOf< String , Long >().apply {
            var prevTask: TaskRecord? = null
            for (taskRecord in taskRecords) {
                prevTask?.let {
                    this[it.groupID] = (this[it.groupID] ?: 0) + (taskRecord.startTime - it.startTime)
                }
                prevTask = taskRecord
            }
            prevTask?.let {
                var  dayLimit = GlDateTime.datetime().time - dailyTs
                if ((dayLimit > TIMESTAMP_COUNT_IN_DAY) || (dayLimit < 0)) {
                     dayLimit = TIMESTAMP_COUNT_IN_DAY.toLong()
                }
                this[it.groupID] = (this[it.groupID] ?: 0) + (dailyTs + dayLimit - it.startTime)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun parseDailyFile(dailyJsonPath: String) : Boolean {
        return loadPathDict(dailyJsonPath, dailyRecord) && parseDailyData()

/*        val dailyFileData: String = GlFile.loadFile(dailyFile).toString(Charsets.UTF_8)

        return if (dailyFileData.isNotEmpty()) {
            dailyData.attach(GlJson.deserializeAnyDict(dailyFileData))
            dailyData.hasUpdate = false
            parseDailyData()
        } else {
            false
        }*/
    }

    private fun parseDailyData() : Boolean {
        return try {
            dailyTs = (dailyRecord.get(PATH_DAILY_START_TS) as? Long) ?: 0
            val dailyRecord = dailyRecord.get(PATH_DAILY_TASK_RECORD) as? List< * >

            if ((dailyTs == 0L) || (dailyRecord == null)) {
                return false
            }

            GlLog.i("Parsing daily data ${Date(dailyTs)} ...")

            taskRecords = TaskRecord.fromAnyStructList(dailyRecord).toMutableList()
            reshapeTaskRecords()
            true
        } catch (e: Exception) {
            GlLog.i("Parse Daily Data FAIL.")
            GlLog.e(e.stackTraceToString())
            false
        }
    }

    private fun collectDailyExtraFiles() : Boolean {
        dailyExtraFiles = GlFile.listFiles(dailyPath, GlFile.LIST_FILE)
        dailyExtraFiles = dailyExtraFiles.filter { !it.lowercase().endsWith(".json") }

        // GlLog.i("Collect daily files: $dailyExtraFiles")

        return true
    }

    private fun reshapeTaskRecords() {
        taskRecords.sortBy { it.startTime }

        var i = 0
        val taskIntervalThreshold = GlRoot.systemConfig.taskRecordThreshold

        while (i < taskRecords.size - 1) {
            if (taskRecords[i + 1].startTime - taskRecords[i].startTime < taskIntervalThreshold) {
                taskRecords.removeAt(i)
            } else {
                i += 1
            }
        }
    }
}