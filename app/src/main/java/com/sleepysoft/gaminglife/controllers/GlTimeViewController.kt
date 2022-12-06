package com.sleepysoft.gaminglife.controllers

import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Handler
import android.os.Looper
import com.sleepysoft.gaminglife.DailyBrowseActivity
import glcore.*
import graphengine.*
import kotlin.math.cos
import kotlin.math.sin


class GlTimeViewController(
    private val mCtrlContext: GlControllerContext,
    private val mGlTaskModule: GlTaskModule,
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

    private lateinit var mMenuDailyStatistics: GraphRectangle

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
        val currentTaskData = mGlTaskModule.getCurrentTaskInfo()
        val currentTaskStartTimeMs = currentTaskData["startTime"] as Long

        val deltaTimeMs: Long = System.currentTimeMillis() - currentTaskStartTimeMs
        val deltaTimeS: Long = deltaTimeMs / 1000

        val ms: Long = deltaTimeMs % 1000
        val hour: Long = deltaTimeS / 3600
        val remainingSec: Long = deltaTimeS % 3600
        val minutes: Long = remainingSec / 60
        val seconds: Long = remainingSec % 60

        if (hour != 0L) {
            mCenterItemText.mainText = "%02d:%02d:%02d".format(hour, minutes, seconds)
        }
        else {
            mCenterItemText.mainText = "%02d:%02d".format(minutes, seconds)
        }

        // processRecordProgress()

        // mCenterItem.mainText = "%02d:%02d:%02d.%03d".format(hour, minutes, seconds, ms)

        mCtrlContext.refresh()
        mHandler.postDelayed(mRunnable, 100)
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
        if (item == mMenuDailyStatistics) {
            mCtrlContext.launchActivity(DailyBrowseActivity::class.java)
        }
    }

    override fun onItemDropped(item: GraphItem, intersectItems: List< GraphItem >) {

        if (item == mCenterItem) {
            // Drag center item to surround, closestItem as surroundItem

            val closestItem : GraphItem? = closestGraphItem(
                item.boundRect().centerPoint(),
                intersectItems.intersect(mSurroundItems.toSet()).toList())

            closestItem?.run {
                mCenterItem.itemData = closestItem.itemData
                mCenterItem.shapePaint.color = closestItem.shapePaint.color

                @Suppress("UNCHECKED_CAST")
                handleTaskSwitching(mCenterItem.itemData as GlStrStruct?,
                    closestItem.itemData as GlStrStruct?)
            }
        }
        else if (intersectItems.contains(mCenterItem)) {
            // Drag surround item to center

            if (item in mSurroundItems) {
                mCenterItem.itemData = item.itemData
                mCenterItem.shapePaint.color = item.shapePaint.color

                @Suppress("UNCHECKED_CAST")
                handleTaskSwitching(mCenterItem.itemData as GlStrStruct?,
                    item.itemData as GlStrStruct?)
            }
        }
        item.offsetPixel.x = 0.0f
        item.offsetPixel.y = 0.0f
    }

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
        mCtrlContext.graphView?.also { graphView ->

            val layer = GraphLayer("TimeView.BaseLayer", true, graphView)
            graphView.addLayer(layer)

            val currentTaskInfo = mGlTaskModule.getCurrentTaskInfo()
            val currentTaskGroupData =
                mGlTaskModule.getTaskData(currentTaskInfo["groupID"].toString() ?: "") ?:
                mGlTaskModule.getTaskData(GROUP_ID_IDLE)

            mCenterItem = GraphCircle().apply {
                this.id = "TimeView.CenterItem"
                this.itemData = currentTaskGroupData
                this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(
                        mGlTaskModule.colorOfTask(currentTaskGroupData?.get("id") ?: GROUP_ID_IDLE))
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
            mCenterItem.graphActionDecorator.add(InteractiveDecorator(mCtrlContext, mCenterItem, this))
            mCenterItem.graphActionDecorator.add(LongPressProgressDecorator(
                mCtrlContext, mCenterItem, mProgressItem, 30.0f, this).apply { init() })

            val taskGroupTop = mGlTaskModule.getTaskGroupTop()
            for ((k, v) in taskGroupTop) {
                val item = GraphCircle().apply {
                    this.id = "TimeView.$k"
                    this.itemData = v
                    this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                        this.color = Color.parseColor(mGlTaskModule.colorOfTask(k))
                        this.style = Paint.Style.FILL
                    }
                }

                val text = AutoFitTextDecorator(mCtrlContext, item).apply {
                    this.mainText = v["name"] ?: ""
                    this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                        this.color = Color.parseColor("#FFFFFF")
                        this.textAlign = Paint.Align.CENTER
                    }
                }
                item.graphItemDecorator.add(text)
                item.graphActionDecorator.add(InteractiveDecorator(mCtrlContext, item, this))
                layer.addGraphItem(item)

                mSurroundItems.add(item)
                mSurroundItemText.add(text)
            }

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

            layer.addGraphItem(mMenuDailyStatistics)
            layer.addGraphItem(mCenterItem)
            layer.insertGraphItemAfter(mProgressItem, mCenterItem)

            mTimeViewBaseLayer = layer
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
                val groupData = item.itemData as GlStrStruct

                if (groupData["id"] == GROUP_ID_IDLE) {
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
        mMenuDailyStatistics.rect = RectF(0.0f, 0.0f, 200.0f, 100.0f)
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

    private fun handleTaskSwitching(fromTask: GlStrStruct?, toTask: GlStrStruct?) {
        if (toTask == null) {
            println("The toTask should not be null")
        }
        fromTask?.run {

        }
        toTask?.run {
            mGlTaskModule.switchToTask(toTask)
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
                GlRoot.saveContentToDailyFolder(result.toByteArray(), "md")
            }
            "Audio" -> {
                GlRoot.archiveRootPathFileToDailyFolder(FILE_NAME_AUDIO_TEMP_WAV)
            }
            else -> {

            }
        }
    }
}