package com.sleepysoft.gaminglife.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarLayout
import com.haibin.calendarview.CalendarView
import com.sleepysoft.gaminglife.R
import glcore.ENUM_TASK_CONCLUSION_FINISHED
import glcore.ENUM_TASK_CONCLUSION_PARTIAL
import glcore.GlService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date


// =================================================================================================
// The usage and layout of CalendarView is referenced to:
//     https://github.com/huanghaibin-dev/CalendarView
// Any many thanks to new bing. It helps me a lot on this file.
// =================================================================================================

class RecordCalendarActivity
    : AppCompatActivity(),
    CalendarView.OnWeekChangeListener,
    CalendarView.OnMonthChangeListener,
    CalendarView.OnCalendarSelectListener {

    // ----------------------------------------------

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var mDisplayTaskId = ""


    private lateinit var mTextYear: TextView
    private lateinit var mTextLunar: TextView
    private lateinit var mTextMonthDay: TextView
    private lateinit var mTextToday: TextView
    private lateinit var mLayoutLoading: LinearLayout

    private lateinit var mCalendarView: CalendarView
    private lateinit var mRelativeLayout: RelativeLayout
    private lateinit var mCalendarLayout: CalendarLayout

    // ----------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_calendar)

        mDisplayTaskId = intent.getStringExtra("task_id").toString()

        // -----------------------------------------------------------------------------------------

        mTextYear = findViewById(R.id.tv_year)
        mTextLunar = findViewById(R.id.tv_lunar)
        mTextMonthDay = findViewById(R.id.tv_month_day)
        mTextToday = findViewById(R.id.text_today)
        mLayoutLoading = findViewById(R.id.layout_loading)

        mCalendarView = findViewById(R.id.calendarView)
        mRelativeLayout = findViewById(R.id.rl_tool)
        mCalendarLayout = findViewById(R.id.calendarLayout)

        // -----------------------------------------------------------------------------------------

        mCalendarView.setOnCalendarSelectListener(this)
        mCalendarView.setOnMonthChangeListener(this)

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

        updateCalendarTopDisplay()
    }

    override fun onResume() {
        super.onResume()
        updateCalendarMarksByCurrentDate()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    // ---------------------------------------------------------------------------------------------

    override fun onCalendarOutOfRange(calendar: Calendar?) {

    }

    override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
        calendar?.run {
            // TODO:
        }

        mTextLunar.visibility = View.VISIBLE
        mTextYear.visibility = View.VISIBLE

        updateCalendarTopDisplay()
    }

    override fun onWeekChange(weekCalendars: MutableList<Calendar>?) {

    }

    override fun onMonthChange(year: Int, month: Int) {
        updateCalendarMarks(year, month)
    }

    // ---------------------------------------------------------------------------------------------

    private fun updateCalendarMarks(year: Int, month: Int) {
        job?.cancel()
        mCalendarView.clearSchemeDate()

        mLayoutLoading.visibility = View.VISIBLE

        job = scope.launch(Dispatchers.Main) {
            val daysTaskFinished = withContext(Dispatchers.IO) {
                updateMonthlyDataForPeriodicTask(year, month)
            }
            job = null
            updateCalendarMarks(daysTaskFinished)
            mLayoutLoading.visibility = View.GONE
        }
    }

    private fun updateCalendarMarks(daysTaskFinished: List< Date >) {
        val schemeMap = mutableMapOf< String, Calendar>()

        for (d in daysTaskFinished) {
            val cal = java.util.Calendar.getInstance().apply { time = d }

            val calendar = Calendar()
            calendar.year = cal.get(java.util.Calendar.YEAR)
            calendar.month = cal.get(java.util.Calendar.MONTH) + 1
            calendar.day = cal.get(java.util.Calendar.DAY_OF_MONTH)
            calendar.scheme = "\uD83D\uDC51"
            calendar.schemeColor = Color.parseColor("#FFFFFF")
            schemeMap[calendar.toString()] = calendar
        }

        mCalendarView.clearSchemeDate()
        mCalendarView.setSchemeDate(schemeMap)
    }

    private fun updateCalendarTopDisplay() {
        val calendar: Calendar = mCalendarView.selectedCalendar

        mTextLunar.text = calendar.lunar
        mTextYear.text = mCalendarView.curYear.toString()
        mTextMonthDay.text = resources.getString(
            R.string.FORMAT_CALENDAR_M_D).format(calendar.month, calendar.day)
    }

    // ---------------------------------------------------------------------------------------------

    private fun updateMonthlyDataForPeriodicTask(year: Int, month: Int) : List<Date> {
        val taskRecords = GlService.getPeriodicTaskInMonth(year, month)
        val filteredDates = taskRecords.filterValues {
            it.any { task ->
                task.id == mDisplayTaskId && (task.conclusion == ENUM_TASK_CONCLUSION_FINISHED ||
                                              task.conclusion == ENUM_TASK_CONCLUSION_PARTIAL)
            }
        }.keys
        return filteredDates.toList()
    }


    private fun updateCalendarMarksByCurrentDate() {
        updateCalendarMarks(mCalendarView.curYear, mCalendarView.curMonth)
    }
}
