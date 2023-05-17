package com.sleepysoft.gaminglife.controllers

import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.sleepysoft.gaminglife.activities.AdventureTaskExecuteActivity
import glcore.*
import graphengine.*
import kotlin.math.cos
import kotlin.math.sin


class GlTimeViewController(
    private val mCtrlContext: GlControllerContext,
    private val audioRecordController: GlAudioRecordLayerController)
    : GraphInteractiveListener(), GraphViewObserver {

    private lateinit var mTimeViewBaseLayer: GraphLayer
    private var mCenterRadius = 0.1f
    private var mSurroundRadius = 0.1f

    private lateinit var mCenterItem: GraphCircle
    private lateinit var mProgressItem: GraphCircleProgress
    private lateinit var mCenterItemText: AutoFitTextDecorator

    private var mSurroundItems = mutableListOf< GraphCircle >()
    private var mSurroundItemText = mutableListOf< AutoFitTextDecorator >()

/*    private lateinit var mMenuConfigKeyPair: GraphRectangle
    private lateinit var mMenuDailyStatistics: GraphRectangle*/

    private var mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable { polling() }

/*    private var mRecording: Boolean = false
    private lateinit var mRecordController: GlAudioRecordLayerController*/

    fun init() {
        checkBuildTimeViewLayer()
        mCtrlContext.graphView?.mGraphViewObserver?.add(this)

/*        adaptViewArea()
        doLayout()*/

        mHandler.postDelayed(mRunnable, 100)
    }

    fun polling() {
        mCenterItemText.mainText = GlService.getCurrentTaskLastTimeFormatted()
        mCtrlContext.refresh()
        mHandler.postDelayed(mRunnable, 100)
    }

    fun refreshPeriodicTaskData() {
        // Only shows the count that have to be finished in
        for (item in mSurroundItems) {
            val taskData = item.itemData as? TaskData
            taskData?.let {
                var text = ""
                val ptasks = GlService.getStartedPeriodicTasksByGroup(taskData.id)
                val busyCount = GlService.countBusyPeriodicTask(ptasks)

                if (busyCount > 0) {
                    val urgency = GlService.CoreLogic.calculateTaskUrgency(ptasks, GlDateTime.timeStamp())
                    val urgencyCount = urgency.count { it >= 0.5f }
                    text = if (urgencyCount > 0) urgencyCount.toString() else ""
                }

                item.service.serviceCall(
                    "CornerTextDecorator.setText", text)
                item.service.serviceCall(
                    "CornerTextDecorator.setVisible", text.isNotEmpty())
            }
        }
        mCtrlContext.refresh()
    }

    // -------------------------- Implements GraphViewObserver interface ---------------------------

    override fun onItemLayout() {
        doLayout()
    }

    override fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        adaptViewArea()
    }

    // ----------------------- Implements GraphInteractiveListener interface -----------------------

    override fun onItemClicked(item: GraphItem) {
        if (item in mSurroundItems) {
            val taskData = item.itemData as? TaskData
            taskData?.run {
                val ptasks = GlService.getStartedPeriodicTasksByGroup(taskData.id)
                // Optional: Task count or busy task count
                // if (GlService.countBusyPeriodicTask(ptasks) > 0) {
                if (ptasks.isNotEmpty()) {
                    mCtrlContext.launchActivity(AdventureTaskExecuteActivity::class.java, null) {
                        it.putExtra("group", this.id)
                    }
                }
            }
        }
/*        if (item == mMenuDailyStatistics) {
            // mCtrlContext.launchActivity(DailyBrowseActivity::class.java)
            mCtrlContext.launchActivity(DailyCalendarActivity::class.java)
        } else if (item == mMenuConfigKeyPair) {
            mCtrlContext.launchActivity(GLIDManagementActivity::class.java)
        }*/
    }

    override fun onItemDropped(item: GraphItem, intersectItems: List< GraphItem >, pos: PointF) {

        if (item == mCenterItem) {
            // Drag center item to surround, closestItem as surroundItem

            val closestItem : GraphItem? = closestGraphItem(
                item.boundRect().centerPoint(),
                intersectItems.intersect(mSurroundItems.toSet()).toList())

            closestItem?.run {
                mCenterItem.itemData = closestItem.itemData
                mCenterItem.shapePaint.color = closestItem.shapePaint.color

                @Suppress("UNCHECKED_CAST")
                handleTaskSwitching(
                    mCenterItem.itemData as TaskData?, closestItem.itemData as TaskData?)
            }
        }
        else if (intersectItems.contains(mCenterItem)) {
            // Drag surround item to center

            if (item in mSurroundItems) {
                mCenterItem.itemData = item.itemData
                mCenterItem.shapePaint.color = item.shapePaint.color

                @Suppress("UNCHECKED_CAST")
                handleTaskSwitching(
                    mCenterItem.itemData as TaskData?, item.itemData as TaskData?)
            }
        }
        item.offsetPixel.x = 0.0f
        item.offsetPixel.y = 0.0f
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onItemTriggered(item: GraphItem) {
        if (item == mCenterItem) {
            audioRecordController.popupInput(
                mCenterItem.boundRect().centerPoint()) {
                    inputType: String, result: Any? ->
                handleInputComplete(inputType, (result as String?) ?: "")
            }
        }
    }

/*    override fun onItemPicked(pickedItem: GraphItem) {
        pickedItem.inflatePct = 10.0f
        mTimeViewBaseLayer.bringGraphItemToFront(pickedItem)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVibrator.vibrate(VibrationEffect.createOneShot(
                100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            mVibrator.vibrate(100)
        }

        // Process Long Long Press
        if (pickedItem == mCenterItem) {
            mPressSince = System.currentTimeMillis()
        }

        GlControllerBuilder.graphView.invalidate()
    }

    override fun onItemDragging(draggingItem: GraphItem, pos: PointF) {
        if (draggingItem == mCenterItem) {
            if ((abs(mCenterItem.offsetPixel.x) > GlControllerBuilder.graphView.unitScale * 0.3) ||
                (abs(mCenterItem.offsetPixel.y) > GlControllerBuilder.graphView.unitScale * 0.3)) {
                endLongLongPress()
            }
        }
    }

    override fun onItemDropped(droppedItem: GraphItem) {
        val intersectingItems: List< GraphItem > =
            mTimeViewBaseLayer.itemIntersectRect(droppedItem.boundRect()) {
                it.visible && it.interactive
            }

        if (droppedItem == mCenterItem) {
            // Drag center item to surround, closestItem as surroundItem

            val closestItem : GraphItem? = closestGraphItem(
                droppedItem.boundRect().centerPoint(),
                intersectingItems.intersect(mSurroundItems.toSet()).toList())

            closestItem?.run {
                mCenterItem.itemData = closestItem.itemData
                mCenterItem.shapePaint.color = closestItem.shapePaint.color

                @Suppress("UNCHECKED_CAST")
                handleTaskSwitching(mCenterItem.itemData as GlStrStruct?,
                    closestItem.itemData as GlStrStruct?)
            }
        }
        else if (intersectingItems.contains(mCenterItem)) {
            // Drag surround item to center

            if (droppedItem in mSurroundItems) {
                mCenterItem.itemData = droppedItem.itemData
                mCenterItem.shapePaint.color = droppedItem.shapePaint.color

                @Suppress("UNCHECKED_CAST")
                handleTaskSwitching(mCenterItem.itemData as GlStrStruct?,
                    droppedItem.itemData as GlStrStruct?)
            }
        }

        droppedItem.inflatePct = 0.0f
        endLongLongPress()
    }

    override fun onItemClicked(clickedItem: GraphItem) {
        if (clickedItem == mMenuDailyStatistics) {
            val activityIntent = Intent(mContext, DailyBrowseActivity::class.java)
            mContext.startActivity(activityIntent)
        }
    }*/

/*    override fun onItemDropIntersecting(droppedItem: GraphItem, intersectingItems: List< GraphItem >) {
        if (droppedItem == mCenterItem) {
            // Drag center item to surround, closestItem as surroundItem

            val closestItem : GraphItem? = closestGraphItem(
                centerFOfRectF(droppedItem.boundRect()),
                intersectingItems.intersect(mSurroundItems.toSet()).toList())

            closestItem?.run {
                mCenterItem.itemData = closestItem.itemData
                mCenterItem.shapePaint.color = closestItem.shapePaint.color

                @Suppress("UNCHECKED_CAST")
                handleTaskSwitching(mCenterItem.itemData as GlStrStruct?,
                    closestItem.itemData as GlStrStruct?)
            }
        }
        else if (intersectingItems.contains(mCenterItem)) {
            // Drag surround item to center

            if (droppedItem in mSurroundItems) {
                mCenterItem.itemData = droppedItem.itemData
                mCenterItem.shapePaint.color = droppedItem.shapePaint.color

                @Suppress("UNCHECKED_CAST")
                handleTaskSwitching(mCenterItem.itemData as GlStrStruct?,
                    droppedItem.itemData as GlStrStruct?)
            }
        }

        endLongLongPress()
    }*/

    // ------------------------------------- Private Functions -------------------------------------

    private fun checkBuildTimeViewLayer() {
        mCtrlContext.graphView?.let { graphView ->

            val layer = GraphLayer("TimeView.BaseLayer", true, graphView)
            graphView.addLayer(layer)

            val currentTaskInfo = GlService.getCurrentTaskInfo()
            val currentTaskGroupData: TaskData =
                GlService.getTaskGroupData(currentTaskInfo.groupID) ?:
                GlService.getTaskGroupData(GROUP_ID_IDLE) ?: TaskData()

            mCenterItem = GraphCircle().apply {
                this.id = "TimeView.CenterItem"
                this.itemData = currentTaskGroupData
                this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(currentTaskGroupData.color)
                    this.style = Paint.Style.FILL
                }
            }

            mCenterItemText = AutoFitTextDecorator(mCtrlContext, mCenterItem).apply {
                this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor("#FFFFFF")
                    this.textAlign = Paint.Align.CENTER
                }
            }
            mCenterItem.graphItemDecorator.add(mCenterItemText)

            mProgressItem = GraphCircleProgress(
                mCenterItem, 1.3f).apply {
                this.visible = false
                this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor("#FFA500")
                    this.style = Paint.Style.FILL
                }
            }
            mCenterItem.graphActionDecorator.add(InteractiveDecorator(mCtrlContext, mCenterItem, true, this))
            mCenterItem.graphActionDecorator.add(LongPressProgressDecorator(
                mCtrlContext, mCenterItem, mProgressItem, 30.0f, this).apply { init() })

            val taskGroupTop = GlService.getTopTaskGroupsData()
            for (taskData in taskGroupTop) {
                // val ptasks = GlService.getPeriodicTasksByGroup(taskData.id)

                val item = GraphCircle().apply {
                    this.id = "TimeView.${taskData.id}"
                    this.itemData = taskData
                    this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                        this.color = Color.parseColor(taskData.color)
                        this.style = Paint.Style.FILL
                    }
                }

                val text = AutoFitTextDecorator(mCtrlContext, item).apply {
                    this.mainText = taskData.name
                    this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                        this.color = Color.parseColor("#FFFFFF")
                        this.textAlign = Paint.Align.CENTER
                    }
                }

                val cornerText = CornerTextDecorator(mCtrlContext, item,
                    CornerTextDecorator.CORNER_UPPER_RIGHT, 0.4f).apply {
                    // this.mainText = ptasks.size.toString()
                    // Will update in refreshPeriodicTaskData()
                    this.mainText = "0"
                }

                item.graphItemDecorator.add(text)
                item.graphItemDecorator.add(cornerText)
                item.graphActionDecorator.add(InteractiveDecorator(mCtrlContext, item, true, this))
                layer.addGraphItem(item)

                mSurroundItems.add(item)
                mSurroundItemText.add(text)
            }

            // --------------------------------------- Menu ---------------------------------------

