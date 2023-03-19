package com.sleepysoft.gaminglife.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarLayout
import com.haibin.calendarview.CalendarView
import com.sleepysoft.gaminglife.R


class DailyCalendarActivity
    : AppCompatActivity(),
      CalendarView.OnWeekChangeListener,
      CalendarView.OnMonthChangeListener,
      CalendarView.OnCalendarSelectListener {

    private lateinit var mCalendarView: CalendarView
    private lateinit var mCalendarLayout: CalendarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calendar)

        mCalendarView = findViewById(R.id.calendarView)
        mCalendarLayout = findViewById(R.id.calendarLayout)

        // calendarLayout.expand() //展开
        // calendarLayout.shrink() //折叠

        // 响应用户点击事件
        mCalendarView.setOnCalendarSelectListener(this)


        val schemeCalendar = Calendar()
        schemeCalendar.year = 2023
        schemeCalendar.month = 3
        schemeCalendar.day = 16

/*        val customScheme = CustomScheme()
        customScheme.schemeColor = Color.RED // 设置标记颜色

        schemeCalendar.addScheme(customScheme)*/

        mCalendarView.addSchemeDate(schemeCalendar)
    }

    // ---------------------------------------------------------------------------------------------

    override fun onCalendarOutOfRange(calendar: Calendar?) {
        TODO("Not yet implemented")
    }

    override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
        // 在这里处理用户点击事件
        // calendar.year 获取年份
        // calendar.month 获取月份
        // calendar.day 获取日期
    }

    override fun onWeekChange(weekCalendars: MutableList<Calendar>?) {
        TODO("Not yet implemented")
    }

    override fun onMonthChange(year: Int, month: Int) {
        TODO("Not yet implemented")
    }
}