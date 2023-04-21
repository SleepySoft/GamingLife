package com.sleepysoft.gaminglife.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import com.sleepysoft.gaminglife.resultCode
import com.sleepysoft.gaminglife.taskGroupIcon
import glcore.GlRoot
import glcore.PeriodicTask
import glenv.GlApp


class AdventureTaskListAdapter(
    private val onEditAction: (action: String, uuid: String) -> Unit) :
    RecyclerView.Adapter< AdventureTaskListAdapter.ViewHolder >() {

    private var mPeriodicTasks: List< PeriodicTask > =
        GlRoot.systemConfig.periodicTaskEditor.getGlDataList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconTask: ImageView = view.findViewById(R.id.icon_task_icon)
        val textTaskName: TextView = view.findViewById(R.id.text_task_name)
        val textTaskPeriod: TextView = view.findViewById(R.id.text_task_period)
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < mPeriodicTasks.size) {
            val ptask = mPeriodicTasks[position]

            holder.iconTask.setImageResource(taskGroupIcon(ptask.classification))
            holder.textTaskName.text = ptask.name
            holder.textTaskPeriod.text = ptask.periodic.toString()

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
        GlRoot.systemConfig.periodicTaskEditor.getGlDataList().let {
            mPeriodicTasks = it
        }
}


// -------------------------------------------------------------------------------------------------

class AdventureTaskListActivity : AppCompatActivity() {
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

        recycleViewAdapter = AdventureTaskListAdapter { action: String, uuid: String ->
            if (action == "delete") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("是否确定删除此项任务？\n\n任务删除后无法恢复")
                    .setCancelable(false)
                    .setPositiveButton(R.string.BUTTON_YES) { _, _ ->
                        GlRoot.systemConfig.periodicTaskEditor.removeGlData(uuid)
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
        GlRoot.systemConfig.saveSystemConfig()
        recycleViewAdapter.refreshPeriodicTaskList()
        recycleViewAdapter.notifyDataSetChanged()
    }
}