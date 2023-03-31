package com.sleepysoft.gaminglife.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.R


class AdventureTaskEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure_task_editor)

/*        val spinner = findViewById<View>(R.id.spinner_task_type) as Spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.string., android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter*/

/*        val spinnerPeriod = findViewById<View>(R.id.spinner_task_period) as Spinner
        with(spinnerPeriod) {
            val adapter = ArrayAdapter.createFromResource(
                this@AdventureTaskEditorActivity,
                R.array.TASK_PERIOD_ARRAY, android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
        }*/
    }
}