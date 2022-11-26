package com.sleepysoft.gaminglife.controllers

import android.content.Context
import glcore.GlRoot
import graphengine.GraphView


class GlControllerContext {
    var valid: Boolean = false
    lateinit var context: Context
    lateinit var graphView: GraphView
    lateinit var timeViewController: GlTimeViewController
    lateinit var audioRecordController: GlAudioRecordLayerController
}


class GlControllerBuilder {

    var mControllerContext = GlControllerContext()

    fun init(context: Context, graphView: GraphView) {
        mControllerContext.context = context
        mControllerContext.graphView = graphView

        // Do not access other controller in controller's init() function
        createTimeViewController()
        createAudioRecordController()

        mControllerContext.valid = true
    }

    private fun createTimeViewController() {
        mControllerContext.timeViewController =
            GlTimeViewController(mControllerContext, GlRoot.glTaskModule).apply { init() }
    }

    private fun createAudioRecordController() {
        mControllerContext.audioRecordController =
            GlAudioRecordLayerController(mControllerContext).apply { init() }
    }
}