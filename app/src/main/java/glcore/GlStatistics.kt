package glcore

import java.util.*


class TaskRecordEx : TaskRecord() {
    var endTime: Long = 0L
}


class GlDailyStatistics {
    var dailyPath: String = ""
        private set
    var dailyFile: String = ""
        private set
    val dailyData: PathDict = PathDict()

    var dailyExtraFiles = listOf< String >()

    var dailyTs: Long = 0
    val taskRecords: MutableList< TaskRecordEx > = mutableListOf()
    val dailyGroupTime: MutableMap< String , Long > = mutableMapOf()

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
            taskRecords.clear()
            dailyGroupTime.clear()

            dailyTs = (dailyData.get(PATH_DAILY_START_TS) as? Long) ?: 0
            val dailyHis = dailyData.get(PATH_DAILY_TASK_RECORD) as? List< * >

            if ((dailyTs == 0L) || (dailyHis == null)) {
                return false
            }

            GlLog.i("Parsing daily data ${Date(dailyTs)} ...")

            var prevTime = dailyTs
            for (task in dailyHis) {
                val taskRecord = TaskRecordEx().apply { fromAnyStruct(task) }
                if (taskRecord.dataValid) {
                    if (taskRecord.startTime >= prevTime ) {
                        taskRecord.endTime = taskRecord.startTime - prevTime
                        dailyGroupTime[taskRecord.groupID] =
                            (dailyGroupTime[taskRecord.groupID] ?: 0) + taskRecord.endTime
                        prevTime = taskRecord.startTime
                    }
                    else {
                        taskRecord.endTime = 0
                    }
                    taskRecords.add(taskRecord)
                } else {
                    return false
                }
            }
            true
        } catch (e: Exception) {
            GlLog.i("Parse daily data fail - $e")
            false
        }
    }

    private fun collectDailyExtraFiles() : Boolean {
        dailyExtraFiles = GlFile.listFiles(dailyPath, GlFile.LIST_FILE)
        dailyExtraFiles.filter { !it.endsWith(".json") }

        GlLog.i("Collect daily files: $dailyExtraFiles")

        return true
    }
}