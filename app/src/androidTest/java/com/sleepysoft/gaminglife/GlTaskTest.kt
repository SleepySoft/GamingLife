package com.sleepysoft.gaminglife

import glcore.GlRoot
import glcore.GlService
import org.junit.Test

internal class GlTaskTest {

    @Test
    fun testDailyDataSettle() {
        GlService.checkSettleDailyData()
    }
}