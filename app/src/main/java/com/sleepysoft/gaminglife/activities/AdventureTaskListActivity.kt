package com.sleepysoft.gaminglife.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.taskGroupIcon
import glcore.GlRoot
import glcore.PeriodicTask


class AdventureTaskListAdapter(
    private val mPeriodicTasks: List< PeriodicTask >) :
    RecyclerView.Adapter< AdventureTaskListAdapter.ViewHolder >() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconTask: ImageView = view.findViewById(R.id.task_icon)
        val textTaskName: TextView = view.findViewById(R.id.task_name)
        val textTaskPeriod: TextView = view.findViewById(R.id.task_period)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_adventure_task_list, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ptask = mPeriodicTasks[position]
        holder.iconTask.setImageResource(taskGroupIcon(ptask.classification))
        holder.textTaskName.text = ptask.name
        holder.textTaskPeriod.text = ptask.periodic.toString()
    }

    override fun getItemCount() = mPeriodicTasks.size
}


class AdventureTaskListActivity : AppCompatActivity() {
    lateinit var mTaskRecycleVew: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure_task_list)

        val ptasks = GlRoot.systemConfig.getPeriodicTasks()

        mTaskRecycleVew = findViewById(R.id.recycler_view_task)
        mTaskRecycleVew.layoutManager = LinearLayoutManager(this)
        mTaskRecycleVew.adapter = AdventureTaskListAdapter(ptasks)
        mTaskRecycleVew.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL
            )
        )
    }
}