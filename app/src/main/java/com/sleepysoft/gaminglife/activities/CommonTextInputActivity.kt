package com.sleepysoft.gaminglife

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class CommonTextInputActivity : AppCompatActivity() {

    lateinit var mTextMain: TextView
    lateinit var mButtonOk: Button
    lateinit var mButtonCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_text_input)

        mTextMain = findViewById(R.id.id_text_editor_md)
        mButtonOk = findViewById(R.id.id_button_ok)
        mButtonCancel = findViewById(R.id.id_button_cancel)

        mButtonOk.setOnClickListener {
            finish()
        }

        mButtonCancel.setOnClickListener {
            finish()
        }
    }
}