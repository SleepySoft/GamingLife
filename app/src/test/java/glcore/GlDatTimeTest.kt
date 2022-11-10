package glcore

import org.junit.Test
import java.util.*

internal class GlDatTimeTest {

    private fun printDateTime(ts: Date) {
        println(ts)
        println(GlDateTime.formatToDay(ts))
        println(GlDateTime.formatToSec(ts))
        println(GlDateTime.formatToMSec(ts))
    }

    private fun verifyDaysOffsetAndDaysBetween(offsetDays: Int) {
        val baseDate: Date = GlDateTime.datetime()
        val targetDate: Date = GlDateTime.datetime(offsetDays)
        val daysBetween: Int = GlDateTime.daysBetween(baseDate, targetDate)
        if (daysBetween != offsetDays) {
            println("verifyDaysOffsetAndDaysBetween($offsetDays) Fail, daysBetween = $daysBetween.")
            assert(false)
        }
    }

    @Test
    fun testDatetime() {
        println("--------------------------- Original ---------------------------")
        printDateTime(GlDateTime.datetime())

        println("---------------------- Offset +-367 days -----------------------")
        printDateTime(GlDateTime.datetime(367))
        printDateTime(GlDateTime.datetime(-367))

        println("---------------------- Offset +-25 hours -----------------------")
        printDateTime(GlDateTime.datetime(0, 25))
        printDateTime(GlDateTime.datetime(0, -25))

        println("---------------------- Offset +-60 minutes ----------------------")
        printDateTime(GlDateTime.datetime(0, 0, 61))
        printDateTime(GlDateTime.datetime(0, 0, -61))

        println("---------------------- Offset +-60 seconds ----------------------")
        printDateTime(GlDateTime.datetime(0, 0, 0, 61))
        printDateTime(GlDateTime.datetime(0, 0, 0, -61))
    }

    @Test
    fun testDateTimeDebug() {
        println("--------------------- Debug DateTime: +365 ---------------------")

        GlDateTime.debugDateTime = null
        GlDateTime.debugDateTime = GlDateTime.datetime(365)

        println("--------------------------- Original ---------------------------")
        printDateTime(GlDateTime.datetime())

        println("---------------------- Offset +-367 days -----------------------")
        printDateTime(GlDateTime.datetime(367))
        printDateTime(GlDateTime.datetime(-367))

        println("---------------------- Offset +-25 hours -----------------------")
        printDateTime(GlDateTime.datetime(0, 25))
        printDateTime(GlDateTime.datetime(0, -25))

        println("---------------------- Offset +-60 minutes ----------------------")
        printDateTime(GlDateTime.datetime(0, 0, 61))
        printDateTime(GlDateTime.datetime(0, 0, -61))

        println("---------------------- Offset +-60 seconds ----------------------")
        printDateTime(GlDateTime.datetime(0, 0, 0, 61))
        printDateTime(GlDateTime.datetime(0, 0, 0, -61))
    }

    @Test
    fun testDaysBetween() {
        verifyDaysOffsetAndDaysBetween(0)
        verifyDaysOffsetAndDaysBetween(1)
        verifyDaysOffsetAndDaysBetween(-1)
        verifyDaysOffsetAndDaysBetween(7)
        verifyDaysOffsetAndDaysBetween(-7)
        verifyDaysOffsetAndDaysBetween(30)
        verifyDaysOffsetAndDaysBetween(-30)
        verifyDaysOffsetAndDaysBetween(31)
        verifyDaysOffsetAndDaysBetween(-31)
        verifyDaysOffsetAndDaysBetween(365)
        verifyDaysOffsetAndDaysBetween(-365)
        verifyDaysOffsetAndDaysBetween(366)
        verifyDaysOffsetAndDaysBetween(-366)
    }
}