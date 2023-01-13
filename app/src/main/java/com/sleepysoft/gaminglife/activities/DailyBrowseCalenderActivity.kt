package com.sleepysoft.gaminglife.activities

import android.os.Bundle
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.R
import java.util.*


class DailyBrowseCalenderActivity : AppCompatActivity() {

    lateinit var browseCalender: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_browse_calender)

        browseCalender = findViewById(R.id.id_browse_calendar)
        browseCalender.firstDayOfWeek = Calendar.MONDAY
        browseCalender.setOnDateChangeListener {
                calenderView, year, month, dayOfMonth ->
            Toast.makeText(
                this@DailyBrowseCalenderActivity,
                year.toString() + "年" + month + "月" + dayOfMonth + "日",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}