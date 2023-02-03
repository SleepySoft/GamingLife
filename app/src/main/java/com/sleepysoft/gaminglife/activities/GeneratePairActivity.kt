package com.sleepysoft.gaminglife.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.sleepysoft.gaminglife.R
import glcore.GlEncryption
import glenv.GlKeyPair


class GeneratePairActivity : AppCompatActivity() {
    val mQuitFlag = mutableListOf(false)
    val mGlEncryption = GlEncryption()
    val mCalculateGlIdThread = CalculateGlIdThread(8, mGlEncryption, mQuitFlag)

    private var mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable { polling() }

    lateinit var mSeekBarPoW: SeekBar
    lateinit var mTextOutput: TextView

    class CalculateGlIdThread(
        var pow: Int,
        val mGlEncryption: GlEncryption,
        val quitFlag: List< Boolean >) : Thread("CalculateGlIdThread") {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() = mGlEncryption.createKeyPair(pow, quitFlag).let {  }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_key_pair)

        mSeekBarPoW = findViewById(R.id.id_seek_pow)
        mTextOutput = findViewById(R.id.id_text_output)

        val buttonGenerate: Button = findViewById< Button >(R.id.id_button_generate_glid)
        buttonGenerate.setOnClickListener {
            if (!mCalculateGlIdThread.isAlive) {
                mQuitFlag[0] = false
                mCalculateGlIdThread.start()
                buttonGenerate.isEnabled = false
                mHandler.postDelayed(mRunnable, 100)
            }
        }

        val buttonCancelGenerate: Button = findViewById< Button >(R.id.id_button_cancel_generate)
        buttonCancelGenerate.setOnClickListener {
            if (mCalculateGlIdThread.isAlive) {
                mQuitFlag[0] = true
                buttonGenerate.isEnabled = true
            }
        }
    }

    fun polling() {
        if (mCalculateGlIdThread.isAlive) {
            mHandler.postDelayed(mRunnable, 100)
        }
    }
}