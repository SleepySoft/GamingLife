package glcore

import org.junit.Test
import java.util.*
import org.junit.Assert.assertEquals
import java.text.SimpleDateFormat
import java.util.Locale


internal class GlDatTimeTest {

    private fun printDateTime(ts: Date) {
        println(ts)
        println(GlDateTime.formatDateToDay(ts))
        println(GlDateTime.formatDateToSec(ts))
        println(GlDateTime.formatDateToMSec(ts))
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

    @Test
    fun testWeekStartTimeStamp() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date1 = dateFormat.parse("2023-05-11")
        val expectedTimestamp1 = dateFormat.parse("2023-05-08")?.time
        assertEquals(expectedTimestamp1, GlDateTime.weekStartTimeStamp(date1))

        val date2 = dateFormat.parse("2023-01-01")
        val expectedTimestamp2 = dateFormat.parse("2022-12-26")?.time
        assertEquals(expectedTimestamp2, GlDateTime.weekStartTimeStamp(date2))

        val date3 = dateFormat.parse("2023-12-31")
        val expectedTimestamp3 = dateFormat.parse("2023-12-25")?.time
        assertEquals(expectedTimestamp3, GlDateTime.weekStartTimeStamp(date3))

        val date4 = dateFormat.parse("2023-02-28")
        val expectedTimestamp4 = dateFormat.parse("2023-02-27")?.time
        assertEquals(expectedTimestamp4, GlDateTime.weekStartTimeStamp(date4))
    }

    @Test
    fun testMonthStartTimeStamp() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date1 = dateFormat.parse("2023-05-11")
        val expectedTimestamp1 = dateFormat.parse("2023-05-01")?.time
        assertEquals(expectedTimestamp1, GlDateTime.monthStartTimeStamp(date1))

        val date2 = dateFormat.parse("2023-01-31")
        val expectedTimestamp2 = dateFormat.parse("2023-01-01")?.time
        assertEquals(expectedTimestamp2, GlDateTime.monthStartTimeStamp(date2))

        val date3 = dateFormat.parse("2023-12-31")
        val expectedTimestamp3 = dateFormat.parse("2023-12-01")?.time
        assertEquals(expectedTimestamp3, GlDateTime.monthStartTimeStamp(date3))

        val date4 = dateFormat.parse("2024-02-29")
        val expectedTimestamp4 = dateFormat.parse("2024-02-01")?.time
        assertEquals(expectedTimestamp4, GlDateTime.monthStartTimeStamp(date4))
    }

    @Test
    fun testQuarterStartTimeStamp() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date1 = dateFormat.parse("2023-05-11")
        val expectedTimestamp1 = dateFormat.parse("2023-04-01")?.time
        assertEquals(expectedTimestamp1, GlDateTime.quarterStartTimeStamp(date1))

        val date2 = dateFormat.parse("2023-01-31")
        val expectedTimestamp2 = dateFormat.parse("2023-01-01")?.time
        assertEquals(expectedTimestamp2, GlDateTime.quarterStartTimeStamp(date2))

        val date3 = dateFormat.parse("2023-12-31")
        val expectedTimestamp3 = dateFormat.parse("2023-10-01")?.time
        assertEquals(expectedTimestamp3, GlDateTime.quarterStartTimeStamp(date3))

        val date4 = dateFormat.parse("2024-02-29")
        val expectedTimestamp4 = dateFormat.parse("2024-01-01")?.time
        assertEquals(expectedTimestamp4, GlDateTime.quarterStartTimeStamp(date4))
    }
}