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
import glcore.DAILY_FOLDER_PREFIX
import glcore.GlDailyStatistics
import glcore.GlDateTime


class DailyExtFileAdapter(private val dailyStatistics: GlDailyStatistics) :
    RecyclerView.Adapter< DailyExtFileAdapter.DailyStatisticsHolder >() {

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
        val fileName = dailyStatistics.dailyExtraFiles[position].removePrefix(DAILY_FOLDER_PREFIX)
        holder.dataValid = true
        holder.extFileName = fileName
        holder.fileActionButton.text = fileName
        holder.fileActionButton.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return dailyStatistics.dailyExtraFiles.size
    }
}


class DailyStatisticsActivity : AppCompatActivity() {
    lateinit var mDailyExtFileList: RecyclerView
    var mDailyStatistics: GlDailyStatistics = GlDailyStatistics()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_statistics)

        val dateStr: String = intent.getStringExtra("dateStr") ?: "日期错误"
        mDailyStatistics.loadDailyData(GlDateTime.stringToDate(dateStr))

        title = "GamingLife - 回顾 ($dateStr)"

        mDailyExtFileList = findViewById(R.id.id_recycler_view_ext_files)
        mDailyExtFileList.layoutManager = LinearLayoutManager(this)
        mDailyExtFileList.adapter = DailyExtFileAdapter(mDailyStatistics)
        mDailyExtFileList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL))
    }
}