package com.sleepysoft.gaminglife.controllers

import android.graphics.*
import android.os.Handler
import android.os.Looper
import com.sleepysoft.gaminglife.R
import glcore.*
import graphengine.*


class GlTimeViewControllerEx(
    private val mGlTaskModule: GlTaskModule)
    : GraphInteractiveListener(), GraphViewObserver {

    private var mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable { polling() }

    private val recordBubble = GraphImage()
    private val suggestionBubble = GraphCircle()

    private val dailyBarBase = GraphRectangle()
    private val dailySubBars = mutableListOf< GraphRectangle >()

    private var mSurroundItems = mutableListOf< GraphCircle >()
    private var mSurroundItemText = mutableListOf< AutoFitTextDecorator >()

    fun init() {
        buildTimeViewLayer()
        GlControllerBuilder.graphShadowView.mGraphViewObserver.add(this)

/*        adaptViewArea()
        doLayout()*/

        mHandler.postDelayed(mRunnable, 100)
    }

    fun polling() {

    }

    // -------------------------- Implements GraphViewObserver interface ---------------------------

    override fun onItemLayout() {
        TODO("Not yet implemented")
    }

    override fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        TODO("Not yet implemented")
    }

    // ----------------------- Implements GraphInteractiveListener interface -----------------------

    override fun onItemClicked(item: GraphItem) {

    }

    override fun onItemDropped(item: GraphItem, intersectItems: List<GraphItem>) {

    }

    override fun onItemTriggered(item: GraphItem) {

    }

    // ------------------------------------- Private Functions -------------------------------------

    private fun buildTimeViewLayer() {
        val layer = GraphLayer("TimeView.BaseLayer", true,
            GlControllerBuilder.graphShadowView)
        GlControllerBuilder.graphShadowView.addLayer(layer)

        buildMainGraph(layer)
        buildTaskGroupGraph(layer)
        rebuildDailyBar(layer)
    }

    private fun buildMainGraph(layer: GraphLayer) {
        val context = GlControllerContext.context.get()
        if (context != null) {
            with(recordBubble) {
                this.id = "TimeView.Record"
                this.mBitmapImage = BitmapFactory.decodeResource(
                    context.resources, R.drawable.icon_audio_recording)
            }
        }
        else {
            assert(false)
        }

        with(suggestionBubble) {
            this.id = "TimeView.Record"
            this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor(COLOR_SUGGESTION)
                this.style = Paint.Style.FILL
            }
            this.graphItemDecorator.add(AutoFitTextDecorator(this).apply {
                this.mainText = "?"
                this.fontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor("#FFFFFF")
                    this.textAlign = Paint.Align.CENTER
                }
            })
        }

        with(dailyBarBase) {
            this.id = "TimeView.Record"
            this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor(COLOR_DAILY_BAR_BASE)
                this.style = Paint.Style.FILL
            }
        }
    }

    private fun buildTaskGroupGraph(layer: GraphLayer) {
        val taskGroupTop = mGlTaskModule.getTaskGroupTop()
        for ((k, v) in taskGroupTop) {
            val item = GraphCircle().apply {
                this.id = "TimeView.$k"
                this.itemData = v
                this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(mGlTaskModule.colorOfTask(k))
                    this.style = Paint.Style.FILL
                }
            }

            val text = AutoFitTextDecorator(item).apply {
                this.mainText = v["name"] ?: ""
                this.fontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor("#FFFFFF")
                    this.textAlign = Paint.Align.CENTER
                }
            }
            item.graphItemDecorator.add(text)
            item.graphActionDecorator.add(InteractiveDecorator(item, this))
            layer.addGraphItem(item)

            mSurroundItems.add(item)
            mSurroundItemText.add(text)
        }
    }

    private fun rebuildDailyBar(layer: GraphLayer) {
        val dailyStat = GlDailyStatistics().apply {
            loadDailyData(0)
        }

        // Append current task to the tail of task record

        val currentTaskData = GlRoot.glDatabase.runtimeData.getDictAny(PATH_RUNTIME_CURRENT_TASK)
        val currentTaskRecord = TaskRecordEx().apply { fromAnyStruct(currentTaskData) }

        if (currentTaskRecord.dataValid) {
            dailyStat.taskRecords.add(currentTaskRecord)
        }

        dailySubBars.clear()
        for (taskRecord in dailyStat.taskRecords) {
            dailySubBars.add(GraphRectangle().apply {
                this.id = "TimeView.SubTaskBar"
                this.itemData = taskRecord
                this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(mGlTaskModule.colorOfTask(taskRecord.groupID))
                    this.style = Paint.Style.FILL
                }
            })
        }
    }

    private fun doLayout() {
        if (GlControllerBuilder.graphShadowView.isPortrait()) {
            layoutPortrait()
        }
        else {
            layoutLandscape()
        }
    }

    private fun layoutPortrait() {

    }

    private fun layoutLandscape() {

    }
}