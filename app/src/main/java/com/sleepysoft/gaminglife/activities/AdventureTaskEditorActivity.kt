package com.sleepysoft.gaminglife.activities

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.UiRes
import com.sleepysoft.gaminglife.finishWithResult
import glcore.*


class AdventureTaskEditorActivity : AppCompatActivity() {
    private var editTaskId = ""
    private var editDataUuid = ""

    private lateinit var spinnerTaskGroup: Spinner
    private lateinit var textTaskName: EditText

    private lateinit var spinnerTaskPeriod: Spinner
    private lateinit var checkOptionalTask: CheckBox

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
        checkOptionalTask = findViewById(R.id.check_optional)

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

        buttonOk.setOnClickListener {
            val periodicTaskData = ui_to_data()
            if (periodicTaskData.uuid.isNotEmpty()) {
                if (editDataUuid.isNotEmpty()) {
                    periodicTaskData.uuid = editDataUuid
                }
                GlService.upsertPeriodicTask(periodicTaskData)
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

        // ----------------------------------------------------------

        editDataUuid = intent.getStringExtra("edit") ?: ""
        val data: PeriodicTask? =
            if (editDataUuid.isNotEmpty()) {
                GlService.getPeriodicTask(editDataUuid)
            } else {
                null
            }

        editTaskId = if (data != null) {
            data_to_ui(data)
            data.id
        } else {
            editTimeEstimation.setText("5")
            editBatchSize.setText("1")
            randomUUID()
        }
    }

    // ----------------------- Thanks new bing -----------------------

    private fun ui_to_data(): PeriodicTask {
        val taskGroup = spinnerTaskGroup.selectedItemPosition
        val taskGroupId = UiRes.TASK_GROUP_SELECT_ORDER[taskGroup]

        val taskName = textTaskName.text.toString().trim()
        val taskPeriod = spinnerTaskPeriod.selectedItemPosition

        val taskProperty = if (checkOptionalTask.isChecked) ENUM_TASK_PROPERTY_OPTIONAL else ENUM_TASK_PROPERTY_NORMAL

        val taskTimeQuality = spinnerTaskTimeQuality.selectedItemPosition
        val timeEstimationText = editTimeEstimation.text.toString()
        val timeEstimation = timeEstimationText.toIntOrNull() ?: 5

        val single = radioSingle.isChecked
        val batch = radioBatch.isChecked

        val batchSpinner = spinnerBatch.selectedItemPosition
        val batchSizeText = editBatchSize.text.toString()
        val batchSize = batchSizeText.toIntOrNull() ?: 1

        if (taskName.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("必须填写任务名")
                .setPositiveButton("确定", null)
                .show()
            return PeriodicTask().apply { uuid = "" }
        }

        if (batch && (batchSize <= 0)) {
            // 弹出错误框
            AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("批批次必须为大于0的整数")
                .setPositiveButton("确定", null)
                .show()
            return PeriodicTask().apply { uuid = "" }
        }

        if (editTaskId.isEmpty()) {
            editTaskId = randomUUID()
        }

        return PeriodicTask().apply {
            this.id = editTaskId
            this.name = taskName
            this.group = taskGroupId
            this.periodic = ENUM_TASK_PERIOD_ARRAY[taskPeriod]
            this.property = taskProperty
            this.timeQuality = ENUM_TIME_QUALITY_ARRAY[taskTimeQuality]
            this.timeEstimation = timeEstimation
            this.batch = (if (single) 1 else batchSpinner + 1)
            this.batchSize = batchSize
        }
    }

    private fun data_to_ui(task: PeriodicTask) {
        val taskGroupIndex = UiRes.TASK_GROUP_SELECT_ORDER.indexOf(task.group)
        spinnerTaskGroup.setSelection(taskGroupIndex)

        textTaskName.setText(task.name)

        val taskPeriodIndex = ENUM_TASK_PERIOD_ARRAY.indexOf(task.periodic)
        spinnerTaskPeriod.setSelection(taskPeriodIndex)

        checkOptionalTask.isChecked = task.property == ENUM_TASK_PROPERTY_OPTIONAL

        val timeQualityIndex = ENUM_TIME_QUALITY_ARRAY.indexOf(task.timeQuality)
        spinnerTaskTimeQuality.setSelection(timeQualityIndex)

        editTimeEstimation.setText(task.timeEstimation.toString())

        if (task.batch == 1) {
            radioSingle.isChecked = true
            radioBatch.isChecked = false
        } else {
            radioSingle.isChecked = false
            radioBatch.isChecked = true
            spinnerBatch.setSelection(task.batch - 1)
        }

        editBatchSize.setText(task.batchSize.toString())
    }
}