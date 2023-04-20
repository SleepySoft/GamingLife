package com.sleepysoft.gaminglife.activities

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.finishWithResult
import glcore.*


class AdventureTaskEditorActivity : AppCompatActivity() {

    companion object {
        val SPINNER_TASK_GROUP_ORDER = listOf(
            GROUP_ID_CREATE,
            GROUP_ID_PROMOTE,
            GROUP_ID_LIFE,
            GROUP_ID_IDLE,
            GROUP_ID_ENJOY,
            GROUP_ID_WORK
        )

        val SPINNER_TASK_PERIOD_ORDER = listOf(1, 7, 14, 30, 90)
    }

    private lateinit var spinnerTaskGroup: Spinner
    private lateinit var textTaskName: EditText

    private lateinit var spinnerTaskPeriod: Spinner

    private lateinit var spinnerTaskTimeQuality: Spinner

    private lateinit var seekTimeEstimation: SeekBar
    private lateinit var editTimeEstimation: EditText

    private lateinit var radioSingle: RadioButton
    private lateinit var radioBatch: RadioButton

    private lateinit var spinnerBatch: Spinner

    private lateinit var seekBatchSize: SeekBar
    private lateinit var editBatchSize: EditText

    private lateinit var buttonOk: Button
    private lateinit var buttonCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure_task_editor)

        spinnerTaskGroup = findViewById(R.id.spinner_task_group)
        textTaskName = findViewById(R.id.text_task_name)

        spinnerTaskPeriod = findViewById(R.id.spinner_task_period)

        spinnerTaskTimeQuality = findViewById(R.id.spinner_task_time_quality)

        seekTimeEstimation = findViewById(R.id.seek_time_estimation)
        editTimeEstimation = findViewById(R.id.edit_time_estimation)

        radioSingle = findViewById(R.id.radio_single)
        radioBatch = findViewById(R.id.radio_batch)

        spinnerBatch = findViewById(R.id.spinner_batch)

        seekBatchSize = findViewById(R.id.seek_batch_size)
        editBatchSize = findViewById(R.id.edit_batch_size)

        seekBatchSize.progress = 1
        editBatchSize.setText("1")

        buttonOk = findViewById(R.id.button_ok)
        buttonCancel = findViewById(R.id.button_cancel)

        // ----------------------------------------------------------

        val uuid = intent.getStringExtra("edit")
        val data: PeriodicTask? =
            if (uuid?.isNotEmpty() == true) {
                GlRoot.systemConfig.periodicTaskEditor.getGlData(uuid)
            } else {
                null
            }

        if (data != null) {

            this.name = taskName
            this.classification = SPINNER_TASK_GROUP_ORDER[taskGroup]
            this.periodic = SPINNER_TASK_PERIOD_ORDER[taskPeriod].toUInt()
            this.timeQuality = taskTimeQuality.toUInt()
            this.timeEstimation = timeEstimation.toUInt()
            this.batch = (if (single) 1 else batchSpinner + 1).toUInt()
            this.batchSize =  batchSize.toUInt()
        } else {
            seekTimeEstimation.progress = 1
            editTimeEstimation.setText("5")

            seekBatchSize.progress = 1
            editBatchSize.setText("1")
        }

        // ----------------------------------------------------------

        buttonOk.setOnClickListener {
            val taskGroup = spinnerTaskGroup.selectedItemPosition
            val taskName = textTaskName.text.toString()
            val taskPeriod = spinnerTaskPeriod.selectedItemPosition

            val taskTimeQuality = spinnerTaskTimeQuality.selectedItemPosition
            val timeEstimationText = editTimeEstimation.text.toString()
            val timeEstimation = timeEstimationText.toIntOrNull() ?: 0

            val single = radioSingle.isChecked
            val batch = radioBatch.isChecked
            val batchSpinner = spinnerBatch.selectedItemPosition
            val batchSizeText = editBatchSize.text.toString()
            val batchSize = batchSizeText.toIntOrNull() ?: 0

            if (batch && (batchSize <= 0)) {
                // 弹出错误框
                AlertDialog.Builder(this)
                    .setTitle("错误")
                    .setMessage("批批次必须为大于0的整数")
                    .setPositiveButton("确定", null)
                    .show()
            } else {
                val periodicTaskData = PeriodicTask().apply {
                    this.name = taskName
                    this.classification = SPINNER_TASK_GROUP_ORDER[taskGroup]
                    this.periodic = SPINNER_TASK_PERIOD_ORDER[taskPeriod].toUInt()
                    this.timeQuality = taskTimeQuality.toUInt()
                    this.timeEstimation = timeEstimation.toUInt()
                    this.batch = (if (single) 1 else batchSpinner + 1).toUInt()
                    this.batchSize =  batchSize.toUInt()
                }
                GlRoot.systemConfig.periodicTaskEditor.upsertGlData(periodicTaskData)

                finishWithResult(mapOf(), true)
            }
        }

        buttonCancel.setOnClickListener {
            finishWithResult(mapOf(), false)
        }

        seekBatchSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the value of editBatchSize when seekBatchSize changes
                editBatchSize.setText(progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        editBatchSize.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Update the value of seekBatchSize when editBatchSize changes
                val progress = s.toString().toIntOrNull()
                if (progress != null) {
                    seekBatchSize.progress = progress
                }
            }
        })

        seekTimeEstimation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress * 5
                editTimeEstimation.setText(value.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        editTimeEstimation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val value = s.toString().toIntOrNull()
                if (value != null && value >= 0) {
                    seekTimeEstimation.progress = value / 5
                }
            }
        })
    }
}