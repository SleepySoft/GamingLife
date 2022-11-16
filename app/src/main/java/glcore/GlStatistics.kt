package glcore

import java.util.*


class GlDailyStatistics() {
    var dailyPath: String = ""
        private set
    val dailyData: PathDict = PathDict()

    var dailyTs: Long = 0
    val taskRecords: MutableList< TaskRecord > = mutableListOf()
    val dailyGroupTime: MutableMap< String , Long > = mutableMapOf()

    fun loadDailyData(dateTime: Date) : Boolean {
        val offsetDays = GlDateTime.daysBetween(GlDateTime.datetime(), dateTime)
        val dayFolderName = GlRoot.getDailyFolderName(offsetDays)
        val dailyFileName: String = GlRoot.getFileNameTs()

        dailyPath = GlFile.joinPaths(dayFolderName, dailyFileName)

        val fileContent: String = GlFile.loadFile(dailyPath).toString(Charsets.UTF_8)

        return if (fileContent.isNotEmpty()) {
            dailyData.attach(GlJson.deserializeAnyDict(fileContent))
            dailyData.hasUpdate = false
            val ret = parseDailyData() and collectExtraFiles()
            GlLog.i("GlDailyStatistics - Load daily data $dateTime ${if (ret) "SUCCESS." else "FAIL."}")
            ret
        } else {
            GlLog.i("GlDailyStatistics - Load daily data $dateTime FAIL.")
            false
        }
    }

    fun loadDailyData(dayOffset: Int) : Boolean {
        return loadDailyData(GlDateTime.datetime(dayOffset))
    }

    private fun parseDailyData() : Boolean {
        return try {
            taskRecords.clear()
            dailyGroupTime.clear()

            dailyTs = (dailyData.get(PATH_DAILY_START_TS) as? Long) ?: 0
            val dailyHis = dailyData.get(PATH_DAILY_TASK_HISTORY) as? List< * >

            if ((dailyTs == 0L) || (dailyHis == null)) {
                return false
            }

            GlLog.i("Parsing daily data ${Date(dailyTs)} ...")

            var prevTime = dailyTs
            for (task in dailyHis) {
                val taskRecord = TaskRecord().apply { fromAnyStruct(task) }
                if (taskRecord.dataValid) {
                    if (taskRecord.startTime >= prevTime ) {
                        taskRecord.extEndTime = taskRecord.startTime - prevTime
                        dailyGroupTime[taskRecord.groupID] =
                            (dailyGroupTime[taskRecord.groupID] ?: 0) + taskRecord.extEndTime
                        prevTime = taskRecord.startTime
                    }
                    else {
                        taskRecord.extEndTime = 0
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

    private fun collectExtraFiles() : Boolean {
        val extraFiles = GlFile.listFiles(dailyPath, false)

        GlLog.i("Collect daily files: $extraFiles")

        return true
    }
}