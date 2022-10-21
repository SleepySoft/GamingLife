package com.sleepysoft.gaminglife

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import glcore.GlRoot
import graphengine.GraphView

class MainActivity : AppCompatActivity() {
    private lateinit var mHandler : Handler
    private lateinit var mRunnable : Runnable
    private lateinit var mView: GraphView
    private lateinit var mController: GlTimeViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlRoot.init()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
        {
            setShowWhenLocked(true)
        }
        else
        {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable {
            doPeriod()
        }

        mView = GraphView(this)
        mController = GlTimeViewController(mView, GlRoot.glTaskModule).apply {
            this.init()
            mView.setObserver(this)
        }

        setContentView(mView)
        mHandler.postDelayed(mRunnable, 1000)
    }

    private fun doPeriod() {
        mController.polling()
        mHandler.postDelayed(mRunnable, 1000)
    }
}