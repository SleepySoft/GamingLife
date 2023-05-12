package glcore

class GlRuntimeData {
/*    var dailyStartTs: Long = 0
    var weeklyStartTs: Long = 0
    var biweeklyStartTs: Long = 0
    var monthlyStartTs: Long = 0
    var quarterlyStartTs: Long = 0*/

    private val runtimeData: PathDict = PathDict()

    var startedPeriodicTask: GlDataListEditor< PeriodicTask > =
        GlDataListEditor(runtimeData, PATH_RUNTIME_PERIODIC_TASKS_STARTED) { PeriodicTask.factory() }

    fun loadRuntimeData() : Boolean {
        val result = loadPathDict(GL_FILE_RUNTIME_DATA, runtimeData)
        if (result) {
            syncUp()
            startedPeriodicTask.syncUp()
        }
        return result
    }

    fun saveRuntimeData() : Boolean {
        startedPeriodicTask.syncDown()
        syncDown()
        return savePathDict(GL_FILE_RUNTIME_DATA, runtimeData)
    }

    fun buildRuntimeData() : Boolean {
        return true
    }

    fun syncUp() {
/*
        dailyStartTs = runtimeData.get(PATH_RUNTIME_PERIOD_DAILY_START_TS) as? Long ?: 0L
        weeklyStartTs = runtimeData.get(PATH_RUNTIME_PERIOD_WEEKLY_TS) as? Long ?: 0L
        biweeklyStartTs = runtimeData.get(PATH_RUNTIME_PERIOD_BIWEEKLY_TS) as? Long ?: 0L
        monthlyStartTs = runtimeData.get(PATH_RUNTIME_PERIOD_MONTHLY_TS) as? Long ?: 0L
        quarterlyStartTs = runtimeData.get(PATH_RUNTIME_PERIOD_QUARTERLY_START_TS) as? Long ?: 0L
*/
    }

    fun syncDown() {
/*
        runtimeData.set(PATH_RUNTIME_PERIOD_DAILY_START_TS, dailyStartTs)
        runtimeData.set(PATH_RUNTIME_PERIOD_WEEKLY_TS, weeklyStartTs)
        runtimeData.set(PATH_RUNTIME_PERIOD_BIWEEKLY_TS, biweeklyStartTs)
        runtimeData.set(PATH_RUNTIME_PERIOD_MONTHLY_TS, monthlyStartTs)
        runtimeData.set(PATH_RUNTIME_PERIOD_QUARTERLY_START_TS, quarterlyStartTs)
*/
    }
}