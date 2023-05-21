package glcore

import android.os.Build
import androidx.annotation.RequiresApi
import glenv.GlKeyPair
import java.lang.Float.max
import java.util.*
import kotlin.math.log10


object GlService {

    object CoreLogic {
        fun checkRefreshPeriodicTask(baseDate: Date,
                                     startedTasks: List< PeriodicTask >,
                                     refreshedTasks: MutableList< PeriodicTask >,
                                     archivedTasks: MutableList< PeriodicTask >) {
            val dayStartTs = GlDateTime.dayStartTimeStamp(baseDate)
            val weekStartTs = GlDateTime.weekStartTimeStamp(baseDate)
            val monthStartTs = GlDateTime.monthStartTimeStamp(baseDate)
            val quarterStartTs = GlDateTime.quarterStartTimeStamp(baseDate)

            for (startedTask in startedTasks) {
                val refreshTs = when (startedTask.periodic) {
                    ENUM_TASK_PERIOD_ONESHOT -> null
                    ENUM_TASK_PERIOD_DAILY -> dayStartTs
                    ENUM_TASK_PERIOD_WEEKLY -> weekStartTs
                    ENUM_TASK_PERIOD_BI_WEEK -> if (startedTask.refreshTs < weekStartTs - 7 * 24 * 3600) weekStartTs else null
                    ENUM_TASK_PERIOD_MONTHLY -> monthStartTs
                    ENUM_TASK_PERIOD_QUARTERLY -> quarterStartTs
                    else -> null
                }
                if (refreshTs != null && startedTask.refreshTs != refreshTs) {
                    if (startedTask.conclusion == ENUM_TASK_CONCLUSION_NONE ||
                        startedTask.conclusion == ENUM_TASK_CONCLUSION_DOING) {
                        // Process Un-finished task
                        startedTask.conclusion = ENUM_TASK_CONCLUSION_FAILED
                        startedTask.conclusionTs = GlDateTime.timeStamp()
                        archivedTasks.add(startedTask.copy() as PeriodicTask)
                    }
                    startedTask.dueDateTime = 0
                    startedTask.refreshTs = refreshTs
                    startedTask.batchRemaining = startedTask.batch
                    startedTask.conclusion = ENUM_TASK_CONCLUSION_NONE
                    startedTask.conclusionTs = 0L
                    refreshedTasks.add(startedTask)
                }
            }
        }

        fun syncConfigPeriodicTaskToStarted(
            configPeriodicTask: List< PeriodicTask >,
            startedPeriodicTask: List< PeriodicTask >,
            updateTasks: MutableList< PeriodicTask >,
            removeTasks: MutableList< String >) {

            val configIds = configPeriodicTask.map { it.id }.toSet()
            startedPeriodicTask.forEach {
                if (it.id !in configIds) {
                    removeTasks.add(it.uuid)
                }
            }

            val startedMap = startedPeriodicTask.associateBy { it.id }
            for (configTask in configPeriodicTask) {
                val updateTask = startedMap[configTask.id] ?: PeriodicTask()
                updateTask.id = configTask.id
                updateTask.name = configTask.name
                updateTask.group = configTask.group
                updateTask.periodic = configTask.periodic
                updateTask.property = configTask.property
                updateTask.timeQuality = configTask.timeQuality
                updateTask.timeEstimation = configTask.timeEstimation
                updateTask.batch = configTask.batch
                updateTask.batchSize = configTask.batchSize
                updateTasks.add(updateTask)
            }
        }