/*            mMenuConfigKeyPair = GraphRectangle().apply {
                this.id = "TimeView.MenuConfigKeyPair"
                this.graphItemDecorator.add(
                    AutoFitTextDecorator(mCtrlContext, this).apply {
                        this.mainText = "GLID"
                        this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                            this.color = Color.parseColor("#FFFFFF")
                            this.textAlign = Paint.Align.CENTER
                        }
                    }
                )
            }
            mMenuConfigKeyPair.graphActionDecorator.add(
                ClickDecorator(mCtrlContext, mMenuConfigKeyPair, this))
            layer.addGraphItem(mMenuConfigKeyPair)

            mMenuDailyStatistics = GraphRectangle().apply {
                this.id = "TimeView.MenuDailyStatistics"
                this.graphItemDecorator.add(
                    AutoFitTextDecorator(mCtrlContext, this).apply {
                        this.mainText = "回顾"
                        this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                            this.color = Color.parseColor("#FFFFFF")
                            this.textAlign = Paint.Align.CENTER
                        }
                    }
                )
            }
            mMenuDailyStatistics.graphActionDecorator.add(
                ClickDecorator(mCtrlContext, mMenuDailyStatistics, this))
            layer.addGraphItem(mMenuDailyStatistics)*/

            // ------------------------------------------------------------------------------------

            layer.addGraphItem(mCenterItem)
            layer.insertGraphItemAfter(mProgressItem, mCenterItem)

            mTimeViewBaseLayer = layer
        }
    }

    private fun doLayout() {
        mCtrlContext.graphView?.also { graphView ->
            if (graphView.isPortrait()) {
                layoutPortrait(graphView)
                mTimeViewBaseLayer.visible = true
            }
            else {
                // layoutLandscape(graphView)
                mTimeViewBaseLayer.visible = false
            }
        }
    }

    private fun adaptViewArea() {
        mCtrlContext.graphView?.also { graphView ->
            val strokeWidth = graphView.unitScale * 1.0f
            for (item in mSurroundItems) {
                item.shapePaint.strokeWidth = strokeWidth
            }
            mCenterItem.shapePaint.strokeWidth = strokeWidth
        }
    }

    private fun layoutPortrait(graphView: GraphView) {
        val layoutArea = RectF(graphView.paintArea)
        layoutArea.top = layoutArea.bottom - layoutArea.height()
        layoutArea.apply {
            this.top += 10.0f * graphView.unitScale
            this.bottom += 10.0f * graphView.unitScale
        }

        mCenterRadius = 8.0f * graphView.unitScale
        mSurroundRadius = 6.5f * graphView.unitScale

        val center = PointF(layoutArea.centerX(), layoutArea.centerY())
        val radius = layoutArea.width() / 2

        mCenterItem.origin = center
        mCenterItem.radius = mCenterRadius

        val relaxItemPos = calcPointByAngle(center, (radius) * 4 / 5 - mSurroundRadius, 90.0f)

        val circumferencePoints = calcCircumferencePoints(
            center, radius - mSurroundRadius,
            // Make the angle 0 since left.
            0.0f + 180.0f, 180.0f + 180.0f, mSurroundItems.size - 1)

        var index = 0
        for (item in mSurroundItems) {
            item.itemData?.run {
                @Suppress("UNCHECKED_CAST")
                val groupData = item.itemData as TaskData

                if (groupData.id == GROUP_ID_IDLE) {
                    // The relax item, special process
                    item.origin = relaxItemPos
                }
                else {
                    item.origin = circumferencePoints[index]
                    index++
                }
                item.radius = mSurroundRadius
            }
        }
/*        mMenuConfigKeyPair.rect = RectF(200.0f, 0.0f, 400.0f, 100.0f)
        mMenuDailyStatistics.rect = RectF(0.0f, 0.0f, 200.0f, 100.0f)*/
    }

    private fun layoutLandscape(graphView: GraphView) {

    }

    private fun calcCircumferencePoints(origin: PointF, radius: Float, startAngle: Float,
                                        endAngle: Float, count: Int): List< PointF > {
        val circumferencePoints = mutableListOf< PointF >()
        if (count == 1) {
            circumferencePoints.add(calcPointByAngle(origin, radius, (endAngle - startAngle) / 2))
        }
        else {
            val unitAngle = (endAngle - startAngle) / (count - 1)
            for (index in 0 until count) {
                val angle = (startAngle + index * unitAngle)
                circumferencePoints.add(calcPointByAngle(origin, radius, angle))
            }
        }
        return circumferencePoints
    }

    private fun calcPointByAngle(origin: PointF, radius: Float, angle: Float): PointF {
        val radian = (angle * Math.PI / 180.0f).toFloat()
        return PointF(
            origin.x + radius * cos(radian),
            origin.y + radius * sin(radian),
        )
    }

    private fun handleTaskSwitching(fromTask: TaskData?, toTask: TaskData?) {
        if (toTask == null) {
            println("The toTask should not be null")
        }
        fromTask?.run {

        }
        toTask?.run {
            GlService.switchToTask(toTask)
        }
    }

