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

}