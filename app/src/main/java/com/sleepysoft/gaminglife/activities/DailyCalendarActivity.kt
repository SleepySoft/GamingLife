package com.sleepysoft.gaminglife.activities

import android.R.color
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarLayout
import com.haibin.calendarview.CalendarView
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.controllers.*
import com.sleepysoft.gaminglife.views.GlView
import glcore.*
import graphengine.GraphView
import java.lang.ref.WeakReference
import java.util.*


class DailyExtFileAdapter(
    private val dailyStatistics: GlDailyRecord,
    private val dailyCalendarActivity: DailyCalendarActivity)
    : RecyclerView.Adapter< DailyExtFileAdapter.DailyStatisticsHolder >() {

    class DailyStatisticsHolder(view: View) : RecyclerView.ViewHolder(view) {
        var dataValid: Boolean = false
        var extFileName: String = ""

        val fileActionButton: Button = view.findViewById(R.id.id_button_item_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyStatisticsHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_recycler_view_item, parent, false)
        return DailyStatisticsHolder(view)
    }

    override fun onBindViewHolder(holder: DailyStatisticsHolder, position: Int) {
        if (position == 0) {
            holder.dataValid = true
            holder.extFileName = ""
            holder.fileActionButton.text = "统计信息"
            holder.fileActionButton.setOnClickListener {
                dailyCalendarActivity.onShowStatistics()
                // dailyStatisticsActivity.onTestShowNextDayStatistics()
            }
        }
        else {
            val fileName = dailyStatistics.dailyExtraFiles[position - 1].removePrefix(DAILY_FOLDER_PREFIX)
            holder.dataValid = true
            holder.extFileName = fileName
            holder.fileActionButton.text = fileName
            holder.fileActionButton.setOnClickListener {
                dailyCalendarActivity.onShowExtFile(fileName)
            }
        }
    }

    override fun getItemCount(): Int {
        return dailyStatistics.dailyExtraFiles.size + 1
    }
}


// =================================================================================================
// -------------------------------------------------------------------------------------------------
// =================================================================================================

class DailyCalendarActivity
    : AppCompatActivity(),
      CalendarView.OnWeekChangeListener,
      CalendarView.OnMonthChangeListener,
      CalendarView.OnCalendarSelectListener {

    private val mDailyRecord = GlDailyRecord()
    private val mDailyDataList = GlDailyRecord.listArchivedDailyData()

    private val mCtrlContext = GlControllerContext()
    lateinit var mDailyStatisticsController: GlDailyStatisticsController
    private lateinit var timeViewEditorController: GlTimeViewEditorController

    lateinit var mMdViewer: TextView
    lateinit var mStatisticsView: GlView
    lateinit var mDailyExtFileList: RecyclerView
    lateinit var mStatisticsLayout: LinearLayout

    private lateinit var mTextYear: TextView
    private lateinit var mTextLunar: TextView
    private lateinit var mTextMonthDay: TextView
    private lateinit var mTextToday: TextView

    private lateinit var mCalendarView: CalendarView
    private lateinit var mCalendarLayout: CalendarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calendar)

        // -----------------------------------------------------------------------------------------

        mTextYear = findViewById(R.id.tv_year)
        mTextLunar = findViewById(R.id.tv_lunar)
        mTextMonthDay = findViewById(R.id.tv_month_day)
        mTextToday = findViewById(R.id.text_today)

        mCalendarView = findViewById(R.id.calendarView)
        mCalendarLayout = findViewById(R.id.calendarLayout)

        // calendarLayout.expand() //展开
        // calendarLayout.shrink() //折叠

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

