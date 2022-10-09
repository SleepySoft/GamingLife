package com.sleepysoft.gaminglife

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.support.annotation.RequiresApi
import glcore.GlData
import glcore.GROUP_ID_RELAX
import graphengine.*
import kotlin.math.cos
import kotlin.math.sin


const val DEBUG_TAG = "DefaultDbg"


class GlTimeViewController(
    private val mGraphView: GraphView,
    private val mGlData: GlData) : GraphViewObserver {

    private var mVibrator: Vibrator? = null

    private var mCenterItem: GraphCircle = GraphCircle().apply {
        this.itemData = GROUP_ID_RELAX
        this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
            this.textAlign = Paint.Align.CENTER
        }
        this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }
    }

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
        refreshItems()
    }

    fun polling() {
        val currentTimeMs = System.currentTimeMillis()
        val currentTimeS = currentTimeMs / 1000

        val ms = currentTimeMs % 1000
        val hour = currentTimeS / 3600
        val remainingSec = currentTimeS % 3600
        val minutes = remainingSec / 60
        val seconds = remainingSec % 60

        mCenterItem.mainText = "%02d:%02d:%02d".format(hour, minutes, seconds)
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
            mVibrator?.vibrate(VibrationEffect.createOneShot(
                100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            mVibrator?.vibrate(100)
        }
    }

    override fun onItemDropped(droppedItem: GraphItem) {
        droppedItem.inflatePct = 0.0f
        mGraphView.invalidate()
    }

    override fun onItemDropIntersecting(droppedItem: GraphItem, intersectingItems: List< GraphItem >) {

    }

    override fun onItemLayout() {
        if (mGraphView.isPortrait()) {
            layoutPortrait()
        }
        else {
            layoutLandscape()
        }
        refreshItems()
    }

    // ------------------------------------- Private Functions -------------------------------------

    private fun buildItems() {
        val taskGroupTop = mGlData.getTaskGroupTop()
        for ((k, v) in taskGroupTop) {
            val item = GraphCircle().apply {
                this.itemData = k
                this.fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.textAlign = Paint.Align.CENTER
                }
                this.shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.FILL
                }
            }
            mGraphView.addGraphItem(item)
            mSurroundItems.add(item)
        }
        mGraphView.addGraphItem(mCenterItem)
    }

    private fun refreshItems() {
        for (item in mSurroundItems) {
            item.apply {
                // this.mainText = mGlData.g
                this.fontPaint.color = mGlData.getGroupColor(this.itemData as String).toInt()
            }
        }

        val color = mGlData.getGroupColor(mCenterItem.itemData as String)
        mCenterItem.shapePaint.color = Color.parseColor("#000000")
    }

    private fun layoutPortrait() {
        val layoutArea = RectF(mGraphView.paintArea)
        layoutArea.top = layoutArea.bottom - layoutArea.height()
        layoutArea.apply {
            this.top += 10.0f * mGraphView.unitScale
            this.bottom += 10.0f * mGraphView.unitScale
        }

        mCenterRadius = 12 * mGraphView.unitScale
        mSurroundRadius = 8 * mGraphView.unitScale

        val center = PointF(layoutArea.centerX(), layoutArea.centerY())
        val radius = layoutArea.width() / 2

        mCenterItem.origin = center
        mCenterItem.radius = mCenterRadius

        val relaxItemPos = calcPointByAngle(center, radius - mSurroundRadius, 90.0f)

        val circumferencePoints = calcCircumferencePoints(
            center, radius - mSurroundRadius,
            // Make the angle 0 since left.
            0.0f + 180.0f, 180.0f + 180.0f, mSurroundItems.size - 1)

        var index = 0
        for (item in mSurroundItems) {
            if (item.itemData == GROUP_ID_RELAX) {
                // The relax item, special process
                item.origin = relaxItemPos
                item.radius = mSurroundRadius
            }
            else {
                item.origin = circumferencePoints[index]
                item.radius = mSurroundRadius
                index++
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
}