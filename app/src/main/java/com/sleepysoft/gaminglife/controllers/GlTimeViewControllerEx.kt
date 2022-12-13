package com.sleepysoft.gaminglife.controllers

import android.graphics.*
import android.os.Handler
import android.os.Looper
import com.sleepysoft.gaminglife.R
import glcore.*
import graphengine.*


class GlTimeViewControllerEx(
    private val mCtrlContext: GlControllerContext,
    private val mGlTask: GlSystemConfig)
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
        mCtrlContext.graphView?.mGraphViewObserver?.add(this)

/*        adaptViewArea()
        doLayout()*/

        mHandler.postDelayed(mRunnable, 1000)
    }

    fun polling() {
        if (dailySubBars.isNotEmpty()) {
            dailySubBars.last().rect.right = timeStampToBarBaseX(GlDateTime.datetime().time)
            mCtrlContext.refresh()
        }
    }

    // -------------------------- Implements GraphViewObserver interface ---------------------------

    override fun onItemLayout() {
        doLayout()
    }

    override fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

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
        mCtrlContext.graphView?.also { graphView ->
            val layer = GraphLayer("TimeView.BaseLayer", true, graphView)
            graphView.addLayer(layer)

            buildMainGraph(layer)
            buildTaskGroupGraph(layer)
            rebuildDailyBar(layer)
        }
    }

    private fun buildMainGraph(layer: GraphLayer) {
        val context = mCtrlContext.context.get()
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
            this.graphItemDecorator.add(AutoFitTextDecorator(mCtrlContext, this).apply {
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

        layer.addGraphItem(recordBubble)
        layer.addGraphItem(suggestionBubble)
        layer.addGraphItem(dailyBarBase)
    }

    private fun buildTaskGroupGraph(layer: GraphLayer) {
        val taskGroupTop = GlRoot.systemConfig.getTopTasks()
        for (taskData in taskGroupTop) {
            val item = GraphCircle().apply {
                this.id = "TimeView.${taskData.id}"
                this.itemData = taskData
                this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(taskData.color)
                    this.style = Paint.Style.FILL
                }
            }

            val text = AutoFitTextDecorator(mCtrlContext, item).apply {
                this.mainText = taskData.name
                this.fontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor("#FFFFFF")
                    this.textAlign = Paint.Align.CENTER
                }
            }
            item.graphItemDecorator.add(text)
            item.graphActionDecorator.add(
                InteractiveDecorator(mCtrlContext, item, this))
            layer.addGraphItem(item)

            mSurroundItems.add(item)
            mSurroundItemText.add(text)
        }
    }

    private fun rebuildDailyBar(layer: GraphLayer) {
/*        dailySubBars.clear()
        layer.removeGraphItem { it.id == "TimeView.SubTaskBar" }

        val dailyStat = GlDaily().apply {
            loadDailyData(0)
        }

        // Append current task to the tail of task record

        val currentTaskData = GlRoot.glDatabase.runtimeData.getDictAny(PATH_RUNTIME_CURRENT_TASK)
        val currentTaskRecord = TaskRecordEx().apply { fromAnyStruct(currentTaskData) }

        if (currentTaskRecord.dataValid) {
            dailyStat.taskRecords.add(currentTaskRecord)
        }

        // If the first task does not start from beginning
        // Insert an idle task at the head of task record

        val dayStartTs = GlDateTime.dayStartTimeStamp()
        if (dailyStat.taskRecords.isEmpty() ||
            dailyStat.taskRecords[0].startTime > dayStartTs) {
            dailyStat.taskRecords.add(0, TaskRecordEx().apply {
                this.groupID = GROUP_ID_IDLE
                this.startTime = dayStartTs
            })
        }

        for (taskRecord in dailyStat.taskRecords) {
            val subBar = GraphRectangle().apply {
                this.id = "TimeView.SubTaskBar"
                this.itemData = taskRecord
                this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(mGlTask.colorOfTask(taskRecord.groupID))
                    this.style = Paint.Style.FILL
                }
            }
            dailySubBars.add(subBar)
            layer.addGraphItem(subBar)
        }*/
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

    private fun layoutPortrait(graphView: GraphView) {
        val layoutArea = RectF(graphView.paintArea)

        // Layout daily bar

        dailyBarBase.rect.set(
            layoutArea.left,
            layoutArea.centerY(),
            layoutArea.right,
            layoutArea.centerY() + 90.0f
        )

/*        val barBaseRect = dailyBarBase.boundRect()
        for (i in 0 until dailySubBars.size) {
            val subBar = dailySubBars[i]
            val taskRec = subBar.itemData as TaskRecordEx
            taskRec.endTime = if (i == dailySubBars.size - 1) {
                GlDateTime.datetime().time
            } else {
                (dailySubBars[i + 1].itemData as TaskRecordEx).startTime
            }

            subBar.rect.set(
                timeStampToBarBaseX(taskRec.startTime),
                barBaseRect.top,
                timeStampToBarBaseX(taskRec.endTime),
                barBaseRect.bottom
            )
        }*/

        // Layout task bubble

        for (i in 0 until mSurroundItems.size) {
            val item = mSurroundItems[i]
            item.origin.x = layoutArea.left + layoutArea.width() * (i + 1) / (mSurroundItems.size + 2).toFloat()
            item.origin.y = layoutArea.centerY() + 160.0f
            item.radius = 60.0f
        }

        // Layout 2 bubbles

        suggestionBubble.radius = 30.0f
        suggestionBubble.moveCenter(PointF(
            layoutArea.centerX() - 60.0f,
            layoutArea.centerY() + 220.0f ))

        recordBubble.paintArea = suggestionBubble.boundRect()
        recordBubble.moveCenter(PointF(
            layoutArea.centerX() + 60.0f,
            layoutArea.centerY() + 220.0f ))
    }

    private fun layoutLandscape(graphView: GraphView) {

    }

    private fun timeStampToBarBaseX(ts: Long) : Float {
        val dayStartTs = GlDateTime.dayStartTimeStamp()
        val barBaseRect = dailyBarBase.boundRect()
        if (ts <= dayStartTs) {
            return 0.0f
        } else if (ts >= dayStartTs + TIMESTAMP_COUNT_IN_DAY) {
            return barBaseRect.right
        } else {
            return barBaseRect.left + barBaseRect.width() * (ts - dayStartTs).toFloat() / TIMESTAMP_COUNT_IN_DAY
        }
    }
}