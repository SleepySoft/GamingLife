package glcore

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

object GlDateTime {

    var debugDateTime: Date? = null

    fun datetime() : Date {
        return debugDateTime ?: Calendar.getInstance().time
    }

    fun datetime(offsetDays: Int,
                 offsetHours: Int = 0,
                 offsetMinutes: Int = 0,
                 offsetSeconds: Int = 0) : Date {
        return Calendar.getInstance().apply {
            this.time = debugDateTime ?: Date()
            this.add(Calendar.DAY_OF_YEAR, offsetDays)
            this.add(Calendar.HOUR_OF_DAY, offsetHours)
            this.add(Calendar.MINUTE, offsetMinutes)
            this.add(Calendar.SECOND, offsetSeconds)
        }.time
    }

    fun formatToDay(dt: Date) : String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(dt)
    }

    fun formatToSec(dt: Date) : String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(dt)
    }

    fun formatToMSec(dt: Date) : String {
        return SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(dt)
    }

    // ---------------------------------------------------------------------

    fun daysBetween(baseDate: Date, targetDate: Date) : Int {
        val deltaSecs: Long = zeroDateHMS(targetDate).time - zeroDateHMS(baseDate).time
        val deltaDays: Int = ceil(deltaSecs.toFloat() / 24.0f / 3600.0f / 1000.0f).toInt()
        return deltaDays
    }

    fun zeroDateHMS(date: Date) : Date {
        return Calendar.getInstance().apply {
            this.time = date
            this.set(Calendar.HOUR_OF_DAY, 0)
            this.set(Calendar.MINUTE, 0)
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun dayStartTimeStamp(offsetDays: Int = 0) = zeroDateHMS(datetime(offsetDays)).time
}