package com.sleepysoft.gaminglife.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
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

    private lateinit var mTextYear: TextView
    private lateinit var mTextLunar: TextView
    private lateinit var mTextMonthDay: TextView
    private lateinit var mTextToday: TextView

    private lateinit var mCalendarView: CalendarView
    private lateinit var mCalendarLayout: CalendarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calendar)

        mTextYear = findViewById(R.id.tv_year)
        mTextLunar = findViewById(R.id.tv_lunar)
        mTextMonthDay = findViewById(R.id.tv_month_day)
        mTextToday = findViewById(R.id.text_today)

        mCalendarView = findViewById(R.id.calendarView)
        mCalendarLayout = findViewById(R.id.calendarLayout)

        // calendarLayout.expand() //展开
        // calendarLayout.shrink() //折叠

        mCalendarView.setOnCalendarSelectListener(this)

        mTextMonthDay.setOnClickListener(View.OnClickListener {
            if (!mCalendarLayout.isExpand) {
                mCalendarLayout.expand()
                return@OnClickListener
            }
            val year = mCalendarView.selectedCalendar.year
            mCalendarView.showYearSelectLayout(year)
            mTextLunar.visibility = View.GONE
            mTextYear.visibility = View.GONE
            mTextMonthDay.text = year.toString()
        })

        mTextToday.setOnClickListener(View.OnClickListener {
            mCalendarView.scrollToCurrent()
        })

        val schemeCalendar = Calendar()
        schemeCalendar.year = 2023
        schemeCalendar.month = 3
        schemeCalendar.day = 16

/*        val customScheme = CustomScheme()
        customScheme.schemeColor = Color.RED // 设置标记颜色

        schemeCalendar.addScheme(customScheme)*/

        mCalendarView.addSchemeDate(schemeCalendar)

        updateDisplay()
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

        mTextLunar.visibility = View.VISIBLE
        mTextYear.visibility = View.VISIBLE

        updateDisplay()
    }

    override fun onWeekChange(weekCalendars: MutableList<Calendar>?) {
        TODO("Not yet implemented")
    }

    override fun onMonthChange(year: Int, month: Int) {

    }

    private fun updateDisplay() {
        val calendar: Calendar = mCalendarView.selectedCalendar

        mTextLunar.text = calendar.lunar
        mTextYear.text = mCalendarView.curYear.toString()
        mTextMonthDay.text = resources.getString(
            R.string.FORMAT_CALENDAR_M_D).format(
            mCalendarView.curMonth, mCalendarView.curDay)
    }
}