        fun calculateTaskDueTime(tasks: List<PeriodicTask>) : List<Long> {
            val taskDueTimeList = mutableListOf<Long>()
            for (task in tasks) {
                if ((task.conclusion == ENUM_TASK_CONCLUSION_NONE) ||
                    (task.conclusion == ENUM_TASK_CONCLUSION_DOING)) {

                    if ((task.periodic == ENUM_TASK_PERIOD_ONESHOT) ||
                        (task.property == ENUM_TASK_PROPERTY_OPTIONAL) ||
                        (task.refreshTs == 0L)) {
                        taskDueTimeList.add(0L)
                    } else {
                        val dueTime = if (task.dueDateTime != 0L) task.dueDateTime else task.refreshTs + when (task.periodic) {
                            ENUM_TASK_PERIOD_DAILY -> 24 * 60 * 60 * 1000L
                            ENUM_TASK_PERIOD_WEEKLY -> 7 * 24 * 60 * 60 * 1000L
                            ENUM_TASK_PERIOD_BI_WEEK -> 14 * 24 * 60 * 60 * 1000L
                            ENUM_TASK_PERIOD_MONTHLY -> 31 * 24 * 60 * 60 * 1000L
                            ENUM_TASK_PERIOD_QUARTERLY -> 92 * 24 * 60 * 60 * 1000L
                            else -> throw IllegalArgumentException("Invalid periodic value")
                        }
                        taskDueTimeList.add(dueTime)
                    }
                } else {
                    taskDueTimeList.add(0L)
                }
            }
            return taskDueTimeList
        }

        fun calculateTaskRemainingTime(tasks: List<PeriodicTask>, currentTime: Long) : List<Long> {
            val remainingTimeList = mutableListOf<Long>()
            val taskDueTimeList = calculateTaskDueTime(tasks)
            for (dueTime in taskDueTimeList) {
                remainingTimeList.add(dueTime - currentTime)
            }
            return remainingTimeList
        }

        fun calculateTaskTolerance(tasks: List<PeriodicTask>, currentTime: Long): List<Float> {
            val taskToleranceList = mutableListOf<Float>()
            val taskDueTimeList = calculateTaskDueTime(tasks)
            for (item in tasks zip taskDueTimeList) {
                val task = item.first
                val dueTime = item.second
                val tolerance = if (dueTime != 0L)
                        ((dueTime - currentTime).toFloat() / (dueTime - task.refreshTs))  else 1f
                taskToleranceList.add(tolerance)
            }
            return taskToleranceList
        }

        // Using -log(x) to calculate task basic urgency
        // If this task have to be done today. The urgency will not less than 0.5

        fun calculateTaskUrgency(tasks: List<PeriodicTask>, currentTime: Long): List<Float> {
            val taskDueTimeList = calculateTaskDueTime(tasks)
            val taskToleranceList = calculateTaskTolerance(tasks, currentTime)
            val taskUrgencyList = mutableListOf<Float>()

            for (item in taskToleranceList zip taskDueTimeList) {
                val urgency = if (item.second == 0L) {
                    0f
                } else {
                    val baseUrgency = -log10(item.first)
                    if ((item.second - currentTime) < TIMESTAMP_COUNT_IN_DAY) {
                        max(0.5f, baseUrgency)
                    } else {
                        max(0f, baseUrgency, )
                    }
                }
                taskUrgencyList.add(urgency)
            }
            return taskUrgencyList
        }
    }

    fun checkSettle() {
        checkSettlePeriodicTask()
        checkSettleDailyData()
    }

    fun calculateTaskUrgency(tasks: List<PeriodicTask>): List<Float> =
        CoreLogic.calculateTaskUrgency(tasks, GlDateTime.timeStamp())

    fun saveDailyRecord() = GlRoot.dailyRecord.saveDailyRecord()
    fun saveRuntimeData() = GlRoot.runtimeData.saveRuntimeData()
    fun saveSystemConfig() = GlRoot.systemConfig.saveSystemConfig()

    fun saveContentToDailyFolder(content: ByteArray, fileSuffix: String, offsetDays: Int = 0) =
        GlRoot.saveContentToDailyFolder(content, fileSuffix, offsetDays)

