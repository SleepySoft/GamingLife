package glcore

import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import java.util.*


internal class GlServiceTest {

    @Test
    fun testOneshotPeriod() {
        val baseDate = Date()
        val startedTasks = mutableListOf<PeriodicTask>()
        val refreshedTasks = mutableListOf<PeriodicTask>()
        val archivedTasks = mutableListOf<PeriodicTask>()

        // Create a oneshot task with refreshTs = 0
        val oneshotTask1 = PeriodicTask().apply {
            id = "1"
            name = "Oneshot Task 1"
            group = "Test Group"
            periodic = ENUM_TASK_PERIOD_ONESHOT
            refreshTs = 0L
        }
        startedTasks.add(oneshotTask1)

        // Create a oneshot task with refreshTs < baseDate
        val oneshotTask2 = PeriodicTask().apply {
            id = "2"
            name = "Oneshot Task 2"
            group = "Test Group"
            periodic = ENUM_TASK_PERIOD_ONESHOT
            refreshTs = baseDate.time - 1000L
        }
        startedTasks.add(oneshotTask2)

        // Create a oneshot task with refreshTs > baseDate
        val oneshotTask3 = PeriodicTask().apply {
            id = "3"
            name = "Oneshot Task 3"
            group = "Test Group"
            periodic = ENUM_TASK_PERIOD_ONESHOT
            refreshTs = baseDate.time + 1000L
        }
        startedTasks.add(oneshotTask3)

        GlService.CoreLogic.checkRefreshPeriodicTask(baseDate, startedTasks, refreshedTasks, archivedTasks)

        // Assert that no tasks were added to the refreshedTasks list
        assertEquals(0, refreshedTasks.size)
        assertEquals(0, archivedTasks.size)
    }

    // 这是第一个测试用例，它测试了在本周期内的情况。在这种情况下，不需要更新，
    // 字段(dueDateTime, refreshTs, batchRemaining, conclusion, conclusionTs)不变。

    @Test
    fun testCheckRefreshPeriodicTask_case1() {
        val baseDate = Date()
        val startedTasks = mutableListOf<PeriodicTask>()
        val refreshedTasks = mutableListOf<PeriodicTask>()
        val archivedTasks = mutableListOf<PeriodicTask>()

        val startedTask = PeriodicTask().apply {
            id = "1"
            name = "Test Task"
            group = "Test Group"
            periodic = ENUM_TASK_PERIOD_DAILY
            timeQuality = 0
            timeEstimation = 0
            batch = 0
            batchSize = 0
            dueDateTime = 0L
            refreshTs = GlDateTime.dayStartTimeStamp(baseDate)
            batchRemaining = 0
            conclusion = ENUM_TASK_CONCLUSION_NONE
            conclusionTs = 0L
        }
        startedTasks.add(startedTask)

        GlService.CoreLogic.checkRefreshPeriodicTask(baseDate, startedTasks, refreshedTasks, archivedTasks)

        assertEquals(0, refreshedTasks.size)
        assertEquals(0, archivedTasks.size)
        assertEquals(startedTask.dueDateTime, startedTasks[0].dueDateTime)
        assertEquals(startedTask.refreshTs, startedTasks[0].refreshTs)
        assertEquals(startedTask.batchRemaining, startedTasks[0].batchRemaining)
        assertEquals(startedTask.conclusion, startedTasks[0].conclusion)
        assertEquals(startedTask.conclusionTs, startedTasks[0].conclusionTs)
    }

    // 这是第二个测试用例，它测试了不在本周期内（startedTask.refreshTs小于本周期）的情况。
    // 在这种情况下，需要更新，对应字段需更新到特定的值。

    @Test
    fun testCheckRefreshPeriodicTask_case2() {
        val baseDate = Date()
        val startedTasks = mutableListOf<PeriodicTask>()
        val refreshedTasks = mutableListOf<PeriodicTask>()
        val archivedTasks = mutableListOf<PeriodicTask>()

        val startedTask = PeriodicTask().apply {
            id = "1"
            name = "Test Task"
            group = "Test Group"
            periodic = ENUM_TASK_PERIOD_DAILY
            timeQuality = 0
            timeEstimation = 0
            batch = 0
            batchSize = 0
            dueDateTime = 0L
            refreshTs = GlDateTime.dayStartTimeStamp(baseDate) - 24 * 3600 // Set refreshTs to previous day
            batchRemaining = 0
            conclusion = ENUM_TASK_CONCLUSION_NONE
            conclusionTs = 0L
        }
        startedTasks.add(startedTask)

        GlService.CoreLogic.checkRefreshPeriodicTask(baseDate, startedTasks, refreshedTasks, archivedTasks)

        assertEquals(1, archivedTasks.size)
        assertEquals(ENUM_TASK_CONCLUSION_FAILED, archivedTasks[0].conclusion)

        assertEquals(1, refreshedTasks.size)
        assertEquals(0L, refreshedTasks[0].dueDateTime)
        assertEquals(GlDateTime.dayStartTimeStamp(baseDate), refreshedTasks[0].refreshTs)
        assertEquals(startedTask.batch, refreshedTasks[0].batchRemaining)
        assertEquals(ENUM_TASK_CONCLUSION_NONE, refreshedTasks[0].conclusion)
        assertEquals(0L, refreshedTasks[0].conclusionTs)
    }

