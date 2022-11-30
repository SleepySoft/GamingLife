package com.sleepysoft.gaminglife.controllers

import glcore.GlRoot
import graphengine.GraphShadowView
import java.lang.ref.WeakReference


object GlControllerBuilder {
    var built = false
    val graphShadowView = GraphShadowView()
    lateinit var timeViewController: GlTimeViewController
    lateinit var  audioRecordController: GlAudioRecordLayerController

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
