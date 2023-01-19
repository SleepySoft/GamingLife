package com.sleepysoft.gaminglife.controllers

import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import com.sleepysoft.gaminglife.R
import glcore.*
import graphengine.*
import kotlin.math.min


class GlTimeViewEditorController(
    private val mCtrlContext: GlControllerContext,
    private val mDailyRecord: GlDailyRecord,
    private val mGlSystemConfig: GlSystemConfig)
    : GraphInteractiveListener(), GraphViewObserver {

    private var selectedProgressData: MultipleSectionProgressDecorator.ProgressData? = null

    private var thisLayer: GraphLayer? = null
    private lateinit var statisticsBar: GraphRectangle
    private lateinit var progressDecorator: MultipleSectionProgressDecorator
    private lateinit var interactiveDecorator: InteractiveDecorator

    private var mSurroundItems = mutableListOf< GraphCircle >()
    private var mSurroundItemText = mutableListOf< AutoFitTextDecorator >()

    fun init() {
        buildTimeViewEditorLayer()
        mCtrlContext.graphView?.mGraphViewObserver?.add(this)
        updateProgress()
    }

    // -------------------------- Implements GraphViewObserver interface ---------------------------

    override fun onItemLayout() {
        doLayout()
    }

    override fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

    }

    // ----------------------- Implements GraphInteractiveListener interface -----------------------

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onItemDropped(item: GraphItem, intersectItems: List<GraphItem>, pos: PointF) {
        if (statisticsBar == item) {
            if (!item.boundRect().contains(pos.x, pos.y)) {
                // User drag outside the bar
                selectedProgressData?.run {
                    val taskRecord = this.userData as TaskRecord?
                    taskRecord?.run {
                        // Find the task data from progress data and remove from record by its uuid
                        mDailyRecord.removeTask(this.uuid)
                        updateProgress()
                    }
                }
            }
        } else if (mSurroundItems.contains(item)) {
            val taskData = item.itemData as TaskData?
            taskData?.let {
                val pct = progressDecorator.xToPct(pos.x)
                val timeStamp = mDailyRecord.dailyTs + (TIMESTAMP_COUNT_IN_DAY * pct).toLong()
                mDailyRecord.addTask(TaskRecord().apply {
                    taskID = ""
                    groupID = it.id
                    startTime = timeStamp
                })
                updateProgress()
            }
            item.offsetPixel = PointF(0.0f, 0.0f)
        }

        // Drop all select data
        selectedProgressData = null
    }

    override fun onItemDragging(item: GraphItem, intersectItems: List< GraphItem >, pos: PointF) {
        if (mSurroundItems.contains(item)) {
            if (item.boundRect().intersect(statisticsBar.boundRect())) {
                // TODO: Show current scale
            }
        }
    }

    override fun onItemSelected(item: GraphItem, pos: PointF) {
        if (statisticsBar == item) {
            val pct = progressDecorator.xToPct(pos.x)
            selectedProgressData = progressDecorator.pctToProgressData(pct)
        }
    }

    // ------------------------------------- Private Functions -------------------------------------

    private fun buildTimeViewEditorLayer() {
        mCtrlContext.graphView?.also { graphView ->
            val layer = GraphLayer("TimeViewEditor.BaseLayer", false, graphView)
            graphView.addLayer(layer)

            buildMainGraph(layer)
            buildTaskGroupGraph(layer)
            // rebuildDailyBar(layer)

            thisLayer = layer
        }
    }

    private fun buildMainGraph(layer: GraphLayer) {
        statisticsBar = GraphRectangle().apply {
            this.id = "Statistics.BaseBar"
            this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor(COLOR_DAILY_BAR_BASE)
                this.style = Paint.Style.FILL
            }
        }

        progressDecorator = MultipleSectionProgressDecorator(mCtrlContext, statisticsBar).apply {
            progressScale.add(MultipleSectionProgressDecorator.ProgressScale(0.125f, "03:00"))
            progressScale.add(MultipleSectionProgressDecorator.ProgressScale(0.250f, "06:00"))
            progressScale.add(MultipleSectionProgressDecorator.ProgressScale(0.500f, "12:00"))
            progressScale.add(MultipleSectionProgressDecorator.ProgressScale(0.625f, "15:00"))
            progressScale.add(MultipleSectionProgressDecorator.ProgressScale(0.750f, "18:00"))
            progressScale.add(MultipleSectionProgressDecorator.ProgressScale(0.875f, "21:00"))
        }
        statisticsBar.graphItemDecorator.add(progressDecorator)
        statisticsBar.graphActionDecorator.add(
            InteractiveDecorator(mCtrlContext, statisticsBar, false, this))

        layer.addGraphItem(statisticsBar)
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
                InteractiveDecorator(mCtrlContext, item, true, this))
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
                    this.color = Color.parseColor(mGlSystemConfig.colorOfTask(taskRecord.groupID))
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
                // Do nothing
                // This controller should not be layout as portrait
                thisLayer?.run { visible = false }
            }
            else {
                layoutLandscape(graphView)
                thisLayer?.run { visible = true }
            }
        }
    }

/*    private fun layoutPortrait(graphView: GraphView) {
        val layoutArea = RectF(graphView.paintArea)

        // Layout daily bar

        statisticsBar.rect.set(
            layoutArea.left,
            layoutArea.centerY(),
            layoutArea.right,
            layoutArea.centerY() + 90.0f
        )

        val barBaseRect = statisticsBar.boundRect()
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
        }

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
    }*/

    private fun layoutLandscape(graphView: GraphView) {
        val layoutArea = RectF(graphView.paintArea)

        // Layout daily bar

        statisticsBar.rect.run {
            left = layoutArea.left
            right = layoutArea.right
            top = layoutArea.top + layoutArea.height() * 0.3f
            bottom = top + 90.0f     // TODO: Relative
        }

        // Layout task bubble

        for (i in 0 until mSurroundItems.size) {
            val item = mSurroundItems[i]
            item.origin.x = layoutArea.left + layoutArea.width() * (i + 1) / (mSurroundItems.size + 2).toFloat()
            item.origin.y = layoutArea.centerY() + 160.0f     // TODO: Relative
            item.radius = 60.0f
        }
    }

/*    private fun timeStampToBarBaseX(ts: Long) : Float {
        val dayStartTs = GlDateTime.dayStartTimeStamp()
        val barBaseRect = statisticsBar.boundRect()
        if (ts <= dayStartTs) {
            return 0.0f
        } else if (ts >= dayStartTs + TIMESTAMP_COUNT_IN_DAY) {
            return barBaseRect.right
        } else {
            return barBaseRect.left + barBaseRect.width() * (ts - dayStartTs).toFloat() / TIMESTAMP_COUNT_IN_DAY
        }
    }*/

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

    private fun buildPaintForTask(task: TaskRecord) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(
                GlRoot.systemConfig.getTaskData(task.groupID)?.color ?: COLOR_DAILY_BAR_BASE)
            this.style = Paint.Style.FILL
        }
}