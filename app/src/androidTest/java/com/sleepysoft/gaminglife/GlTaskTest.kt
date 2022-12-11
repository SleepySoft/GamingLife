package com.sleepysoft.gaminglife

import glcore.GlRoot
import org.junit.Test

internal class GlTaskTest {

    @Test
    fun testDailyDataSettle() {
        GlRoot.glTask.checkSettleDailyData()
    }
}