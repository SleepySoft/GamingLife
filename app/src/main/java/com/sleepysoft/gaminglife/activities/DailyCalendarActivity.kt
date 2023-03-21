package com.sleepysoft.gaminglife.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.widget.*
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


class NotHideMediaController(context: Context) : MediaController(context) {
    override fun hide() {
        // Do not hide
    }

    fun doHide() {
        super.hide()
    }
}


// =================================================================================================
// The usage and layout of CalendarView is referenced to:
//     https://github.com/huanghaibin-dev/CalendarView
// Any many thanks to new bing. It helps me a lot on this file.
// =================================================================================================

class DailyCalendarActivity
    : AppCompatActivity(),
      SurfaceHolder.Callback,
      CalendarView.OnWeekChangeListener,
      CalendarView.OnMonthChangeListener,
      CalendarView.OnCalendarSelectListener {

    companion object {
        // Workaround: For the orientation change
        private var selectDateStr: String = ""
    }

    // ----------------------------------------------

    private var mOrientation = Configuration.ORIENTATION_PORTRAIT

    private val mDailyRecord = GlDailyRecord()
    private val mDailyDataList = GlDailyRecord.listArchivedDailyData()

    private val mCtrlContext = GlControllerContext()
    lateinit var mDailyStatisticsController: GlDailyStatisticsController
    private lateinit var timeViewEditorController: GlTimeViewEditorController

    lateinit var mMdViewer: TextView
    lateinit var mStatisticsView: GlView
    lateinit var mDailyExtFileList: RecyclerView
    lateinit var mStatisticsLayout: LinearLayout
    lateinit var mDailyExtFileListAdapter: DailyExtFileAdapter

    var mMediaPlayer = MediaPlayer()
    var mToBePlayedMediaUri = ""

    lateinit var mSurfaceView: SurfaceView
    lateinit var mSurfaceHolder: SurfaceHolder
    lateinit var mMediaController : MediaController

    // ----------------------------------------------

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
        setContentView(R.layout.activity_daily_calendar)

        // -----------------------------------------------------------------------------------------

        mTextYear = findViewById(R.id.tv_year)
        mTextLunar = findViewById(R.id.tv_lunar)
        mTextMonthDay = findViewById(R.id.tv_month_day)
        mTextToday = findViewById(R.id.text_today)

        mCalendarView = findViewById(R.id.calendarView)
        mRelativeLayout = findViewById(R.id.rl_tool)
        mCalendarLayout = findViewById(R.id.calendarLayout)

        // -----------------------------------------------------------------------------------------

        mMdViewer = findViewById(R.id.id_text_view_md)
        mDailyExtFileList = findViewById(R.id.id_recycler_view_ext_files)
        mStatisticsLayout = findViewById(R.id.liner_statistics)

        mSurfaceView = findViewById(R.id.surface_view_av)
        mMediaController = MediaController(this)
        mMediaController.setAnchorView(mSurfaceView)

        mDailyExtFileListAdapter = DailyExtFileAdapter(mDailyRecord, this)

        // --------------------------------------------------------

        mOrientation = this.resources.configuration.orientation

        mStatisticsView = if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            findViewById(R.id.id_view_statistics)
        } else {
            // If landscape, the calendar will not be selected.
            // So we have to init the mDailyRecord manually.
            // TODO: Do Refactor for the process logic

            if (selectDateStr.isNotEmpty()) {
                mDailyRecord.loadDailyRecord(GlDateTime.stringToDate(selectDateStr))
            } else {
                val now = GlDateTime.datetime()
                mDailyRecord.loadDailyRecord(now)
                selectDateStr = GlDateTime.formatDateToDay(now)
            }
            findViewById(R.id.id_view_statistics_landscape)
        }

        buildContextAndController(mOrientation)
        mStatisticsView.visibility = View.VISIBLE
        mStatisticsView.graphView = mCtrlContext.graphView

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            initAsPortrait()
        } else {
            initAsLandscape()
        }
    }

    private fun initAsPortrait() {

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

        // --------------------------------------------------------

        mSurfaceHolder = mSurfaceView.holder
        mSurfaceHolder.addCallback(this)

        with(mDailyExtFileList) {
            adapter = mDailyExtFileListAdapter
            layoutManager = LinearLayoutManager(this@DailyCalendarActivity)
            addItemDecoration(
                DividerItemDecoration(
                    this@DailyCalendarActivity, DividerItemDecoration.HORIZONTAL))
        }

        // -----------------------------------------------------

        onShowStatistics()
        updateCalendarTopDisplay()
        updateCalendarMarksByCurrentDate()
        updateDailyStatisticsByCalendar(mCalendarView.selectedCalendar)
    }

    private fun initAsLandscape() {
        mRelativeLayout.visibility = View.GONE
        mCalendarLayout.visibility = View.GONE

        updateDailyStatistics(selectDateStr)

/*        mTextYear.visibility = View.GONE
        mTextLunar.visibility = View.GONE
        mTextMonthDay.visibility = View.GONE
        mTextToday.visibility = View.GONE
        mCalendarView.visibility = View.GONE
        mMediaController.visibility = View.GONE

        mDailyExtFileList.visibility = View.GONE*/
    }

