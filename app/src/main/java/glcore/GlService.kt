package glcore

import java.util.*


class GlService {

    // ----------------------- Task Switching -----------------------

    fun switchToTask(taskData: TaskData) {
        checkSettleDailyData()
        GlRoot.dailyRecord.addTask(TaskRecord().apply {
            taskID = taskData.id
            groupID = GlRoot.systemConfig.getTopGroupOfTask(taskData.id)
            startTime = GlDateTime.datetime().time
        })
        GlRoot.dailyRecord.saveDailyRecord()
    }

    fun getCurrentTaskInfo() : TaskRecord {
        // checkSettleDailyData()
        return GlRoot.dailyRecord.lastTask() ?: TaskRecord().apply {
            var taskID: String = ""
            var groupID: String = GROUP_ID_IDLE
            var startTime: Long = GlDateTime.datetime().time
        }
    }

    // ---------------------------------------- Daily data ----------------------------------------

    fun checkSettleDailyData() {
        GlLog.i("Check settle daily data.")

        /*******************************************************************************************
         *
         * The algorithm of settling daily data:
         * If daily data's timestamp does not match current day's start time, it means we should do
         *      the daily data settling.
         * 1. Duplicate and record the current task data to history data
         * 2. Reset the current task's start time to current day's start time
         * 3. Archive the daily data to it's daily folder (by its time stamp).
         *
         *******************************************************************************************/

        val dailyDataTs = GlRoot.dailyRecord.dailyTs
        val currentDayTs = GlDateTime.dayStartTimeStamp()

        if (currentDayTs != dailyDataTs) {
            GlLog.i("> Do settling.")

            GlRoot.dailyRecord.saveDailyRecord()
            GlRoot.archiveJsonFilesToDailyFolder(Date(dailyDataTs))

            // Yesterday's last task will be today's first task

            val lastTask = GlRoot.dailyRecord.lastTask()

            GlRoot.dailyRecord.newDailyRecord()

            lastTask?.run {
                val lastingTask = TaskRecord().apply {
                    taskID = lastTask.taskID
                    groupID = lastTask.groupID
                    startTime = currentDayTs
                }
                GlRoot.dailyRecord.addTask(lastingTask)
            }

            GlRoot.dailyRecord.saveDailyRecord()
        }
        else {
            // The daily data is the current day, everything is OK.
            GlLog.i("> Not need to settle.")
        }
    }

/*

    // ------------------------------------------ Private ------------------------------------------

    private fun settleDailyData(dailyDataTs: Long, currentDayTs: Long) {
        GlRoot.archiveJsonFilesToDailyFolder(Date(dailyDataTs))
    }

    private fun resetDayDailyData() {
        GlRoot.glDatabase.dailyRecord.clear()
        GlRoot.glDatabase.dailyRecord.set(PATH_DAILY_START_TS, GlDateTime.dayStartTimeStamp())
        GlRoot.glDatabase.dailyRecord.set(PATH_DAILY_TASK_RECORD, mutableListOf< GlAnyDict >())
        GlRoot.glDatabase.save()
    }

    private fun currentTaskToHistory() {
        val currentTask = getCurrentTaskInfo()
        val currentTaskStartTs = currentTask["startTime"] as Long
        val taskRecordThreshold: Long? =
            mDatabase.systemConfig.get(PATH_SYSTEM_TASK_RECORD_THRESHOLD) as? Long

        taskRecordThreshold?.run {
            if (currentTaskStartTs < taskRecordThreshold) {
                return
            }
        }

        val currentTaskCopy = currentTask.deepCopy()
        val taskHistory = mDatabase.dailyRecord.get(PATH_DAILY_TASK_RECORD)

        if (taskHistory is MutableList< * >) {
            @Suppress("UNCHECKED_CAST")
            (taskHistory as GlAnyStructList).add(currentTaskCopy)
            mDatabase.dailyRecord.hasUpdate = true
        }
        else {
            mDatabase.dailyRecord.set(PATH_DAILY_TASK_RECORD, mutableListOf(currentTaskCopy))
        }
    }

    private fun setCurrentTask(taskId: Any?, ts: Long? = null) {
        val taskIdStr: String = (taskId ?: GROUP_ID_IDLE) as String
        mDatabase.runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/taskID", taskIdStr)
        mDatabase.runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/groupID", groupOfTask(taskIdStr))
        mDatabase.runtimeData.set("$PATH_RUNTIME_CURRENT_TASK/startTime", ts ?: System.currentTimeMillis())
    }*/
}