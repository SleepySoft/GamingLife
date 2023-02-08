package com.sleepysoft.gaminglife.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.finishWithResult
import glcore.GlEncryption
import glcore.GlRoot


const val THRESHOLD_POW = 8


class GeneratePairActivity : AppCompatActivity() {
    var mPrevPoW = 0
    var mExpectPoW = 0
    val mQuitFlag = mutableListOf(false)
    val mGlEncryption = GlEncryption()
    var mCalculateGlIdThread = CalculateGlIdThread(8, mGlEncryption, mQuitFlag)

    private var mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable { updateKeyGenInfo() }

    lateinit var mSeekBarPoW: SeekBar
    lateinit var mTextOutput: TextView
    lateinit var mTextSeekPow: TextView
    lateinit var mButtonGenerate: Button

    class CalculateGlIdThread(
        var pow: Int,
        val mGlEncryption: GlEncryption,
        val quitFlag: List< Boolean >) : Thread("CalculateGlIdThread") {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() = mGlEncryption.createKeyPair(pow, quitFlag).let {  }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_key_pair)

        mTextSeekPow = findViewById(R.id.id_text_current_pow)

        mSeekBarPoW = findViewById(R.id.id_seek_pow)
        mSeekBarPoW.run {
            min = 8
            max = 32
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (progress < 10) {
                        mTextSeekPow.text = " %d".format(progress)
                    } else {
                        mTextSeekPow.text = "%d".format(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
        }

        mTextOutput = findViewById(R.id.id_text_output)
        // https://www.geeksforgeeks.org/how-to-make-textview-scrollable-in-android/
        mTextOutput.movementMethod = ScrollingMovementMethod()

        mButtonGenerate = findViewById(R.id.id_button_generate_glid)
        mButtonGenerate.setOnClickListener {
            if (!mCalculateGlIdThread.isAlive) {
                mQuitFlag[0] = false
                mPrevPoW = 0
                mExpectPoW = mSeekBarPoW.progress

                mCalculateGlIdThread = CalculateGlIdThread(
                    mExpectPoW, mGlEncryption, mQuitFlag).apply { start() }

                mTextOutput.text = ""
                mButtonGenerate.isEnabled = false

                mHandler.postDelayed(mRunnable, 100)
            }
        }

        val buttonCancelGenerate: Button = findViewById< Button >(R.id.id_button_cancel_generate)
        buttonCancelGenerate.setOnClickListener {
            if (mCalculateGlIdThread.isAlive) {
                mQuitFlag[0] = true
            }
        }

        val buttonAcceptGlId: Button = findViewById< Button >(R.id.id_button_accept)
        buttonAcceptGlId.setOnClickListener {
            GlRoot.systemConfig.GLID = glId()
            GlRoot.systemConfig.publicKey = mGlEncryption.powKeyPair.publicKeyString
            GlRoot.systemConfig.privateKey = mGlEncryption.powKeyPair.privateKeyString
            finishWithResult(mapOf(), true)
        }
    }

    fun updateKeyGenInfo() {
        if (mPrevPoW != mGlEncryption.keyPairPow) {
            mPrevPoW = mGlEncryption.keyPairPow
            val text = resources.getString(R.string.FORMAT_GLID_GEN_LOG)
            mTextOutput.append(text.format(mGlEncryption.powLoop, mGlEncryption.keyPairPow))
        }

        if (mCalculateGlIdThread.isAlive) {
            mHandler.postDelayed(mRunnable, 100)
        } else {
            mButtonGenerate.isEnabled = true

            if (mPrevPoW >= THRESHOLD_POW) {
                // Got the expect PoW
                val glId = glId()
                val text = resources.getString(R.string.FORMAT_GLID_GEN_SUCCESS)

                mTextOutput.append("---------------------------------------------\n")
                mTextOutput.append(text.format(glId, mGlEncryption.keyPairPow))
                mTextOutput.append("---------------------------------------------\n")
            } else {
                val text = resources.getString(R.string.FORMAT_GLID_GEN_FAIL)
                mTextOutput.append(text)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun glId() :  String =
        mGlEncryption.powKeyPair.publicKey?.let {
            val pubKeySha = mGlEncryption.dataSha256(it.encoded)
            val glId = mGlEncryption.glidFromPublicKeyHash(pubKeySha)
            glId
        } ?: ""
}