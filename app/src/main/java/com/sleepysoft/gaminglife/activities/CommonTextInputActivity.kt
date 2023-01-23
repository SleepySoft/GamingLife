package com.sleepysoft.gaminglife

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
            finishWithResult(mapOf("text" to mTextMain.text.toString()), true)

/*            val resultIntent = Intent().apply {
                putExtra("text", mTextMain.text.toString())
                putExtra(
                    GlControllerContext.KEY_REQUEST_CODE,
                    intent.getIntExtra(
                        GlControllerContext.KEY_REQUEST_CODE,
                        GlControllerContext.REQUEST_INVALID))
                putExtra(
                    GlControllerContext.KEY_RESULT_CODE,
                    GlControllerContext.RESULT_ACCEPTED)
            }
            setResult(GlControllerContext.RESULT_ACCEPTED, resultIntent)
            finish()*/
        }

        mButtonCancel.setOnClickListener {
            finishWithResult(mapOf(), false)

/*            val resultIntent = Intent().apply {
                putExtra(
                    GlControllerContext.KEY_RESULT_CODE,
                    GlControllerContext.RESULT_CANCELED)
            }
            setResult(GlControllerContext.RESULT_CANCELED, resultIntent)
            finish()*/
        }

        mTextMain.text = intent.getStringExtra("text") ?: ""
    }
}