/*    override fun onPause() {
        super.onPause()
        mMediaController.doHide()
    }*/

    // ---------------------------------------------------------------------------------------------

    fun onShowStatistics() {
        mMdViewer.visibility = View.GONE
        mSurfaceView.visibility = View.GONE
        mStatisticsView.visibility = View.VISIBLE
    }

    fun onShowExtFile(fileName: String) {
        mStatisticsView.visibility = View.GONE
        mSurfaceView.visibility = View.GONE
        mMdViewer.visibility = View.GONE

        checkEndPlay()
        val filePath = GlFile.joinPaths(mDailyRecord.dailyPath, fileName)
        val fileAbsPath = GlFile.absPath(filePath)

        when {
            fileName.lowercase().endsWith(".md") -> {
                val fileData = GlFile.loadFile(filePath)
                val fileText = fileData.decodeToString()
                mMdViewer.visibility = View.VISIBLE
                mMdViewer.text = fileText
            }
            fileName.lowercase().endsWith(".wav") -> {
                mSurfaceView.visibility = View.VISIBLE
                newPlayer(fileAbsPath)
            }
            else -> {
                // No support yet.
                mMdViewer.visibility = View.VISIBLE
                mMdViewer.text = "不支持的文件类型"
            }
        }
    }

    private fun buildContextAndController(orientation: Int) {
        mCtrlContext.view = WeakReference(mStatisticsView)
        mCtrlContext.context = WeakReference(this)
        mCtrlContext.graphView = GraphView(mCtrlContext)

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mDailyStatisticsController = GlDailyStatisticsController(
                mCtrlContext, mDailyRecord).apply { init() }
        } else {
            timeViewEditorController = GlTimeViewEditorController(
                mCtrlContext, mDailyRecord, GlRoot.systemConfig).apply { init() }
        }
    }

    // ---------------------------------------------------------------------------------------------

    override fun onCalendarOutOfRange(calendar: Calendar?) {

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

    }

    override fun onMonthChange(year: Int, month: Int) {
        updateCalendarMarks(year, month)
    }

    // ---------------------------------------------------------------------------------------------

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (mOrientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        mMediaPlayer = MediaPlayer()
        mMediaPlayer.setDisplay(holder)

        mMediaController.setMediaPlayer(object : MediaController.MediaPlayerControl {
            override fun start() { mMediaPlayer.start() }
            override fun pause() { mMediaPlayer.pause() }
            override fun getDuration(): Int { return mMediaPlayer.duration }
            override fun getCurrentPosition(): Int { return mMediaPlayer.currentPosition }
            override fun seekTo(pos: Int) { mMediaPlayer.seekTo(pos) }
            override fun isPlaying(): Boolean { return mMediaPlayer.isPlaying }
            override fun getBufferPercentage(): Int { return 0 }
            override fun canPause(): Boolean { return true }
            override fun canSeekBackward(): Boolean { return true }
            override fun canSeekForward(): Boolean { return true }
            override fun getAudioSessionId(): Int { return 0 }
        })

        if (mToBePlayedMediaUri.isNotEmpty()) {
            doPlay(mToBePlayedMediaUri)
        }

        mMediaController.show()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            try {
                mMediaPlayer.release()
            } catch (e: Exception) {
                println("Play Release Error.")
                GlLog.e(e.stackTraceToString())
            } finally {

            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun updateCalendarMarks(year: Int, month: Int) {
        val prefix = String.format("$DAILY_FOLDER_PREFIX%04d%02d", year, month)
        val daysWithData = mDailyDataList.filter { it.startsWith(prefix) }
        val daysDateWithData = daysWithData.map { it.removePrefix(DAILY_FOLDER_PREFIX) }

        val schemeMap = mutableMapOf< String, Calendar >()

        for (dateStr in daysDateWithData) {
            val calendar = Calendar()
            val extFiles = GlDailyRecord.listDailyExtraFiles(dateStr)
            val dayInt = dateStr.substring(dateStr.length - 2).toInt()

            calendar.year = year
            calendar.month = month
            calendar.day = dayInt
            calendar.scheme = "GL"
            calendar.schemeColor = Color.parseColor(
                if (extFiles.isNotEmpty()) COLOR_SCHEME_EXTREME else COLOR_SCHEME_NORMAL)

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

    @SuppressLint("NotifyDataSetChanged")
    private fun updateDailyStatistics(dateStr: String) {
        title = resources.getString(R.string.TITLE_STATISTICS).format(dateStr)

        if (mDailyRecord.loadDailyRecord(GlDateTime.stringToDate(dateStr))) {
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                selectDateStr = dateStr
                mStatisticsLayout.visibility = View.VISIBLE
                mDailyExtFileListAdapter.notifyDataSetChanged()
                mDailyStatisticsController.updateDailyRecord(mDailyRecord)
            } else {
                // Will not update on portrait mode.
                // timeViewEditorController
            }
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

    private fun newPlayer(uri: String) {
/*        val newSurfaceView = SurfaceView(this)
        val newSurfaceHolder = newSurfaceView.holder
        newSurfaceHolder.addCallback(this)*/

        mToBePlayedMediaUri = uri
/*        mMediaController.setAnchorView(newSurfaceView)

        // ------------------ Replace the SurfaceView ------------------

        val index = mStatisticsLayout.indexOfChild(mSurfaceView)
        val layoutParams = mSurfaceView.layoutParams as LinearLayout.LayoutParams

        newSurfaceView.layoutParams = layoutParams

        mStatisticsLayout.removeView(mSurfaceView)
        mStatisticsLayout.addView(newSurfaceView, index)

        mSurfaceView = newSurfaceView
        mSurfaceHolder = newSurfaceHolder*/
    }

    private fun doPlay(uri: String) {
        try {
            mMediaPlayer.setDataSource(uri)
            mMediaPlayer.prepare()
            mMediaPlayer.start()
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
    }
}
