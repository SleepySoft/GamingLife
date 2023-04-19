package com.sleepysoft.gaminglife.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.R


class AdventureTaskEditorActivity : AppCompatActivity() {

    private lateinit var spinnerTaskGroup: Spinner
    private lateinit var seekBatchSize: SeekBar
    private lateinit var editBatchSize: EditText
    private lateinit var textTaskName: EditText
    private lateinit var spinnerBatch: Spinner
    private lateinit var spinnerTaskPeriod: Spinner
    private lateinit var radioSingle: RadioButton
    private lateinit var radioBatch: RadioButton
    private lateinit var buttonOk: Button
    private lateinit var buttonCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure_task_editor)

        spinnerTaskGroup = findViewById<View>(R.id.spinner_task_group) as Spinner
        seekBatchSize = findViewById(R.id.seek_batch_size)
        editBatchSize = findViewById(R.id.edit_batch_size)
        textTaskName = findViewById(R.id.text_task_name)
        spinnerBatch = findViewById(R.id.spinner_batch)
        spinnerTaskPeriod = findViewById(R.id.spinner_task_period)
        radioSingle = findViewById(R.id.radio_single)
        radioBatch = findViewById(R.id.radio_batch)

        buttonOk = findViewById(R.id.button_ok)
        buttonCancel = findViewById(R.id.button_cancel)

        buttonOk.setOnClickListener {
            // Handle OK button click here
        }

        buttonCancel.setOnClickListener {
            // Handle CANCEL button click here
        }
    }
}