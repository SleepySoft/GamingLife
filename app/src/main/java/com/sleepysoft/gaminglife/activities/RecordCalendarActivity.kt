package com.sleepysoft.gaminglife.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarLayout
import com.haibin.calendarview.CalendarView
import com.sleepysoft.gaminglife.R
import glcore.COLOR_SCHEME_EXTREME
import glcore.COLOR_SCHEME_NORMAL
import glcore.ENUM_TASK_CONCLUSION_FINISHED
import glcore.GlDailyRecord
import glcore.GlService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
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

        job = scope.launch(Dispatchers.Main) {
            val daysTaskFinished = withContext(Dispatchers.IO) {
                updateMonthlyDataForPeriodicTask(year, month)
            }
            job = null
            updateCalendarMarks(daysTaskFinished)
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

    private fun updateMonthlyDataForPeriodicTask(year: Int, month: Int) : List< Date > {
        val taskRecords = GlService.getPeriodicTaskInMonth(year, month)
        val filteredDates = taskRecords.filter {
            it.value.any { task ->
                task.id == mDisplayTaskId &&
                task.conclusion == ENUM_TASK_CONCLUSION_FINISHED
            } }.keys
        return filteredDates.toList()
    }

    private fun updateCalendarMarksByCurrentDate() {
        updateCalendarMarks(mCalendarView.curYear, mCalendarView.curMonth)
    }

/*    private fun updateDailyStatisticsByCalendar(calendar: Calendar) {
        val dateStr = "%04d%02d%02d".format(calendar.year, calendar.month, calendar.day)
        updateDailyStatistics(dateStr)
    }

    private fun newPlayer(uri: String) {
        *//*        val newSurfaceView = SurfaceView(this)
                val newSurfaceHolder = newSurfaceView.holder
                newSurfaceHolder.addCallback(this)*//*

        mToBePlayedMediaUri = uri
        *//*        mMediaController.setAnchorView(newSurfaceView)

                // ------------------ Replace the SurfaceView ------------------

                val index = mStatisticsLayout.indexOfChild(mSurfaceView)
                val layoutParams = mSurfaceView.layoutParams as LinearLayout.LayoutParams

                newSurfaceView.layoutParams = layoutParams

                mStatisticsLayout.removeView(mSurfaceView)
                mStatisticsLayout.addView(newSurfaceView, index)

                mSurfaceView = newSurfaceView
                mSurfaceHolder = newSurfaceHolder*//*
    }

    private fun doPlay(uri: String) {
        try {
            mMediaPlayer.setDataSource(uri)
            mMediaPlayer.prepare()
            // mMediaPlayer.start()
        } catch (e: Exception) {
            println("Play Error.")
            GlLog.e(e.stackTraceToString())
        } finally {

        }
    }

    private fun checkEndPlay() {
        try {
            mMediaPlayer.stop()
        } catch (e: Exception) {
            // println("Stop play Error.")
            // GlLog.e(e.stackTraceToString())
        } finally {

        }
    }*/
}
