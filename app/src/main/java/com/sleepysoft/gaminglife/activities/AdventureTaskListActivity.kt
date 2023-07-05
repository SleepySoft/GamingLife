package com.sleepysoft.gaminglife.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.UiRes
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import com.sleepysoft.gaminglife.resultCode
import com.sleepysoft.gaminglife.taskGroupIcon
import glcore.COLOR_PERIODIC_TASK_OPTIONAL_BK
import glcore.COLOR_PERIODIC_TASK_URGENCY_LONG_START
import glcore.ENUM_TASK_PERIOD_ARRAY
import glcore.ENUM_TASK_PERIOD_DAILY
import glcore.ENUM_TASK_PERIOD_ONESHOT
import glcore.ENUM_TASK_PROPERTY_OPTIONAL
import glcore.GlRoot
import glcore.GlService
import glcore.PeriodicTask
import glenv.GlApp


class AdventureTaskListAdapter(
    private val context: Context,
    private val onEditAction: (action: String, uuid: String) -> Unit) :
    RecyclerView.Adapter< AdventureTaskListAdapter.ViewHolder >() {

    private var mPeriodicTasks: List< PeriodicTask > = GlService.getPeriodicTasks()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconTask: ImageView = view.findViewById(R.id.icon_task_icon)
        val textTaskName: TextView = view.findViewById(R.id.text_task_name)
        val textTaskPeriod: TextView = view.findViewById(R.id.text_task_period)
        val textTaskBatch: TextView = view.findViewById(R.id.text_task_batch)
        val buttonEdit: ImageButton = view.findViewById(R.id.button_edit)
        val buttonDelete: ImageButton = view.findViewById(R.id.button_delete)
        val layoutEdit: LinearLayout = view.findViewById(R.id.layout_edit)

        val layoutCreate: LinearLayout = view.findViewById(R.id.layout_create)
        val buttonCreate: ImageButton = view.findViewById(R.id.button_create)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        refreshPeriodicTaskList()
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_task_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < mPeriodicTasks.size) {
            val ptask = mPeriodicTasks[position]

            holder.iconTask.setImageResource(taskGroupIcon(ptask.group))

            holder.textTaskName.text = ptask.name
            holder.textTaskName.setOnClickListener {
                val intent = Intent(context, RecordCalendarActivity::class.java)
                intent.putExtra("task_id", ptask.id)
                context.startActivity(intent)
            }

            if (ptask.property == ENUM_TASK_PROPERTY_OPTIONAL) {
                holder.textTaskName.setBackgroundColor(Color.parseColor(COLOR_PERIODIC_TASK_OPTIONAL_BK))
            }/* else if ((ptask.periodic != ENUM_TASK_PERIOD_ONESHOT) &&
                       (ptask.periodic != ENUM_TASK_PERIOD_DAILY)) {
                holder.textTaskName.setBackgroundColor(Color.parseColor(COLOR_PERIODIC_TASK_URGENCY_LONG_START))
            }*/

            val periodIndex = ENUM_TASK_PERIOD_ARRAY.indexOf(ptask.periodic)
            holder.textTaskPeriod.text = UiRes.stringArray("TASK_PERIOD_ARRAY")[periodIndex]

            if (ptask.batch > 1) {
                holder.textTaskBatch.text = if (ptask.batchSize > 1) {
                    "%dx%d".format(ptask.batch, ptask.batchSize)
                } else {
                    val formatString = context.resources.getString(R.string.FORMAT_BATCH_NUM)
                    formatString.format(ptask.batch)
                }
                holder.textTaskBatch.visibility = View.VISIBLE
                val layoutParams = holder.textTaskPeriod.layoutParams as RelativeLayout.LayoutParams
                layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT)
                holder.textTaskPeriod.setLayoutParams(layoutParams)
            } else {
                holder.textTaskBatch.visibility = View.GONE
                val layoutParams = holder.textTaskPeriod.layoutParams as RelativeLayout.LayoutParams
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
                holder.textTaskPeriod.setLayoutParams(layoutParams)
            }

            holder.buttonEdit.setOnClickListener {
                onEditAction("edit", ptask.uuid)
            }

            holder.buttonDelete.setOnClickListener {
                onEditAction("delete", ptask.uuid)
            }

            holder.layoutEdit.visibility = View.VISIBLE
            holder.layoutCreate.visibility = View.GONE
        } else {
            holder.layoutEdit.visibility = View.GONE
            holder.layoutCreate.visibility = View.VISIBLE

            holder.buttonCreate.setOnClickListener {
                onEditAction("create", "")
            }
        }
    }

    override fun getItemCount() = mPeriodicTasks.size + 1

    fun refreshPeriodicTaskList() =
        GlService.getPeriodicTasks().let {
            mPeriodicTasks = it
        }
}


// -------------------------------------------------------------------------------------------------

class AdventureTaskListActivity : InteractiveActivity() {
    lateinit var mTaskRecycleVew: RecyclerView
    lateinit var recycleViewAdapter: AdventureTaskListAdapter

    @SuppressLint("NotifyDataSetChanged")
    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode() == GlControllerContext.RESULT_ACCEPTED) {
            onDataChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure_task_list)

        recycleViewAdapter = AdventureTaskListAdapter(this) { action: String, uuid: String ->
            if (action == "delete") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("是否确定删除此项任务？\n\n任务删除后无法恢复")
                    .setCancelable(false)
                    .setPositiveButton(R.string.BUTTON_YES) { _, _ ->
                        glcore.GlService.removePeriodicTask(uuid)
                        onDataChanged()
                    }
                    .setNegativeButton(R.string.BUTTON_NO) { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            } else {
                val intent = Intent(GlApp.applicationContext(), AdventureTaskEditorActivity::class.java)
                intent.putExtra("edit", uuid)
                startForResult.launch(intent)
            }
        }

        mTaskRecycleVew = findViewById(R.id.recycler_view_task)
        mTaskRecycleVew.adapter = recycleViewAdapter
        mTaskRecycleVew.layoutManager = LinearLayoutManager(this)
        mTaskRecycleVew.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onDataChanged() {
        GlService.syncConfigPeriodicTaskToStarted()
        GlService.checkRefreshPeriodicTask()
        GlService.saveSystemConfig()
        GlService.saveRuntimeData()
        recycleViewAdapter.refreshPeriodicTaskList()
        recycleViewAdapter.notifyDataSetChanged()
    }
}