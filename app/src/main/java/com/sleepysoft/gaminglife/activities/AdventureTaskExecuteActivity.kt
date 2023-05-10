package com.sleepysoft.gaminglife.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.UiRes
import glcore.ENUM_TASK_PERIOD_ARRAY
import glcore.GlService
import glcore.PeriodicTask
import glenv.GlApp


class AdventureTaskExecListAdapter(filterGroup: String)
    : RecyclerView.Adapter< AdventureTaskExecListAdapter.ViewHolder >() {

    private var mPeriodicTasks: List< PeriodicTask > =
        if (filterGroup.isNotEmpty()) {
            GlService.getPeriodicTasksByGroup(filterGroup)
        } else {
            GlService.getPeriodicTasks()
        }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTaskName: TextView = view.findViewById(R.id.text_task_name)
        val textTaskPeriod: TextView = view.findViewById(R.id.text_task_period)

        val buttonPlay: ImageButton = view.findViewById(R.id.button_play)
        val buttonGoal: ImageButton = view.findViewById(R.id.button_goal)
        val buttonPause: ImageButton = view.findViewById(R.id.button_pause)
        val buttonAbandon: ImageButton = view.findViewById(R.id.button_abandon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_task_exec_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < mPeriodicTasks.size) {
            val ptask = mPeriodicTasks[position]

            holder.textTaskName.text = ptask.name

            val periodIndex = ENUM_TASK_PERIOD_ARRAY.indexOf(ptask.periodic)
            holder.textTaskPeriod.text = UiRes.stringArray("TASK_PERIOD_ARRAY")[periodIndex]

            holder.buttonPlay.setOnClickListener {

            }

            holder.buttonGoal.setOnClickListener {

            }

            holder.buttonPause.setOnClickListener {

            }

            holder.buttonAbandon.setOnClickListener {

            }
        }
    }

    override fun getItemCount() = mPeriodicTasks.size + 1
}


class AdventureTaskExecuteActivity : AppCompatActivity() {
    lateinit var mTaskRecycleVew: RecyclerView
    lateinit var recycleViewAdapter: AdventureTaskExecListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure_task_execute)

        recycleViewAdapter = AdventureTaskExecListAdapter("")

        mTaskRecycleVew = findViewById(R.id.recycler_view_exec_task)
        mTaskRecycleVew.adapter = recycleViewAdapter
        mTaskRecycleVew.layoutManager = LinearLayoutManager(this)
        mTaskRecycleVew.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL
            )
        )
    }
}