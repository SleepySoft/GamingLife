package com.sleepysoft.gaminglife.controllers

import glcore.GlRoot
import graphengine.GraphView


class GlControllerMainBuilder() {
    var built = false
    lateinit var graphView: GraphView
    lateinit var timeViewController: GlTimeViewController
    lateinit var timeViewControllerEx: GlTimeViewControllerEx
    lateinit var  audioRecordController: GlAudioRecordLayerController

/*    private var mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable { polling() }*/

    fun checkBuildController() {
        // Do not access other controller in controller's init() function
        if (!built) {
            built = true
            createTimeViewController()
            createAudioRecordController()
        }
        // mHandler.postDelayed(mRunnable, 100)
    }

    private fun createTimeViewController() {
        timeViewController = GlTimeViewController(GlRoot.glTaskModule).apply { init() }
        // timeViewControllerEx = GlTimeViewControllerEx(GlRoot.glTaskModule).apply { init() }
    }

    private fun createAudioRecordController() {
        audioRecordController = GlAudioRecordLayerController().apply { init() }
    }

/*    private fun polling() {
        mHandler.postDelayed(mRunnable, 100)
    }*/
}
