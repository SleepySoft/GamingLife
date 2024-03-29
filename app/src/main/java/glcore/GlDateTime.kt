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

    fun smartFormatSec(sec: Long) : String {
        val day: Long = sec / 3600 / 24
        val hour: Long = sec / 3600
        val remainingSec: Long = sec % 3600
        val minutes: Long = remainingSec / 60
        val seconds: Long = remainingSec % 60

        return when {
            day > 0L -> "%d %02d:%02d:%02d".format(day, hour, minutes, seconds)
            hour > 0L -> "%02d:%02d:%02d".format(hour, minutes, seconds)
            else -> "%02d:%02d".format(minutes, seconds)
        }
    }

    fun formatDateToDay(dt: Date) : String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(dt)
    }

    fun formatDateToSec(dt: Date) : String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(dt)
    }

    fun formatDateToMSec(dt: Date) : String {
        return SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(dt)
    }

    fun stringToDate(dtStr: String, dateDefault: Date = Date()) : Date {
        return  SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(dtStr) ?:
                SimpleDateFormat("yyyyMMdd HHmmss", Locale.getDefault()).parse(dtStr) ?:
                SimpleDateFormat("yyyyMMdd HHmmss SSS", Locale.getDefault()).parse(dtStr) ?:

                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).parse(dtStr) ?:
                SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).parse(dtStr) ?:

                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dtStr) ?:
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dtStr) ?:
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.getDefault()).parse(dtStr) ?: dateDefault
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
