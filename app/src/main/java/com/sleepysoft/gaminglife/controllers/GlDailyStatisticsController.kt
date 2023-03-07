package com.sleepysoft.gaminglife.controllers

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import glcore.*
import graphengine.*
import kotlin.math.min

class GlDailyStatisticsController(
    private val mCtrlContext: GlControllerContext,
    private var mDailyRecord: GlDailyRecord) : GraphViewObserver {

    private lateinit var statisticsBar: GraphRectangle
    private lateinit var progressDecorator: MultipleSectionProgressDecorator

    private lateinit var multipleStatisticsBar: GraphRectangle
    private lateinit var multipleStatisticsDecorator: MultipleHorizonStatisticsBarDecorator

    fun init() {
        buildStatisticsLayer()
        mCtrlContext.graphView?.mGraphViewObserver?.add(this)
        updateProgress()
        updateStatistics()
    }

    fun release() {
        mCtrlContext.graphView?.mGraphViewObserver?.remove(this)
    }

    fun updateDailyRecord(dailyRecord: GlDailyRecord) {
        mDailyRecord = dailyRecord
        updateProgress()
        updateStatistics()
        mCtrlContext.refresh()
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

            progressDecorator = MultipleSectionProgressDecorator(mCtrlContext, statisticsBar).apply {
                progressScale.add(MultipleSectionProgressDecorator.ProgressScale(0.25f, "06:00"))
                progressScale.add(MultipleSectionProgressDecorator.ProgressScale(0.50f, "12:00"))
                progressScale.add(MultipleSectionProgressDecorator.ProgressScale(0.75f, "18:00"))
            }
            statisticsBar.graphItemDecorator.add(progressDecorator)

            layer.addGraphItem(statisticsBar)

            // -------------------------------------------------------------------------------------

            multipleStatisticsBar = GraphRectangle().apply {
                this.id = "Statistics.StatisticsBar"
                this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(COLOR_DAILY_BAR_BASE)
                    this.style = Paint.Style.FILL
                }
            }

            multipleStatisticsDecorator = MultipleHorizonStatisticsBarDecorator(mCtrlContext, multipleStatisticsBar).apply {
                emptyAreaPaint = Paint().apply {
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = 2.0f
                }
                defaultMaxValue = (8 * TIME_ONE_HOUR).toFloat()
                barTextFormatter = { barData -> "%s [%02d:%02d:%02d]".format(
                    barData.text,
                    barData.value.toInt() / 1000 / 3600,
                    barData.value.toInt() / 1000 % 3600 / 60,
                    barData.value.toInt() / 1000 % 3600 % 60) }

/*                val taskDatas = GlRoot.systemConfig.getTopTasks()
                val groupStatistics = mDailyRecord.groupStatistics()

                for (taskData in taskDatas) {
                    barDatas.add(MultipleHorizonStatisticsBarDecorator.BarData(
                        text=taskData.name,
                        value=(groupStatistics[taskData.id] ?: 0).toFloat(),
                        barPaint=Paint().apply {
                            this.color = Color.parseColor(taskData.color)
                            this.style = Paint.Style.FILL
                        },
                        textPaint=Paint().apply {
                            this.color = Color.parseColor("#000000")
                            this.textSize = 30.0f
                            this.textAlign = Paint.Align.CENTER
                        }
                    ))
                }*/
            }
            multipleStatisticsBar.graphItemDecorator.add(multipleStatisticsDecorator)

            layer.addGraphItem(multipleStatisticsBar)
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
        statisticsBar.rect = RectF(graphView.paintArea).apply {
            top = graphView.paintArea.top + graphView.unitScale * 10
            bottom = graphView.paintArea.top + graphView.unitScale * 20
        }

        multipleStatisticsBar.rect = RectF(graphView.paintArea).apply {
            top = graphView.paintArea.top + graphView.unitScale * 25
            bottom = graphView.paintArea.top + graphView.unitScale * 60
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

        progressDecorator.progressData.clear()
        for (task in mDailyRecord.taskRecords) {
            progressDecorator.progressData.add(
                MultipleSectionProgressDecorator.ProgressData(
                    (task.startTime - dayTsBase).toFloat() / TIMESTAMP_COUNT_IN_DAY,
                    buildPaintForTask(task),
                    task))
        }
        progressDecorator.progressEnd = (dayTsLimit - dayTsBase).toFloat() / TIMESTAMP_COUNT_IN_DAY
    }

    private fun updateStatistics() {
        val taskDatas = GlRoot.systemConfig.getTopTasks()
        val groupStatistics = mDailyRecord.groupStatistics()

        multipleStatisticsDecorator.barDatas.clear()
        for (taskData in taskDatas) {
            multipleStatisticsDecorator.barDatas.add(
                MultipleHorizonStatisticsBarDecorator.BarData(
                text=taskData.name,
                value=(groupStatistics[taskData.id] ?: 0).toFloat(),
                barPaint=Paint().apply {
                    this.color = Color.parseColor(taskData.color)
                    this.style = Paint.Style.FILL
                },
                textPaint=Paint().apply {
                    this.color = Color.parseColor("#000000")
                    this.textSize = 30.0f
                    this.textAlign = Paint.Align.CENTER
                }
            ))
        }
    }

    private fun buildPaintForTask(task: TaskRecord) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(
                GlRoot.systemConfig.getTaskData(task.groupID)?.color ?: COLOR_DAILY_BAR_BASE)
            this.style = Paint.Style.FILL
        }
}