    @Test
    fun testCheckRefreshPeriodicTask_case3() {
        // Test case for startedTask.refreshTs > current period
        val baseDate = Date()
        val startedTasks = mutableListOf<PeriodicTask>()
        val refreshedTasks = mutableListOf<PeriodicTask>()
        val archivedTasks = mutableListOf<PeriodicTask>()

        val startedTask = PeriodicTask().apply {
            id = "1"
            name = "Test Task"
            group = "Test Group"
            periodic = ENUM_TASK_PERIOD_DAILY
            timeQuality = 0
            timeEstimation = 0
            batch = 0
            batchSize = 0
            dueDateTime = 0L
            refreshTs = GlDateTime.dayStartTimeStamp(baseDate) + 24 * 3600 // Set refreshTs to next day
            batchRemaining = 0
            conclusion = ENUM_TASK_CONCLUSION_NONE
            conclusionTs = 0L
        }
        startedTasks.add(startedTask)

        GlService.CoreLogic.checkRefreshPeriodicTask(baseDate, startedTasks, refreshedTasks, archivedTasks)

        assertEquals(1, archivedTasks.size)
        assertEquals(ENUM_TASK_CONCLUSION_FAILED, archivedTasks[0].conclusion)

        assertEquals(1, refreshedTasks.size)
        assertEquals(0L, refreshedTasks[0].dueDateTime)
        assertEquals(GlDateTime.dayStartTimeStamp(baseDate), refreshedTasks[0].refreshTs)
        assertEquals(startedTask.batch, refreshedTasks[0].batchRemaining)
        assertEquals(ENUM_TASK_CONCLUSION_NONE, refreshedTasks[0].conclusion)
        assertEquals(0L, refreshedTasks[0].conclusionTs)
    }

    @Test
    fun testCheckRefreshPeriodicTask_case4() {
        // Test case for startedTask.refreshTs == 0
        val baseDate = Date()
        val startedTasks = mutableListOf<PeriodicTask>()
        val refreshedTasks = mutableListOf<PeriodicTask>()
        val archivedTasks = mutableListOf<PeriodicTask>()

        val startedTask = PeriodicTask().apply {
            id = "1"
            name = "Test Task"
            group = "Test Group"
            periodic = ENUM_TASK_PERIOD_DAILY
            timeQuality = 0
            timeEstimation = 0
            batch = 0
            batchSize = 0
            dueDateTime = 0L
            refreshTs = 0L // Set refreshTs to 0
            batchRemaining = 0
            conclusion = ENUM_TASK_CONCLUSION_NONE
            conclusionTs = 0L
        }
        startedTasks.add(startedTask)

        GlService.CoreLogic.checkRefreshPeriodicTask(baseDate, startedTasks, refreshedTasks, archivedTasks)

        assertEquals(1, archivedTasks.size)
        assertEquals(ENUM_TASK_CONCLUSION_FAILED, archivedTasks[0].conclusion)

        assertEquals(1, refreshedTasks.size)
        assertEquals(0L, refreshedTasks[0].dueDateTime)
        assertEquals(GlDateTime.dayStartTimeStamp(baseDate), refreshedTasks[0].refreshTs)
        assertEquals(startedTask.batch, refreshedTasks[0].batchRemaining)
        assertEquals(ENUM_TASK_CONCLUSION_NONE, refreshedTasks[0].conclusion)
        assertEquals(0L, refreshedTasks[0].conclusionTs)
    }

