package com.sleepysoft.gaminglife.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.taskGroupIcon
import glcore.GlRoot
import glcore.PeriodicTask
import glenv.GlApp


class AdventureTaskListAdapter(
    private val mPeriodicTasks: List< PeriodicTask >) :
    RecyclerView.Adapter< AdventureTaskListAdapter.ViewHolder >() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconTask: ImageView = view.findViewById(R.id.icon_task_icon)
        val textTaskName: TextView = view.findViewById(R.id.text_task_name)
        val textTaskPeriod: TextView = view.findViewById(R.id.text_task_period)
        val buttonEdit: ImageButton = view.findViewById(R.id.button_edit)
        val buttonDelete: ImageButton = view.findViewById(R.id.button_delete)
        val layoutEdit: LinearLayout = view.findViewById(R.id.layout_edit)

/*        val buttonNewCreate: ImageButton = view.findViewById(R.id.button_new_create)
        val buttonNewPromote: ImageButton = view.findViewById(R.id.button_new_promote)
        val buttonNewWork: ImageButton = view.findViewById(R.id.button_new_work)
        val buttonNewEnjoy: ImageButton = view.findViewById(R.id.button_new_enjoy)
        val buttonNewLife: ImageButton = view.findViewById(R.id.button_new_life)
        val buttonNewIdle: ImageButton = view.findViewById(R.id.button_new_idle)*/

        val layoutCreate: LinearLayout = view.findViewById(R.id.layout_create)
        val buttonCreate: ImageButton = view.findViewById(R.id.button_create)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_task_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < mPeriodicTasks.size) {
            val ptask = mPeriodicTasks[position]

            holder.iconTask.setImageResource(taskGroupIcon(ptask.classification))
            holder.textTaskName.text = ptask.name
            holder.textTaskPeriod.text = ptask.periodic.toString()

            holder.buttonEdit.setOnClickListener {
                val intent = Intent(GlApp.applicationContext(), AdventureTaskEditorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("edit", ptask.uuid)
                ActivityCompat.startActivity(GlApp.applicationContext(), intent, null)
            }

            holder.buttonDelete.setOnClickListener {

            }
        } else {
            holder.layoutEdit.visibility = View.GONE
            holder.layoutCreate.visibility = View.VISIBLE

            holder.buttonCreate.setOnClickListener {
                val intent = Intent(GlApp.applicationContext(), AdventureTaskEditorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("edit", "")
                ActivityCompat.startActivity(GlApp.applicationContext(), intent, null)
            }
        }
    }

    override fun getItemCount() = mPeriodicTasks.size + 1
}


// -------------------------------------------------------------------------------------------------

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