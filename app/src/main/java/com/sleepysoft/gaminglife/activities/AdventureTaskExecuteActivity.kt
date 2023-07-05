package com.sleepysoft.gaminglife.activities

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.UiRes
import glcore.COLOR_PERIODIC_TASK_OPTIONAL_BK
import glcore.COLOR_PERIODIC_TASK_URGENCY_FINAL
import glcore.COLOR_PERIODIC_TASK_URGENCY_DAILY_START
import glcore.COLOR_PERIODIC_TASK_URGENCY_LONG_START
import glcore.ENUM_TASK_CONCLUDED_ARRAY
import glcore.ENUM_TASK_CONCLUSION_ABANDONED
import glcore.ENUM_TASK_CONCLUSION_DOING
import glcore.ENUM_TASK_CONCLUSION_FINISHED
import glcore.ENUM_TASK_CONCLUSION_NONE
import glcore.ENUM_TASK_PERIOD_ARRAY
import glcore.ENUM_TASK_PERIOD_DAILY
import glcore.ENUM_TASK_PROPERTY_OPTIONAL
import glcore.GlService
import glcore.PeriodicTask


class AdventureTaskExecListAdapter(
    private val context: Context, filterGroup: String)
    : RecyclerView.Adapter< AdventureTaskExecListAdapter.ViewHolder >() {

    private var mTaskUrgency: List< Float > = mutableListOf()
    private var mPeriodicTasks: List< PeriodicTask > = mutableListOf()
    private var mPeriodicTasksSorted: List< Pair<PeriodicTask, Float> > = mutableListOf()

    init {
        mPeriodicTasks = if (filterGroup.isNotEmpty()) {
            GlService.getStartedPeriodicTasksByGroup(filterGroup)
        } else {
            GlService.getPeriodicTasks()
        }
        mTaskUrgency = GlService.calculateTaskUrgency(mPeriodicTasks)

        val sortedPairs = mPeriodicTasks.zip(mTaskUrgency).sortedByDescending { it.second }
        mPeriodicTasksSorted =
            sortedPairs.filter { it.first.conclusion !in ENUM_TASK_CONCLUDED_ARRAY } +
            sortedPairs.filter { it.first.conclusion in ENUM_TASK_CONCLUDED_ARRAY }

/*        mPeriodicTasksSorted = mPeriodicTasks.zip(mTaskUrgency)
            .sortedBy { it.second }
            .filter { it.first.conclusion !in ENUM_TASK_CONCLUDED_ARRAY }
            .map { it.first } + mPeriodicTasks.filter { it.conclusion in ENUM_TASK_CONCLUDED_ARRAY }*/
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTaskName: TextView = view.findViewById(R.id.text_task_name)
        val textTaskPeriod: TextView = view.findViewById(R.id.text_task_period)
        val textTaskBatch: TextView = view.findViewById(R.id.text_task_batch)

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
        if (position < mPeriodicTasksSorted.size) {
            val ptask = mPeriodicTasksSorted[position].first
            val urgency = mPeriodicTasksSorted[position].second

            holder.textTaskName.text = ptask.name
            if (ptask.property == ENUM_TASK_PROPERTY_OPTIONAL) {
                holder.textTaskName.setBackgroundColor(
                    Color.parseColor(COLOR_PERIODIC_TASK_OPTIONAL_BK))
            } else {
/*                val startColor = if (ptask.periodic == ENUM_TASK_PERIOD_DAILY) {
                    COLOR_PERIODIC_TASK_URGENCY_DAILY_START
                } else {
                    COLOR_PERIODIC_TASK_URGENCY_LONG_START
                }
                holder.textTaskName.setBackgroundColor(
                    urgencyToColor(urgency, Color.parseColor(startColor)))*/
                holder.textTaskName.setBackgroundColor(urgencyToColor(
                    urgency, Color.parseColor(COLOR_PERIODIC_TASK_URGENCY_DAILY_START)))
            }

            if (ptask.conclusion !in ENUM_TASK_CONCLUDED_ARRAY) {
                layoutUnProcessTask(holder, ptask)
            } else {
                layoutConcludedTask(holder, ptask)
            }
        }
    }

    override fun getItemCount() = mPeriodicTasksSorted.size

    // ---------------------------------------------------------------------------------------------

    @SuppressLint("NotifyDataSetChanged")
    private fun layoutUnProcessTask(holder: ViewHolder, ptask: PeriodicTask) {
        val periodIndex = ENUM_TASK_PERIOD_ARRAY.indexOf(ptask.periodic)
        holder.textTaskPeriod.text = UiRes.stringArray("TASK_PERIOD_ARRAY")[periodIndex]

        if (ptask.batch > 1) {
            val formatString = context.resources.getString(R.string.FORMAT_BATCH_LEFT)
            holder.textTaskBatch.text = formatString.format(ptask.batchRemaining)
            holder.textTaskBatch.visibility = View.VISIBLE

            val layoutParams = holder.textTaskPeriod.layoutParams as RelativeLayout.LayoutParams
            layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT)
            holder.textTaskPeriod.layoutParams = layoutParams
        } else {
            holder.textTaskBatch.visibility = View.GONE

            val layoutParams = holder.textTaskPeriod.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            holder.textTaskPeriod.layoutParams = layoutParams
        }

        // If remove this line, textTaskPeriod will be invisible after clicking buttonGoal.
        holder.textTaskPeriod.visibility = View.VISIBLE

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

    private fun layoutConcludedTask(holder: ViewHolder, ptask: PeriodicTask) {
        holder.buttonPlay.visibility = View.GONE
        holder.buttonGoal.visibility = View.GONE
        holder.buttonPause.visibility = View.GONE
        holder.imageFinished.visibility = View.GONE
        holder.buttonAbandon.visibility = View.GONE
        holder.textTaskPeriod.visibility = View.GONE

        if (ptask.conclusion == ENUM_TASK_CONCLUSION_FINISHED) {
            holder.imageAbandoned.visibility = View.GONE
            holder.imageFinished.visibility = View.VISIBLE
        }
        if (ptask.conclusion == ENUM_TASK_CONCLUSION_ABANDONED) {
            holder.imageFinished.visibility = View.GONE
            holder.imageAbandoned.visibility = View.VISIBLE
        }
    }

    private fun urgencyToColor(urgency: Float, startColor: Int) : Int {
        val evaluator = ArgbEvaluator()
        val endColor = Color.parseColor(COLOR_PERIODIC_TASK_URGENCY_FINAL)
        return evaluator.evaluate(urgency, startColor, endColor) as Int
    }
}


