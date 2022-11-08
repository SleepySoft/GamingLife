package glcore

import java.text.SimpleDateFormat
import java.util.*

object GlDateTime {

    fun datetime() : Date {
        return Calendar.getInstance().time
    }

    fun datetime(offsetDays: Int,
                 offsetHours: Int = 0,
                 offsetMinutes: Int = 0,
                 offsetSeconds: Int = 0) : Date {
        return Calendar.getInstance().apply {
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

    fun daysBetween(baseDate: Date, targetDate: Date) : Int {
        val deltaSecs: Long = zeroDateHMS(targetDate).time - zeroDateHMS(baseDate).time
        val deltaDays: Int = ((deltaSecs + 24 * 3600 - 1) / 24 * 3600).toInt()
        return deltaDays
    }

    fun zeroDateHMS(data: Date) : Date {
        return Calendar.getInstance().apply {
            time = Date()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time
    }
}