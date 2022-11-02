package com.sleepysoft.gaminglife

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import glcore.*
import graphengine.*
import java.io.File
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


const val DEBUG_TAG = "DefaultDbg"


class GlTimeViewController(
    private val mGraphView: GraphView,
    private val mGlTaskModule: GlTaskModule) : GraphViewObserver {

    private lateinit var mVibrator: Vibrator

    private lateinit var mTimeViewBaseLayer: GraphLayer
    private var mCenterRadius = 0.1f
    private var mSurroundRadius = 0.1f
    private lateinit var mCenterItem: GraphCircle
    private lateinit var mLongLongPressProgress: GraphCircleProgress
    private var mSurroundItems = mutableListOf< GraphCircle >()

    private var mRecording: Boolean = false
    private var mPressSince: Long = 0
    private lateinit var mRecordController: GlAudioRecordLayerController

    fun init() {
        mVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                mGraphView.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            mGraphView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        mRecordController = GlAudioRecordLayerController(mGraphView).apply { init() }

        checkBuildTimeViewLayer()
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
            mCenterItem.mainText = "%02d:%02d:%02d".format(hour, minutes, seconds)
        }
        else {
            mCenterItem.mainText = "%02d:%02d".format(minutes, seconds)
        }

        processRecordProgress()

        // mCenterItem.mainText = "%02d:%02d:%02d.%03d".format(hour, minutes, seconds, ms)
        mGraphView.invalidate()
    }

    // -------------------------- Implements GraphViewObserver interface ---------------------------

    override fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val strokeWidth = mGraphView.unitScale * 1.0f

        for (item in mSurroundItems) {
            item.shapePaint.strokeWidth = strokeWidth
        }

        mCenterItem.shapePaint.strokeWidth = strokeWidth
    }

    override fun onItemPicked(pickedItem: GraphItem) {
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

        mGraphView.invalidate()
    }

    override fun onItemDragging(draggingItem: GraphItem, pos: PointF) {
        if (draggingItem == mCenterItem) {
            if ((abs(mCenterItem.offsetPixel.x) > mGraphView.unitScale * 0.1) ||
                (abs(mCenterItem.offsetPixel.y) > mGraphView.unitScale * 0.1)) {
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

        droppedItem.inflatePct = 0.0f
        endLongLongPress()
    }

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

    override fun onItemLayout() {
        if (mGraphView.isPortrait()) {
            layoutPortrait()
        }
        else {
            layoutLandscape()
        }
    }

    // ------------------------------------- Private Functions -------------------------------------

    private fun checkBuildTimeViewLayer() {
        val layers = mGraphView.pickLayer { it.id == "TimeView.BaseLayer" }
        val layer = if (layers.isNotEmpty()) {
            layers[0]
        } else {
            GraphLayer("TimeView.BaseLayer", true).apply {
                mGraphView.addLayer(this)
            }
        }

        layer.removeGraphItem() { true }

        val currentTaskInfo = mGlTaskModule.getCurrentTaskInfo()
        val currentTaskGroupData =
            mGlTaskModule.getTaskData(currentTaskInfo["groupID"].toString() ?: "") ?:
            mGlTaskModule.getTaskData(GROUP_ID_IDLE)

        mCenterItem = GraphCircle().apply {
            this.itemData = currentTaskGroupData
            this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#FFFFFF")
                this.textAlign = Paint.Align.CENTER
            }
            this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor(
                    mGlTaskModule.colorOfTask(currentTaskGroupData?.get("id") ?: GROUP_ID_IDLE))
                this.style = Paint.Style.FILL
            }
        }

        mLongLongPressProgress = GraphCircleProgress(
            mCenterItem, 1.3f).apply {
            this.visible = false
            this.interactive = false
            this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#FFA500")
                this.style = Paint.Style.FILL
            }
        }

        val taskGroupTop = mGlTaskModule.getTaskGroupTop()
        for ((k, v) in taskGroupTop) {
            val item = GraphCircle().apply {
                this.itemData = v
                this.mainText = v["name"] ?: ""
                this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor("#FFFFFF")
                    this.textAlign = Paint.Align.CENTER
                }
                this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(mGlTaskModule.colorOfTask(k))
                    this.style = Paint.Style.FILL
                }
            }

            layer.addGraphItem(item)
            mSurroundItems.add(item)
        }
        layer.addGraphItem(mCenterItem)
        layer.insertGraphItemAfter(mLongLongPressProgress, mCenterItem)

        mTimeViewBaseLayer = layer
    }

    private fun layoutPortrait() {
        val layoutArea = RectF(mGraphView.paintArea)
        layoutArea.top = layoutArea.bottom - layoutArea.height()
        layoutArea.apply {
            this.top += 10.0f * mGraphView.unitScale
            this.bottom += 10.0f * mGraphView.unitScale
        }

        mCenterRadius = 8.0f * mGraphView.unitScale
        mSurroundRadius = 6.5f * mGraphView.unitScale

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
    }

    private fun layoutLandscape() {

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

    private fun processRecordProgress() {
        if (!mRecording && (mPressSince > 0)) {
            val duration: Int = (System.currentTimeMillis() - mPressSince).toInt()
            if (duration >= LONG_LONG_PRESS_TIMEOUT) {
                mLongLongPressProgress.visible = false
                mRecordController.popupInput(mCenterItem.origin) {
                    inputType: String, result: Any? -> handleInputComplete(
                    inputType, if (result is String) result else "")
                }
            }
            else {
                mLongLongPressProgress.progress =
                    duration.toFloat() / LONG_LONG_PRESS_TIMEOUT.toFloat()
                mLongLongPressProgress.visible = true
            }
            mGraphView.invalidate()
        }
    }

    private fun endLongLongPress() {
        if (mPressSince > 0) {
            mPressSince = 0
            mLongLongPressProgress.visible = false
            mLongLongPressProgress.progress = 0.0f
        }
        if (mRecording) {
            GlAudioRecorder.stopRecord()
            mRecording = false
        }
    }

    private fun handleInputComplete(inputType: String, result: String) {
        when (inputType) {
            "Text" -> {
                GlRoot.saveContentToDailyFolder(result.toByteArray(), ".md")
            }
            "Audio" -> {
                GlRoot.archiveTemporaryFileToDailyFolder(GlAudioRecorder.WAVPath)
            }
            else -> {

            }
        }
    }
}