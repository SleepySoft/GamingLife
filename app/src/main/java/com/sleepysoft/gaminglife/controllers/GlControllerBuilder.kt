package com.sleepysoft.gaminglife.controllers

import android.os.Handler
import android.os.Looper
import com.sleepysoft.gaminglife.activities.MainActivity
import glcore.GlRoot
import graphengine.GraphShadowView
import java.lang.ref.WeakReference


object GlControllerBuilder {
    var built = false
    val graphShadowView = GraphShadowView()
    lateinit var timeViewController: GlTimeViewController
    lateinit var  audioRecordController: GlAudioRecordLayerController

    private var mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable { polling() }

    fun checkBuildController() {
        // Do not access other controller in controller's init() function
        if (!built) {
            built = true
            createTimeViewController()
            createAudioRecordController()
        }
        mHandler.postDelayed(mRunnable, 100)
    }

    private fun createTimeViewController() {
        timeViewController = GlTimeViewController(GlRoot.glTaskModule).apply { init() }
    }

    private fun createAudioRecordController() {
        audioRecordController = GlAudioRecordLayerController().apply { init() }
    }

    private fun polling() {
        mHandler.postDelayed(mRunnable, 100)
    }
}
