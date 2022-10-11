package com.sleepysoft.gaminglife

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import glcore.GlData
import glcore.GROUP_ID_RELAX
import glcore.GlStrStruct
import glcore.PATH_TASK_HISTORY
import graphengine.*
import kotlin.math.cos
import kotlin.math.sin


const val DEBUG_TAG = "DefaultDbg"


class GlTimeViewController(
    private val mGraphView: GraphView,
    private val mGlData: GlData) : GraphViewObserver {

    private lateinit var mVibrator: Vibrator
    private lateinit var mCenterItem: GraphCircle

    private var mCenterRadius = 0.1f
    private var mSurroundRadius = 0.1f
    private var mSurroundItems = mutableListOf< GraphCircle >()

    fun init() {
        mVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                mGraphView.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            mGraphView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        buildItems()
    }

    fun polling() {
        val currentTaskData = mGlData.getCurrentTaskInfo()
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

        // mCenterItem.mainText = "%02d:%02d:%02d.%03d".format(hour, minutes, seconds, ms)
        mGraphView.invalidate()
    }

    // -------------------------- Implements GraphViewObserver interface ---------------------------

    override fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mCenterItem.radius = 8.0f * mGraphView.unitScale
        mCenterItem.shapePaint.strokeWidth = mGraphView.unitScale * 1.0f

        for (item in mSurroundItems) {
            item.radius = 6.5f * mGraphView.unitScale
            item.shapePaint.strokeWidth = mGraphView.unitScale * 1.0f
        }

        mGraphView.invalidate()
    }

    override fun onItemPicked(pickedItem: GraphItem) {
        pickedItem.inflatePct = 10.0f
        mGraphView.invalidate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVibrator.vibrate(VibrationEffect.createOneShot(
                100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            mVibrator.vibrate(100)
        }
    }

    override fun onItemDropped(droppedItem: GraphItem) {
        droppedItem.inflatePct = 0.0f
        mGraphView.invalidate()
    }

    override fun onItemDropIntersecting(droppedItem: GraphItem, intersectingItems: List< GraphItem >) {
        if (droppedItem == mCenterItem) {
            // Drag center item to surround, closestItem as surroundItem

            val closestItem : GraphItem? = closestGraphItem(
                centerFOfRectF(droppedItem.getBoundRect()), intersectingItems)

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

            mCenterItem.itemData = droppedItem.itemData
            mCenterItem.shapePaint.color = droppedItem.shapePaint.color

            @Suppress("UNCHECKED_CAST")
            handleTaskSwitching(mCenterItem.itemData as GlStrStruct?,
                                droppedItem.itemData as GlStrStruct?)
        }
    }

    override fun onItemLayout() {
        if (mGraphView.isPortrait()) {
            layoutPortrait()
        }
        else {
            layoutLandscape()
        }
    }

    // ------------------------------------- Private Functions -------------------------------------

    private fun buildItems() {
        val currentTaskInfo = mGlData.getCurrentTaskInfo()
        val currentTaskGroupData =
            mGlData.getTaskData(currentTaskInfo["groupID"].toString() ?: "") ?:
            mGlData.getTaskData(GROUP_ID_RELAX)

        mCenterItem = GraphCircle().apply {
            this.itemData = currentTaskGroupData
            this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#FFFFFF")
                this.textAlign = Paint.Align.CENTER
            }
            this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor(mGlData.colorOfTask(GROUP_ID_RELAX))
                this.style = Paint.Style.FILL
            }
        }

        val taskGroupTop = mGlData.getTaskGroupTop()
        for ((k, v) in taskGroupTop) {
            val item = GraphCircle().apply {
                this.itemData = v
                this.mainText = v["name"] ?: ""
                this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor("#FFFFFF")
                    this.textAlign = Paint.Align.CENTER
                }
                this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.color = Color.parseColor(mGlData.colorOfTask(k))
                    this.style = Paint.Style.FILL
                }
            }
            mGraphView.addGraphItem(item)
            mSurroundItems.add(item)
        }
        mGraphView.addGraphItem(mCenterItem)
    }

    private fun layoutPortrait() {
        val layoutArea = RectF(mGraphView.paintArea)
        layoutArea.top = layoutArea.bottom - layoutArea.height()
        layoutArea.apply {
            this.top += 10.0f * mGraphView.unitScale
            this.bottom += 10.0f * mGraphView.unitScale
        }

        mCenterRadius = 20 * mGraphView.unitScale
        mSurroundRadius = 15 * mGraphView.unitScale

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

                if (groupData["id"] == GROUP_ID_RELAX) {
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
            System.out.println("The toTask should not be null")
        }
        fromTask?.run {

        }
        toTask?.run {
            mGlData.switchToTask(toTask)
        }
    }
}