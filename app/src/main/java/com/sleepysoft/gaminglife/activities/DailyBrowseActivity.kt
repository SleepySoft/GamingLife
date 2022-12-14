package com.sleepysoft.gaminglife

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import glcore.DAILY_FOLDER_PREFIX
import glcore.GlDailyRecord
import glcore.GlDateTime
import java.util.Date


class DailyBrowseAdapter(val context: Context) :
    RecyclerView.Adapter< DailyBrowseAdapter.DailyViewHolder >() {

    val dailyDataList = GlDailyRecord.listArchivedDailyData()

    class DailyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var dataValid: Boolean = false
        var dailyDataDate: Date = Date()
        val dailyDataStat: GlDailyRecord = GlDailyRecord()

        val dateButton: Button = view.findViewById(R.id.id_button_item_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_recycler_view_item, parent, false)
        return DailyViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        val dateStr = dailyDataList[position].removePrefix(DAILY_FOLDER_PREFIX)

        holder.dataValid = true
        holder.dailyDataDate = GlDateTime.stringToDate(dateStr)

        // holder.dataValid = holder.dailyDataStat.loadDailyData(holder.dailyDataDate)

        holder.dateButton.text = dateStr
        holder.dateButton.setOnClickListener {
            val intent = Intent(context, DailyStatisticsActivity::class.java)
            intent.putExtra("dateStr", dateStr)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dailyDataList.size
    }
}


// -------------------------------------------------------------------------------------------------

class DailyBrowseActivity : AppCompatActivity() {
    lateinit var mDailyDataList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_browse)

        title = "GamingLife - ??????"

        mDailyDataList = findViewById(R.id.id_recycler_view_daily_list)
        mDailyDataList.layoutManager =
            LinearLayoutManager(this)
        mDailyDataList.adapter = DailyBrowseAdapter(this)
        mDailyDataList.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL
            )
        )
    }
}

