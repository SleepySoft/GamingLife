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
}