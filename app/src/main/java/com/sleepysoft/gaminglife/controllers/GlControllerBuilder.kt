package com.sleepysoft.gaminglife.controllers

import android.content.Context
import glcore.GlRoot
import graphengine.GraphView
import java.lang.ref.WeakReference


object GlControllerBuilder {
    var built = false
    val timeViewController = WeakReference< GlTimeViewController >(null)
    val audioRecordController = WeakReference< GlAudioRecordLayerController >(null)

    fun checkBuildController() {
        // Do not access other controller in controller's init() function
        if (!built) {
            built = true
            createTimeViewController()
            createAudioRecordController()
        }
    }

    private fun createTimeViewController() {
        timeViewController = GlTimeViewController(GlRoot.glTaskModule).apply { init() }
    }

    private fun createAudioRecordController() {
        audioRecordController = GlAudioRecordLayerController().apply { init() }
    }
}
