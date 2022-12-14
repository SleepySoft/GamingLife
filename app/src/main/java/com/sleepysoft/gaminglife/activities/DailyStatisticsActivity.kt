package com.sleepysoft.gaminglife

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import glcore.DAILY_FOLDER_PREFIX
import glcore.GlDailyRecord
import glcore.GlDateTime
import glcore.GlFile


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
    lateinit var mMdViewer: TextView
    lateinit var mStatisticsView: View
    lateinit var mDailyExtFileList: RecyclerView
    var mDailyStatistics: GlDailyRecord = GlDailyRecord()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_statistics)

        val dateStr: String = intent.getStringExtra("dateStr") ?: "日期错误"
        mDailyStatistics.loadDailyRecord(GlDateTime.stringToDate(dateStr))

        title = "GamingLife - 回顾 ($dateStr)"

        mMdViewer = findViewById(R.id.id_text_view_md)
        mStatisticsView = findViewById(R.id.id_view_statistics)

        mDailyExtFileList = findViewById(R.id.id_recycler_view_ext_files)
        mDailyExtFileList.layoutManager = LinearLayoutManager(this)
        mDailyExtFileList.adapter = DailyExtFileAdapter(mDailyStatistics, this)
        mDailyExtFileList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL))
    }

    fun onShowStatistics() {

    }

    fun onShowExtFile(fileName: String) {
        if (fileName.lowercase().endsWith(".md")) {
            val filePath= GlFile.joinPaths(mDailyStatistics.dailyPath, fileName)
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
}