    fun archiveRootPathFileToDailyFolder(fileName: String, offsetDays: Int = 0) =
        GlRoot.archiveRootPathFileToDailyFolder(fileName, offsetDays)

    // ---------------------------------------------------------------------

    fun getServerSession() = GlRoot.glServerSession

    // ---------------------------------------------------------------------

    fun getCurrentDayRecord() = GlRoot.dailyRecord

    // ---------------------------------------------------------------------

    fun getTimeStamp() = GlDateTime.timeStamp()

    fun formatTimeStamp(ts: Long) = GlDateTime.smartFormatSec(ts / 1000)

    fun getMonthDates(year: Int, month: Int) = GlDateTime.datesInMonth(year, month)

    // ---------------------------------------------------------------------

    fun setPlayerGlID(glid: String) = run { GlRoot.systemConfig.GLID = glid }
    fun getPlayerGlID() = GlRoot.systemConfig.GLID

    @RequiresApi(Build.VERSION_CODES.O)
    fun setMainKeyPair(keyPair: GlKeyPair) = run { GlRoot.systemConfig.mainKeyPair = keyPair }
    fun getMainKeyPair() = GlRoot.systemConfig.mainKeyPair

    // ---------------------------------------------------------------------

    fun getTopTaskGroupsData() = GlRoot.systemConfig.taskGroupEditor.getGlDataList()
    fun getTaskGroupData(groupID: String) =
        GlRoot.systemConfig.taskGroupEditor.getGlDataList().firstOrNull { it.id == groupID }

    // ---------------------------------------------------------------------

    fun upsertPeriodicTask(task: PeriodicTask) {
        GlRoot.systemConfig.periodicTaskEditor.upsertGlData(task)
        syncConfigPeriodicTaskToStarted()
    }

    fun removePeriodicTask(uuid: String) {
        GlRoot.systemConfig.periodicTaskEditor.removeGlData(uuid)
        syncConfigPeriodicTaskToStarted()
    }

    fun getPeriodicTask(uuid: String) : PeriodicTask? =
        GlRoot.systemConfig.periodicTaskEditor.getGlData(uuid)

    fun getPeriodicTasks() : List< PeriodicTask > =
        GlRoot.systemConfig.periodicTaskEditor.getGlDataList()

    fun getPeriodicTasksByGroup(groupID: String) : List< PeriodicTask > {
        val ptasks = GlRoot.systemConfig.periodicTaskEditor.getGlDataList()
        return ptasks.filter { it.group == groupID }
    }

    // ---------------------------------------------------------------------------------------------

    fun countBusyPeriodicTask(tasks: List<PeriodicTask>): Int {
        return tasks.filter {
            it.conclusion != ENUM_TASK_CONCLUSION_FINISHED &&
            it.conclusion != ENUM_TASK_CONCLUSION_ABANDONED
        }.count()
    }

    fun getOnGoingPeriodicTask() : PeriodicTask? =
        GlRoot.runtimeData.startedPeriodicTask.getGlDataList().firstOrNull {
            it.conclusion == ENUM_TASK_CONCLUSION_DOING }

    fun getStartedPeriodicTasks() : List< PeriodicTask > {
        return GlRoot.runtimeData.startedPeriodicTask.getGlDataList()
    }

    fun getStartedPeriodicTasksByGroup(groupID: String) : List< PeriodicTask > {
        val ptasks = GlRoot.runtimeData.startedPeriodicTask.getGlDataList()
        return ptasks.filter { it.group == groupID }
    }

