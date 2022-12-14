package com.sleepysoft.gaminglife.controllers

import android.graphics.RectF
import graphengine.GraphLayer
import graphengine.GraphRectangle
import graphengine.MultipleProgressDecorator

class GlDailyStatisticsController(
    private val mCtrlContext: GlControllerContext) {

    private lateinit var statisticsBar: GraphRectangle
    private lateinit var progressDecorator: MultipleProgressDecorator

    fun init() {

    }

    private fun checkBuildStatisticsLayer() {
        mCtrlContext.graphView?.let { graphView ->
            val layer = GraphLayer("Statistics.BaseLayer", true, graphView)
            graphView.addLayer(layer)

            val barArea = RectF(graphView.paintArea).apply {
                val centerY = centerY()
                top = centerY - graphView.unitScale
                bottom = centerY + graphView.unitScale
            }


        }
    }
}
