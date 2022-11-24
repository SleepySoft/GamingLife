package com.sleepysoft.gaminglife

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import glcore.GlRoot
import graphengine.GraphView


class TimeViewActivity : AppCompatActivity() {
    private lateinit var mHandler : Handler
    private lateinit var mRunnable : Runnable

    private lateinit var mView: GraphView
    private lateinit var mController: GlTimeViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)

            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable {
            doPeriod()
        }

        mView = GraphView(this)
        mController = GlTimeViewController(this, mView, GlRoot.glTaskModule).apply {
            this.init()
            mView.pushObserver(this)
        }

        setContentView(mView)
        mHandler.postDelayed(mRunnable, 1000)
    }

    override fun onStart() {
        super.onStart()

        val onLockScreen: Boolean = intent.getBooleanExtra("OnLockedScreen", false)
    }

    private fun doPeriod() {
        mController.polling()
        mHandler.postDelayed(mRunnable, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        
    }
}