    @Test
    fun testCheckRefreshPeriodicTask_case6() {
        // Test case for startedTask.refreshTs < current period and conclusion == ENUM_TASK_CONCLUSION_FINISHED
        val baseDate = Date()
        val startedTasks = mutableListOf<PeriodicTask>()
        val refreshedTasks = mutableListOf<PeriodicTask>()
        val archivedTasks = mutableListOf<PeriodicTask>()

        val startedTask = PeriodicTask().apply {
            id = "1"
            name = "Test Task"
            group = "Test Group"
            periodic = ENUM_TASK_PERIOD_DAILY
            timeQuality = 0
            timeEstimation = 0
            batch = 0
            batchSize = 0
            dueDateTime = 0L
            refreshTs = GlDateTime.dayStartTimeStamp(baseDate) - 24 * 3600 // Set refreshTs to previous day
            batchRemaining = 0
            conclusion = ENUM_TASK_CONCLUSION_FINISHED
            conclusionTs = 0L
        }
        startedTasks.add(startedTask)

        GlService.CoreLogic.checkRefreshPeriodicTask(baseDate, startedTasks, refreshedTasks, archivedTasks)

        assertEquals(0, archivedTasks.size)

        assertEquals(1, refreshedTasks.size)
        assertEquals(0L, refreshedTasks[0].dueDateTime)
        assertEquals(GlDateTime.dayStartTimeStamp(baseDate), refreshedTasks[0].refreshTs)
        assertEquals(startedTask.batch, refreshedTasks[0].batchRemaining)
        assertEquals(ENUM_TASK_CONCLUSION_NONE, refreshedTasks[0].conclusion)
        assertEquals(0L, refreshedTasks[0].conclusionTs)
    }

    @Test
    fun testCheckRefreshPeriodicTask_case7() {
        // Test case for startedTask.refreshTs < current period and conclusion == ENUM_TASK_CONCLUSION_ABANDONED
        val baseDate = Date()
        val startedTasks = mutableListOf<PeriodicTask>()
        val refreshedTasks = mutableListOf<PeriodicTask>()
        val archivedTasks = mutableListOf<PeriodicTask>()

        val startedTask = PeriodicTask().apply {
            id = "1"
            name = "Test Task"
            group = "Test Group"
            periodic = ENUM_TASK_PERIOD_DAILY
            timeQuality = 0
            timeEstimation = 0
            batch = 0
            batchSize = 0
            dueDateTime = 0L
            refreshTs = GlDateTime.dayStartTimeStamp(baseDate) - 24 * 3600 // Set refreshTs to previous day
            batchRemaining = 0
            conclusion = ENUM_TASK_CONCLUSION_ABANDONED
            conclusionTs = 0L
        }
        startedTasks.add(startedTask)

        GlService.CoreLogic.checkRefreshPeriodicTask(baseDate, startedTasks, refreshedTasks, archivedTasks)

        assertEquals(0, archivedTasks.size)

        assertEquals(1, refreshedTasks.size)
        assertEquals(0L, refreshedTasks[0].dueDateTime)
        assertEquals(GlDateTime.dayStartTimeStamp(baseDate), refreshedTasks[0].refreshTs)
        assertEquals(startedTask.batch, refreshedTasks[0].batchRemaining)
        assertEquals(ENUM_TASK_CONCLUSION_NONE, refreshedTasks[0].conclusion)
        assertEquals(0L, refreshedTasks[0].conclusionTs)
    }

