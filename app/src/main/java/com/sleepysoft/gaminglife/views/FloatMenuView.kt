package com.sleepysoft.gaminglife.views

import android.content.Context
import android.content.Intent
import android.text.Layout
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.activities.AdventureTaskListActivity
import com.sleepysoft.gaminglife.activities.DailyCalendarActivity
import com.sleepysoft.gaminglife.activities.GLIDManagementActivity
import com.sleepysoft.gaminglife.activities.MainActivity

class FloatMenuView(context: Context) : GlFloatView(context, R.layout.layout_view_top_menu) {

    lateinit var viewSeparator: View

    lateinit var buttonMainDaily: Button
    lateinit var layoutGroupDaily: TableLayout
    lateinit var buttonSubStatistics: Button
    lateinit var buttonSubChallenge: Button
    lateinit var buttonSubPlan: Button

    lateinit var buttonMainConfig: Button
    lateinit var layoutGroupConfig: TableLayout
    lateinit var buttonSubGLID: Button

    lateinit var subMenuLayouts: MutableList< TableLayout >

    override fun initLayout() {
        initMainMenuDaily()
        initMainMenuConfig()

        viewSeparator = findViewById(R.id.view_separator)

        subMenuLayouts = mutableListOf(
            layoutGroupDaily,
            layoutGroupConfig
        )
    }

    private fun initMainMenuDaily() {
        buttonMainDaily = findViewById(R.id.button_main_daily)
        layoutGroupDaily = findViewById(R.id.table_sub_menu_daily)
        buttonSubStatistics = findViewById(R.id.button_sub_statistics)
        buttonSubChallenge = findViewById(R.id.button_sub_challenge)
        buttonSubPlan = findViewById(R.id.button_sub_plan)

        buttonMainDaily.setOnClickListener {
            hideAllSubMenuExcept(layoutGroupDaily)
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
        }

        buttonSubStatistics.setOnClickListener {
            val intent = Intent(context, DailyCalendarActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
        }

        buttonSubChallenge.setOnClickListener {
            val intent = Intent(context, AdventureTaskListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
        }
    }

    private fun initMainMenuConfig() {
        buttonMainConfig = findViewById(R.id.button_main_config)
        layoutGroupConfig = findViewById(R.id.table_sub_menu_config)
        buttonSubGLID = findViewById(R.id.table_sub_glid)

        buttonMainConfig.setOnClickListener {
            hideAllSubMenuExcept(layoutGroupConfig)
        }

        buttonSubGLID.setOnClickListener {
            val intent = Intent(context, GLIDManagementActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
        }
    }

    private fun hideAllSubMenuExcept(subMenuLayout: TableLayout?) {
        for (l in subMenuLayouts) {
            l.visibility = if (l == subMenuLayout) VISIBLE else GONE
        }
        // viewSeparator.visibility = if (subMenuLayout == null) GONE else VISIBLE
    }
}