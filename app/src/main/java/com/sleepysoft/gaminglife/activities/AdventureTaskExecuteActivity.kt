package com.sleepysoft.gaminglife.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.UiRes
import glcore.ENUM_TASK_CONCLUSION_ABANDONED
import glcore.ENUM_TASK_CONCLUSION_DOING
import glcore.ENUM_TASK_CONCLUSION_FINISHED
import glcore.ENUM_TASK_CONCLUSION_NONE
import glcore.ENUM_TASK_PERIOD_ARRAY
import glcore.GlService
import glcore.PeriodicTask
import glenv.GlApp


class AdventureTaskExecListAdapter(filterGroup: String)
    : RecyclerView.Adapter< AdventureTaskExecListAdapter.ViewHolder >() {

    private var mPeriodicTasks: List< PeriodicTask > =
        if (filterGroup.isNotEmpty()) {
            GlService.getStartedPeriodicTasksByGroup(filterGroup)
        } else {
            GlService.getPeriodicTasks()
        }.sortedWith(compareBy {
            when (it.conclusion) {
                ENUM_TASK_CONCLUSION_DOING -> 0
                ENUM_TASK_CONCLUSION_NONE -> 1
                ENUM_TASK_CONCLUSION_FINISHED -> 2
                ENUM_TASK_CONCLUSION_ABANDONED -> 3
                else -> 99
            }
        })

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTaskName: TextView = view.findViewById(R.id.text_task_name)
        val textTaskPeriod: TextView = view.findViewById(R.id.text_task_period)

        val buttonPlay: ImageButton = view.findViewById(R.id.button_play)
        val buttonGoal: ImageButton = view.findViewById(R.id.button_goal)
        val buttonPause: ImageButton = view.findViewById(R.id.button_pause)
        val buttonAbandon: ImageButton = view.findViewById(R.id.button_abandon)
        val imageFinished: ImageView = view.findViewById(R.id.image_finished)
        val imageAbandoned: ImageView = view.findViewById(R.id.image_abandoned)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_task_exec_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < mPeriodicTasks.size) {
            val ptask = mPeriodicTasks[position]

            holder.textTaskName.text = ptask.name

            when(ptask.conclusion) {
                ENUM_TASK_CONCLUSION_DOING, ENUM_TASK_CONCLUSION_NONE -> {
                    val periodIndex = ENUM_TASK_PERIOD_ARRAY.indexOf(ptask.periodic)
                    holder.textTaskPeriod.text = UiRes.stringArray("TASK_PERIOD_ARRAY")[periodIndex]

                    holder.imageFinished.visibility = View.GONE
                    holder.imageAbandoned.visibility = View.GONE

                    if (ptask.conclusion == ENUM_TASK_CONCLUSION_DOING) {
                        holder.buttonPlay.visibility = View.GONE

                        holder.buttonPause.visibility = View.VISIBLE
                        holder.buttonPause.setOnClickListener {
                            AlertDialog.Builder(it.context)
                                .setMessage("是否挂起这个任务？")
                                .setPositiveButton("是") { _, _ ->
                                    GlService.suspendPeriodicTask(ptask.id)
                                    notifyDataSetChanged()
                                }
                                .setNegativeButton("否", null)
                                .show()
                        }
                    } else {
                        holder.buttonPause.visibility = View.GONE

                        holder.buttonPlay.visibility = View.VISIBLE
                        holder.buttonPlay.setOnClickListener {
                            AlertDialog.Builder(it.context)
                                .setMessage("是否开始这个任务？")
                                .setPositiveButton("是") { _, _ ->
                                    GlService.executePeriodicTask(ptask.id, ENUM_TASK_CONCLUSION_NONE)
                                    notifyDataSetChanged()
                                }
                                .setNegativeButton("否", null)
                                .show()
                        }
                    }

                    holder.buttonGoal.visibility = View.VISIBLE
                    holder.buttonGoal.setOnClickListener {
                        AlertDialog.Builder(it.context)
                            .setMessage("是否完成这个任务？")
                            .setPositiveButton("是") { _, _ ->
                                GlService.finishPeriodicTask(ptask.id)
                                notifyDataSetChanged()
                            }
                            .setNegativeButton("否", null)
                            .show()
                    }

                    holder.buttonAbandon.visibility = View.VISIBLE
                    holder.buttonAbandon.setOnClickListener {
                        AlertDialog.Builder(it.context)
                            .setMessage("是否取消这个任务？")
                            .setPositiveButton("是") { _, _ ->
                                GlService.abandonPeriodicTask(ptask.id)
                                notifyDataSetChanged()
                            }
                            .setNegativeButton("否", null)
                            .show()
                    }
                }
                ENUM_TASK_CONCLUSION_FINISHED, ENUM_TASK_CONCLUSION_ABANDONED -> {
                    holder.buttonPlay.visibility = View.GONE
                    holder.buttonGoal.visibility = View.GONE
                    holder.buttonPause.visibility = View.GONE
                    holder.imageFinished.visibility = View.GONE
                    holder.buttonAbandon.visibility = View.GONE

                    if (ptask.conclusion == ENUM_TASK_CONCLUSION_FINISHED) {
                        holder.imageAbandoned.visibility = View.GONE
                        holder.imageFinished.visibility = View.VISIBLE
                    }
                    if (ptask.conclusion == ENUM_TASK_CONCLUSION_ABANDONED) {
                        holder.imageFinished.visibility = View.GONE
                        holder.imageAbandoned.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun getItemCount() = mPeriodicTasks.size
}


class AdventureTaskExecuteActivity : AppCompatActivity() {
    lateinit var mTaskRecycleVew: RecyclerView
    lateinit var recycleViewAdapter: AdventureTaskExecListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure_task_execute)

        val filerGroup = intent.getStringExtra("group") ?: ""

        recycleViewAdapter = AdventureTaskExecListAdapter(filerGroup)

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