    // ---------------------------------------------------------------------------------------------

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_NONE 且属性为 ENUM_TASK_PROPERTY_OPTIONAL 时，容忍度是否为 1
    @Test
    fun testTaskToleranceCase1() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            property = ENUM_TASK_PROPERTY_OPTIONAL
            refreshTs = 1000L
        }
        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
        // 预计 dueTime: N/A
        assert(result[0] == 1f)
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_NONE 且 refreshTs 为 0 时，容忍度是否为 1
    @Test
    fun testTaskToleranceCase2() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 0L
        }
        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
        // 预计 dueTime: N/A
        assert(result[0] == 1f)
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_NONE 且周期为 ENUM_TASK_PERIOD_ONESHOT 时，容忍度是否正确
    @Test
    fun testTaskToleranceCase3() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
            periodic = ENUM_TASK_PERIOD_ONESHOT
        }
        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
        // 预计 dueTime: 1
        assert(result[0] == 1f)
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_NONE 且周期为 ENUM_TASK_PERIOD_DAILY 时，容忍度是否正确
    @Test
    fun testTaskToleranceCase4() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
            periodic = ENUM_TASK_PERIOD_DAILY
        }
        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
        // 预计 dueTime: 1000 + 24 * 60 * 60 * 1000L
        val dueTime = task.refreshTs + 24 * 60 * 60 * 1000L
        val tolerance = (dueTime - currentTime).toFloat() / (dueTime - task.refreshTs)
        assert(result[0] == tolerance)
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_DOING 且周期为 ENUM_TASK_PERIOD_WEEKLY 时，容忍度是否正确
    @Test
    fun testTaskToleranceCase5() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_DOING
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
            periodic = ENUM_TASK_PERIOD_WEEKLY
        }
        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
        // 预计 dueTime: 1000 + 7 * 24 * 60 * 60 * 1000L
        val dueTime = task.refreshTs + 7 * 24 * 60 * 60 * 1000L
        val tolerance = (dueTime - currentTime).toFloat() / (dueTime - task.refreshTs)
        assert(result[0] == tolerance)
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_DONE 时，容忍度是否为 1
    @Test
    fun testTaskToleranceCase6() {
        val task1 = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_FINISHED
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
        }
        val task2 = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_ABANDONED
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
        }
        val task3 = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_FAILED
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
        }

        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(
            listOf(task1, task2, task3), currentTime)

        assert(result[0] == 1f)
        assert(result[1] == 1f)
        assert(result[2] == 1f)
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_NONE 且周期为 ENUM_TASK_PERIOD_BI_WEEK 时，容忍度是否正确
    @Test
    fun testTaskToleranceCase8() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
            periodic = ENUM_TASK_PERIOD_BI_WEEK
        }
        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
        // 预计 dueTime: 1000 + 14 * 24 * 60 * 60 * 1000L
        val dueTime = task.refreshTs + 14 * 24 * 60 * 60 * 1000L
        val tolerance = (dueTime - currentTime).toFloat() / (dueTime - task.refreshTs)
        assert(result[0] == tolerance)
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_NONE 且周期为 ENUM_TASK_PERIOD_MONTHLY 时，容忍度是否正确
    @Test
    fun testTaskToleranceCase9() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
            periodic = ENUM_TASK_PERIOD_MONTHLY
        }
        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
        // 预计 dueTime: 1000 + 31 * 24 * 60 * 60 * 1000L
        val dueTime = task.refreshTs + 31 * 24 * 60 * 60 * 1000L
        val tolerance = (dueTime - currentTime).toFloat() / (dueTime - task.refreshTs)
        assert(result[0] == tolerance)
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_NONE 且周期为 ENUM_TASK_PERIOD_QUARTERLY 时，容忍度是否正确
    @Test
    fun testTaskToleranceCase10() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
            periodic = ENUM_TASK_PERIOD_QUARTERLY
        }
        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
        // 预计 dueTime: 1000 + 92 * 24 * 60 * 60 * 1000L
        val dueTime = task.refreshTs + 92 * 24 * 60 * 60 * 1000L
        val tolerance = (dueTime - currentTime).toFloat() / (dueTime - task.refreshTs)
        assert(result[0] == tolerance)
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_NONE 且周期为无效值时，是否抛出异常
    @Test
    fun testTaskToleranceCase11() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
            periodic = -1
        }
        val currentTime = 2000L
        try {
            GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
            assert(false) // 如果没有抛出异常，则断言失败
        } catch (e: IllegalArgumentException) {
            // 预计 dueTime: N/A
            assert(e.message == "Invalid periodic value")
        }
    }

    // 测试目的：测试当任务列表为空时，返回的容忍度列表是否为空
    @Test
    fun testTaskToleranceCase12() {
        val tasks = emptyList<PeriodicTask>()
        val currentTime = 2000L
        val result = GlService.CoreLogic.calculateTaskTolerance(tasks, currentTime)
        // 预计 dueTime: N/A
        assert(result.isEmpty())
    }

    // 测试目的：测试当任务状态为 ENUM_TASK_CONCLUSION_NONE 且 dueDateTime 不为 0 时，容忍度是否正确
    fun testTaskToleranceCase13() {
        val task = PeriodicTask().apply {
            conclusion = ENUM_TASK_CONCLUSION_NONE
            property = ENUM_TASK_PROPERTY_NORMAL
            refreshTs = 1000L
            periodic = ENUM_TASK_PERIOD_DAILY
            dueDateTime = 2000L
        }
        val currentTime = 3000L
        val result = GlService.CoreLogic.calculateTaskTolerance(listOf(task), currentTime)
        // 预计 dueTime: 2000
        val tolerance = (task.dueDateTime - currentTime).toFloat() / (task.dueDateTime - task.refreshTs)
        assert(result[0] == tolerance)
    }
}