class AdventureTaskExecuteActivity : InteractiveActivity() {
    lateinit var mTaskRecycleVew: RecyclerView
    lateinit var recycleViewAdapter: AdventureTaskExecListAdapter

    private lateinit var layoutTaskRunning: LinearLayout
    private lateinit var textTaskName: TextView
    private lateinit var textTaskTimer: TextView
    private lateinit var textTaskDetail: TextView
    private lateinit var buttonPause: Button
    private lateinit var buttonFinish: Button
    private lateinit var buttonCancel: Button

    private var mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable { updateTaskLastingTime() }

    private var prevOnGoingTask: PeriodicTask? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure_task_execute)

        val filerGroup = intent.getStringExtra("group") ?: ""

        recycleViewAdapter = AdventureTaskExecListAdapter(this, filerGroup)

        mTaskRecycleVew = findViewById(R.id.recycler_view_exec_task)
        mTaskRecycleVew.adapter = recycleViewAdapter
        mTaskRecycleVew.layoutManager = LinearLayoutManager(this)
        mTaskRecycleVew.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL
            )
        )

        layoutTaskRunning = findViewById(R.id.layout_task_running)
        textTaskName = findViewById(R.id.text_on_going_task)
        textTaskTimer = findViewById(R.id.text_on_going_time)
        textTaskDetail = findViewById(R.id.text_task_detail)
        buttonPause = findViewById(R.id.button_pause)
        buttonFinish = findViewById(R.id.button_goal)
        buttonCancel = findViewById(R.id.button_abandon)

        buttonPause.setOnClickListener {
            val onGoingTask = GlService.getOnGoingPeriodicTask()
            onGoingTask?.run {
                AlertDialog.Builder(it.context)
                    .setMessage("是否挂起这个任务？")
                    .setPositiveButton("是") { _, _ ->
                        GlService.suspendPeriodicTask(id)
                        recycleViewAdapter.notifyDataSetChanged()
                    }
                    .setNegativeButton("否", null)
                    .show()
            }
        }

        buttonFinish.setOnClickListener {
            val onGoingTask = GlService.getOnGoingPeriodicTask()
            onGoingTask?.run {
                AlertDialog.Builder(it.context)
                    .setMessage("是否完成这个任务？")
                    .setPositiveButton("是") { _, _ ->
                        GlService.finishPeriodicTask(id)
                        recycleViewAdapter.notifyDataSetChanged()
                    }
                    .setNegativeButton("否", null)
                    .show()
            }
        }

        buttonCancel.setOnClickListener {
            val onGoingTask = GlService.getOnGoingPeriodicTask()
            onGoingTask?.run {
                AlertDialog.Builder(it.context)
                    .setMessage("是否完成这个任务？")
                    .setPositiveButton("是") { _, _ ->
                        GlService.finishPeriodicTask(id)
                        recycleViewAdapter.notifyDataSetChanged()
                    }
                    .setNegativeButton("否", null)
                    .show()
            }
        }

        mHandler.postDelayed(mRunnable, 500)
    }

    private fun updateTaskLastingTime() {
        val onGoingTask = GlService.getOnGoingPeriodicTask()
        if (onGoingTask != null) {
            layoutTaskRunning.visibility = View.VISIBLE

            if (prevOnGoingTask != onGoingTask) {
                prevOnGoingTask = onGoingTask
                textTaskName.text = onGoingTask.name
                if (onGoingTask.taskDetail.isEmpty()) {
                    textTaskDetail.text = ""
                    textTaskDetail.visibility = View.GONE
                } else {
                    textTaskDetail.text = onGoingTask.taskDetail
                    textTaskDetail.visibility = View.VISIBLE
                }
            }

            val lastingTime = GlService.getTimeStamp() - onGoingTask.conclusionTs
            textTaskTimer.text = GlService.formatTimeStamp(lastingTime)
        } else {
            layoutTaskRunning.visibility = View.GONE
        }
        mHandler.postDelayed(mRunnable, 500)
    }
}