    fun executePeriodicTask(newTaskId: String, pervTaskConclusion: Int = ENUM_TASK_CONCLUSION_NONE) {
        val startedPeriodicTask = GlRoot.runtimeData.startedPeriodicTask.getGlDataList()
        for (task in startedPeriodicTask) {
            if (task.id == newTaskId) {
                task.conclusion = ENUM_TASK_CONCLUSION_DOING
                task.conclusionTs = GlDateTime.timeStamp()

                // Auto switch the current task group to period task's group.
                switchToTask(TaskData().apply { id = task.group })

/*                if (isGroupIdValid(task.group)) {
                    val currentTaskGroup = getCurrentTaskInfo()
                    if (currentTaskGroup.groupID != task.group) {
                        GlRoot.dailyRecord.addTask(TaskRecord().apply {
                            taskID = task.group
                            groupID = task.group
                            startTime = GlDateTime.datetime().time
                        })
                        GlRoot.dailyRecord.saveDailyRecord()
                    }
                } else {
                    // Should not reach here.
                    GlLog.e("An invalid group id has been found: $task.group")
                }*/
            } else if (task.conclusion == ENUM_TASK_CONCLUSION_DOING) {
                task.conclusion = pervTaskConclusion
                task.conclusionTs  = GlDateTime.timeStamp()
            }
        }
        GlRoot.runtimeData.saveRuntimeData()
    }

    fun finishPeriodicTask(taskId: String) {
        val startedPeriodicTask = GlRoot.runtimeData.startedPeriodicTask.getGlDataList()
        startedPeriodicTask.find { it.id == taskId }?.run {
            if (this.batchRemaining > this.batch) {
                // Should not enter here
                this.batchRemaining = this.batch
                println("Warning: Remaining batch is larger than batch.")
            }
            if (this.batchRemaining > 0) {
                this.batchRemaining -= 1
            }
            if (this.batchRemaining == 0) {
                conclusion = ENUM_TASK_CONCLUSION_FINISHED
                conclusionTs = GlDateTime.timeStamp()
                GlRoot.dailyRecord.periodicTaskRecord.upsertGlData(this.copy() as PeriodicTask)
                GlRoot.dailyRecord.saveDailyRecord()
            } else {
                conclusion = ENUM_TASK_CONCLUSION_NONE
                conclusionTs = GlDateTime.timeStamp()
            }
            GlRoot.runtimeData.saveRuntimeData()
        }
    }

    fun suspendPeriodicTask(taskId: String) {
        val startedPeriodicTask = GlRoot.runtimeData.startedPeriodicTask.getGlDataList()
        startedPeriodicTask.find { it.id == taskId }?.run {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            conclusionTs = GlDateTime.timeStamp()
        }
        GlRoot.runtimeData.saveRuntimeData()
    }

    fun abandonPeriodicTask(taskId: String) {
        val startedPeriodicTask = GlRoot.runtimeData.startedPeriodicTask.getGlDataList()
        startedPeriodicTask.find { it.id == taskId }?.run {
            conclusion = ENUM_TASK_CONCLUSION_ABANDONED
            conclusionTs = GlDateTime.timeStamp()
            GlRoot.dailyRecord.periodicTaskRecord.upsertGlData(this.copy() as PeriodicTask)
            GlRoot.dailyRecord.saveDailyRecord()
            GlRoot.runtimeData.saveRuntimeData()
        }
    }

    fun syncConfigPeriodicTaskToStarted() {
        val configPeriodicTask = GlRoot.systemConfig.periodicTaskEditor.getGlDataList()
        val startedPeriodicTask = GlRoot.runtimeData.startedPeriodicTask.getGlDataList()
        val updateTasks = mutableListOf< PeriodicTask >()
        val removeTasks = mutableListOf< String >()

        CoreLogic.syncConfigPeriodicTaskToStarted(
            configPeriodicTask, startedPeriodicTask, updateTasks, removeTasks)

        GlRoot.runtimeData.startedPeriodicTask.removeGlData(removeTasks)
        GlRoot.runtimeData.startedPeriodicTask.upsertGlData(updateTasks)
        GlRoot.runtimeData.saveRuntimeData()
    }

