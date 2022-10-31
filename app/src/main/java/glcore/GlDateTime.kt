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
}