package com.sleepysoft.gaminglife

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.sleepysoft.gaminglife.controllers.GlControllerContext


/*
* Input intent:
*       { "text": "Preset Text" }
* Output intent:
*       { "text": "Result Text" }
* */


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
            val resultIntent = Intent().apply {
                putExtra("text", mTextMain.text.toString())
            }
            setResult(GlControllerContext.RESULT_COMMON_INPUT_TEXT_COMPLETE, resultIntent)
            finish()
        }

        mButtonCancel.setOnClickListener {
            setResult(GlControllerContext.RESULT_COMMON_INPUT_CANCELLED)
            finish()
        }

        val intent = getIntent()
        mTextMain.text = intent.getStringExtra("text") ?: ""
    }
}