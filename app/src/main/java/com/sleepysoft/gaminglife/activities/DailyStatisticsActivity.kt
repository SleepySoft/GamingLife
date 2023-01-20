package com.sleepysoft.gaminglife

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sleepysoft.gaminglife.controllers.*
import com.sleepysoft.gaminglife.views.GlView
import glcore.*
import graphengine.GraphView
import java.lang.ref.WeakReference


class DailyExtFileAdapter(
    private val dailyStatistics: GlDailyRecord,
    private val dailyStatisticsActivity: DailyStatisticsActivity)
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
                dailyStatisticsActivity.onShowStatistics()
            }
        }
        else {
            val fileName = dailyStatistics.dailyExtraFiles[position - 1].removePrefix(DAILY_FOLDER_PREFIX)
            holder.dataValid = true
            holder.extFileName = fileName
            holder.fileActionButton.text = fileName
            holder.fileActionButton.setOnClickListener {
                dailyStatisticsActivity.onShowExtFile(fileName)
            }
        }
    }

    override fun getItemCount(): Int {
        return dailyStatistics.dailyExtraFiles.size + 1
    }
}


// -------------------------------------------------------------------------------------------------

class DailyStatisticsActivity : AppCompatActivity() {
    val mDailyRecord = GlDailyRecord()

    lateinit var mMdViewer: TextView
    lateinit var mStatisticsView: GlView
    lateinit var mDailyExtFileList: RecyclerView

    private val mCtrlContext = GlControllerContext()
    lateinit var mDailyStatisticsController: GlDailyStatisticsController
    private lateinit var timeViewEditorController: GlTimeViewEditorController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_statistics)

        val dateStr: String = intent.getStringExtra("dateStr") ?: "日期错误"
        mDailyRecord.loadDailyRecord(GlDateTime.stringToDate(dateStr))

        title = "GamingLife - 回顾 ($dateStr)"

        mMdViewer = findViewById(R.id.id_text_view_md)
        mStatisticsView = findViewById(R.id.id_view_statistics)

        mDailyExtFileList = findViewById(R.id.id_recycler_view_ext_files)

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
    }

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
}