    fun checkRefreshPeriodicTask() {
        val startedPeriodicTasks = GlRoot.runtimeData.startedPeriodicTask.getGlDataList()
        val refreshedTasks = mutableListOf< PeriodicTask >()
        val archivedTasks = mutableListOf< PeriodicTask >()

        CoreLogic.checkRefreshPeriodicTask(
            GlDateTime.now(), startedPeriodicTasks, refreshedTasks, archivedTasks)

        if (refreshedTasks.isNotEmpty()) {
            GlRoot.runtimeData.startedPeriodicTask.upsertGlData(refreshedTasks)
            GlRoot.runtimeData.saveRuntimeData()
        }

        if (archivedTasks.isNotEmpty()) {
            GlRoot.dailyRecord.periodicTaskRecord.upsertGlData(archivedTasks)
            GlRoot.dailyRecord.saveDailyRecord()
        }
    }

    private fun checkSettlePeriodicTask() {
        syncConfigPeriodicTaskToStarted()
        checkRefreshPeriodicTask()
    }

    fun getPeriodicTaskInMonth(year: Int, month: Int) : List< List< PeriodicTask > > {
        val today = Date()
        val dates = getMonthDates(year, month)
        val taskRecords = mutableListOf< List< PeriodicTask > >()

        for (d in dates) {
            if (d.before(today)) {
                GlDailyRecord().apply { loadDailyRecord(d) }
            } else if (d == today) {
                GlRoot.dailyRecord
            } else {
                null
            }?.run { taskRecords.add(periodicTaskRecord.getGlDataList()) }
        }

        return taskRecords
    }

    // ---------------------------------------------------------------------------------------------

    // ----------------------- Task Switching -----------------------

    fun switchToTask(taskData: TaskData) {
        checkSettle()

        // History reason: Actually now we only top level task group.
        if (isGroupIdValid(taskData.id)) {
            val currentTaskGroup = getCurrentTaskInfo()
            if (currentTaskGroup.groupID != taskData.id) {
                GlRoot.dailyRecord.addTask(TaskRecord().apply {
                    taskID = taskData.id
                    groupID = taskData.id   //GlRoot.systemConfig.getTopGroupOfTask(taskData.id)
                    startTime = GlDateTime.datetime().time
                })
                GlRoot.dailyRecord.saveDailyRecord()
            }
        } else {
            // Should not reach here.
            GlLog.e("An invalid group id has been found: $taskData.id")
        }
    }

    fun isGroupIdValid(taskId: String) : Boolean =
        GlRoot.systemConfig.taskGroupEditor.getGlDataList().any { it.id == taskId }

    fun getCurrentTaskInfo() : TaskRecord {
        // checkSettleDailyData()
        return GlRoot.dailyRecord.lastTask() ?: TaskRecord().apply {
            taskID = ""
            groupID = GROUP_ID_IDLE
            startTime = GlDateTime.datetime().time
        }
    }

    fun getCurrentTaskName() : String {
        val taskRecord = getCurrentTaskInfo()
        val taskData = GlRoot.systemConfig.taskGroupEditor.getGlData(taskRecord.groupID)
        return taskData?.name ?: ""
    }

    fun getCurrentTaskLastTimeMs() : Long {
        val currentTaskData = getCurrentTaskInfo()
        val currentTaskStartTimeMs = currentTaskData.startTime
        return System.currentTimeMillis() - currentTaskStartTimeMs
    }

    fun getCurrentTaskLastTimeFormatted() : String {
        val deltaTimeMs = getCurrentTaskLastTimeMs()
        val deltaTimeS: Long = deltaTimeMs / 1000

        val ms: Long = deltaTimeMs % 1000
        val hour: Long = deltaTimeS / 3600
        val remainingSec: Long = deltaTimeS % 3600
        val minutes: Long = remainingSec / 60
        val seconds: Long = remainingSec % 60

        return if (hour != 0L) {
            "%02d:%02d:%02d".format(hour, minutes, seconds)
        }
        else {
            "%02d:%02d".format(minutes, seconds)
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