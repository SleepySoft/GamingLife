package com.sleepysoft.gaminglife

import glcore.GlRoot
import org.junit.Test

internal class GlTaskModuleTest {

    @Test
    fun testDailyDataSettle() {
        GlRoot.glTaskModule.checkSettleDailyData()
    }
}