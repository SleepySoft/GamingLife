package com.sleepysoft.gaminglife.controllers

import android.content.Context
import glcore.GlRoot
import graphengine.GraphView


class GlControllerContext {
    lateinit var context: Context
    lateinit var graphView: GraphView
    lateinit var timeViewController: GlTimeViewController
    lateinit var audioRecordController: GlAudioRecordLayerController
}


class GlControllerBuilder {

    private var mControllerContext = GlControllerContext()

    fun init(context: Context, graphView: GraphView) {
        mControllerContext.context = context
        mControllerContext.graphView = graphView

        createTimeViewController()
        createAudioRecordController()
    }

    private fun createTimeViewController() {
        mControllerContext.timeViewController =
            GlTimeViewController(mControllerContext, GlRoot.glTaskModule).apply {
                this.init()
            }
    }

    private fun createAudioRecordController() {
        mControllerContext.audioRecordController =
            GlAudioRecordLayerController(mControllerContext).apply {
                init()
            }
    }
}