/*        val schemeCalendar = Calendar()
        schemeCalendar.year = 2023
        schemeCalendar.month = 3
        schemeCalendar.day = 16

        val customScheme = CustomScheme()
        customScheme.schemeColor = Color.RED // 设置标记颜色

        schemeCalendar.addScheme(customScheme)

        mCalendarView.addSchemeDate(schemeCalendar)*/

        updateCalendarTopDisplay()

        // -----------------------------------------------------------------------------------------

        mMdViewer = findViewById(R.id.id_text_view_md)
        mStatisticsView = findViewById(R.id.id_view_statistics)
        mDailyExtFileList = findViewById(R.id.id_recycler_view_ext_files)
        mStatisticsLayout = findViewById(R.id.liner_statistics)

        val orientation = this.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mDailyExtFileList.layoutManager =
                LinearLayoutManager(this)
            mDailyExtFileList.adapter = DailyExtFileAdapter(mDailyRecord, this)
            mDailyExtFileList.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.HORIZONTAL
                )
            )
        } else {
            mDailyExtFileList.visibility = View.GONE
        }

        buildContextAndController(orientation)

        mStatisticsView.graphView = mCtrlContext.graphView

        onShowStatistics()
        updateCalendarMarksByCurrentDate()
        updateDailyStatisticsByCalendar(mCalendarView.selectedCalendar)
    }

    // ---------------------------------------------------------------------------------------------

    fun onShowStatistics() {
        mMdViewer.visibility = View.GONE
        mStatisticsView.visibility = View.VISIBLE
    }

    fun onShowExtFile(fileName: String) {
        mMdViewer.visibility = View.VISIBLE
        mStatisticsView.visibility = View.GONE

        if (fileName.lowercase().endsWith(".md")) {
            val filePath= GlFile.joinPaths(mDailyRecord.dailyPath, fileName)
            val fileData = GlFile.loadFile(filePath)
            val fileText = fileData.decodeToString()
            mMdViewer.text = fileText
        } else if (fileName.lowercase().endsWith(".wav")) {
            // TODO: Play wav
            mMdViewer.text = "音频文件，还未支持"
        } else {
            // No support yet.
            mMdViewer.text = "不支持的文件类型"
        }
    }

    private fun buildContextAndController(orientation: Int) {
        mCtrlContext.view = WeakReference(mStatisticsView)
        mCtrlContext.context = WeakReference(this)
        mCtrlContext.graphView = GraphView(mCtrlContext)

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mDailyStatisticsController = GlDailyStatisticsController(mCtrlContext, mDailyRecord).apply { init() }
        } else {
            timeViewEditorController = GlTimeViewEditorController(
                mCtrlContext, mDailyRecord, GlRoot.systemConfig).apply { init() }
        }
    }

    // ---------------------------------------------------------------------------------------------

    override fun onCalendarOutOfRange(calendar: Calendar?) {
        TODO("Not yet implemented")
    }

    override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
        calendar?.run {
            updateDailyStatisticsByCalendar(this)
        }

        mTextLunar.visibility = View.VISIBLE
        mTextYear.visibility = View.VISIBLE

        updateCalendarTopDisplay()
    }

    override fun onWeekChange(weekCalendars: MutableList<Calendar>?) {
        TODO("Not yet implemented")
    }

    override fun onMonthChange(year: Int, month: Int) {
        updateCalendarMarks(year, month)
    }

    // ---------------------------------------------------------------------------------------------

    private fun updateCalendarMarks(year: Int, month: Int) {
        val prefix = String.format("$DAILY_FOLDER_PREFIX%04d%02d", year, month)
        val daysWithData = mDailyDataList.filter { it.startsWith(prefix) }

        val schemeMap = mutableMapOf< String, Calendar >()

        for (dateStr in daysWithData) {
            val calendar = Calendar()
            val dayInt = dateStr.substring(dateStr.length - 2).toInt()

            calendar.year = year
            calendar.month = month
            calendar.day = dayInt
            calendar.schemeColor = Color.parseColor(COLOR_SCHEME)
            calendar.scheme = "GL"

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

    private fun updateDailyStatistics(dateStr: String) {
        title = resources.getString(R.string.TITLE_STATISTICS).format(dateStr)
        if (mDailyRecord.loadDailyRecord(GlDateTime.stringToDate(dateStr))) {
            mStatisticsLayout.visibility = View.VISIBLE
            mDailyStatisticsController.updateDailyRecord(mDailyRecord)
        } else {
            mStatisticsLayout.visibility = View.INVISIBLE
        }
    }

    private fun updateDailyStatisticsByCalendar(calendar: Calendar) {
        val dateStr = "%04d%02d%02d".format(calendar.year, calendar.month, calendar.day)
        updateDailyStatistics(dateStr)
    }

    private fun updateCalendarMarksByCurrentDate() {
        updateCalendarMarks(mCalendarView.curYear, mCalendarView.curMonth)
    }
}
