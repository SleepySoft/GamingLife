package com.sleepysoft.gaminglife.controllers

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import glcore.*
import graphengine.*
import kotlin.math.min

class GlDailyStatisticsController(
    private val mCtrlContext: GlControllerContext,
    private val mDailyRecord: GlDailyRecord) : GraphViewObserver {

    private lateinit var statisticsBar: GraphRectangle
    private lateinit var progressDecorator: MultipleProgressDecorator

    fun init() {
        buildStatisticsLayer()
        mCtrlContext.graphView?.mGraphViewObserver?.add(this)
        updateProgress()
    }

    // -------------------------- Implements GraphViewObserver interface ---------------------------

    override fun onItemLayout() {
        doLayout()
    }

    override fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        adaptViewArea()
    }

    // ------------------------------------- Private Functions -------------------------------------

    private fun buildStatisticsLayer() {
        mCtrlContext.graphView?.let { graphView ->
            val layer = GraphLayer("Statistics.BaseLayer", true, graphView)
            layer.setBackgroundColor(COLOR_INDIGO_100)
            layer.setBackgroundAlpha(0xFF)
            graphView.addLayer(layer)

            statisticsBar = GraphRectangle().apply {
                this.id = "Statistics.BaseBar"
                this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(COLOR_DAILY_BAR_BASE)
                    this.style = Paint.Style.FILL
                }
            }

            progressDecorator = MultipleProgressDecorator(mCtrlContext, statisticsBar).apply {
                progressScale.add(MultipleProgressDecorator.ProgressScale(0.25f, "06:00"))
                progressScale.add(MultipleProgressDecorator.ProgressScale(0.50f, "12:00"))
                progressScale.add(MultipleProgressDecorator.ProgressScale(0.75f, "18:00"))
            }
            statisticsBar.graphItemDecorator.add(progressDecorator)

            layer.addGraphItem(statisticsBar)
        }
    }

    private fun doLayout() {
        mCtrlContext.graphView?.also { graphView ->
            if (graphView.isPortrait()) {
                layoutPortrait(graphView)
            }
            else {
                layoutLandscape(graphView)
            }
        }
    }

    private fun adaptViewArea() {
        mCtrlContext.graphView?.also { graphView ->
            val strokeWidth = graphView.unitScale * 1.0f
            statisticsBar.shapePaint.strokeWidth = strokeWidth
        }
    }

    private fun layoutCommon(graphView: GraphView) {
        statisticsBar.rect  = RectF(graphView.paintArea).apply {
            val centerY = centerY()
            top = centerY - graphView.unitScale * 5
            bottom = centerY + graphView.unitScale * 5
        }
    }

    private fun layoutPortrait(graphView: GraphView) {
        layoutCommon(graphView)
    }

    private fun layoutLandscape(graphView: GraphView) {
        layoutCommon(graphView)
    }

    private fun updateProgress() {
        val dayTsBase = mDailyRecord.dailyTs
        val dayTsLimit = min(dayTsBase + TIMESTAMP_COUNT_IN_DAY - 1, GlDateTime.datetime().time)

        for (task in mDailyRecord.taskRecords) {
            progressDecorator.progressData.add(
                MultipleProgressDecorator.ProgressData(
                    (task.startTime - dayTsBase).toFloat() / TIMESTAMP_COUNT_IN_DAY,
                    buildPaintForTask(task)))
        }
        progressDecorator.progressEnd = (dayTsLimit - dayTsBase).toFloat() / TIMESTAMP_COUNT_IN_DAY
    }

    private fun buildPaintForTask(task: TaskRecord) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(
                GlRoot.systemConfig.getTaskData(task.groupID)?.color ?: COLOR_DAILY_BAR_BASE)
            this.style = Paint.Style.FILL
        }
}