/*    private fun processRecordProgress() {
        if (!mRecording && (mPressSince > 0)) {
            val duration: Int = (System.currentTimeMillis() - mPressSince).toInt()
            if (duration >= LONG_LONG_PRESS_TIMEOUT) {
                mLongLongPressProgress.visible = false
                mRecordController.popupInput(mCenterItem.origin) {
                    inputType: String, result: Any? -> handleInputComplete(
                    inputType, if (result is String) result else "")
                }
                endLongLongPress()
            }
            else {
                mLongLongPressProgress.progress =
                    duration.toFloat() / LONG_LONG_PRESS_TIMEOUT.toFloat()
                mLongLongPressProgress.visible = true
            }
            GlControllerBuilder.graphView.invalidate()
        }
    }

    private fun endLongLongPress() {
        if (mPressSince > 0) {
            mPressSince = 0
            mLongLongPressProgress.visible = false
            mLongLongPressProgress.progress = 0.0f
        }
        if (mRecording) {
            GlRoot.env.glAudio.stopRecord()
            mRecording = false
        }
    }*/

    private fun handleInputComplete(inputType: String, result: String) {
        when (inputType) {
            "Text" -> {
                GlService.saveContentToDailyFolder(result.toByteArray(), "md")
            }
            "Audio" -> {
                GlService.archiveRootPathFileToDailyFolder(RECORD_FILE_NAME_AUDIO)
            }
            else -> {

            }
        }
    }
}