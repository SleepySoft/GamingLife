package com.sleepysoft.gaminglife

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import glcore.GlDailyStatistics
import glcore.GlDateTime
import java.util.Date


class DailyBrowseAdapter : RecyclerView.Adapter< DailyBrowseAdapter.DailyViewHolder >() {

    val dailyDataList = GlDailyStatistics.listDailyData()

    class DailyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var dataValid: Boolean = false
        var dailyDataDate: Date = Date()
        val dailyDataStat: GlDailyStatistics = GlDailyStatistics()

        val dateText: TextView = view.findViewById< TextView >(R.id.id_text_view_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_daily_data_item, parent, false)
        return DailyViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        val dateStr = dailyDataList[position]
        holder.dailyDataDate = GlDateTime.stringToDate(dateStr)
        holder.dataValid = holder.dailyDataStat.loadDailyData(holder.dailyDataDate)

        holder.dateText.text = dateStr
    }

    override fun getItemCount(): Int {
        return dailyDataList.size
    }
}


class DailyBrowseActivity : AppCompatActivity() {
    lateinit var mDailyDataList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_browse)

        mDailyDataList = findViewById(R.id.id_recycler_reiw_daily_list)
        mDailyDataList.layoutManager = LinearLayoutManager(this)
    }
}