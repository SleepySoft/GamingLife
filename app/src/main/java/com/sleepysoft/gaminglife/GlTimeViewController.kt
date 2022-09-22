package com.sleepysoft.gaminglife

import android.graphics.*
import graphengine.*
import kotlin.math.cos
import kotlin.math.sin


const val DEBUG_TAG = "DefaultDbg"


class GlTimeViewController(graphView: GraphView) : GraphViewObserver {
    
    private val mGraphView = graphView

    private var mCenterItem: GraphCircle = GraphCircle().apply {
        this.radius = 50.0f * unitScale
        this.itemData = "Center"
        this.mainText = "Center"
        this.fontPaint = mGraphView.fontPaint
        this.shapePaint = mGraphView.shapePaint
    }

    private var mCenterRadius = 0.1f
    private var mSurroundRadius = 0.1f
    private var mSurroundItems = mutableListOf< GraphCircle >()

    fun init() {
        for (i in 0 until 1) {
            val item = GraphCircle().apply {
                this.radius = 40.0f * unitScale
                this.itemData = "$i"
                this.mainText = "Item $i"
                this.fontPaint = mGraphView.fontPaint
                this.shapePaint = mGraphView.shapePaint
            }
            mGraphView.addGraphItem(item)
            mSurroundItems.add(item)
        }
        mGraphView.addGraphItem(mCenterItem)
    }

    // -------------------------- Implements GraphViewObserver interface ---------------------------

    override fun onItemPicked(pickedItem: GraphItem) {
        pickedItem.inflatePct = 10.0f
        mGraphView.invalidate()
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
    }

    // ------------------------------------- Private Functions -------------------------------------

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

        val startAngle = 90.0f - mSurroundItems.size * 30.0f / 2
        val endAngle = 90.0f + mSurroundItems.size * 30.0f / 2

        val circumferencePoints = calcCircumferencePoints(
            center, radius - mSurroundRadius,
            // Make the angle 0 since left.
            startAngle - 180.0f, endAngle - 180.0f, mSurroundItems.size)

        for ((index, value) in mSurroundItems.withIndex()) {
            value.origin = circumferencePoints[index]
            value.radius = mSurroundRadius
        }
    }

    private fun layoutLandscape() {

    }

    private fun calcCircumferencePoints(origin: PointF, radius: Float, startAngle: Float,
                                        endAngle: Float, count: Int): List< PointF > {
        val unitAngle = (endAngle - startAngle) / (count + 1)
        val circumferencePoints = mutableListOf< PointF >()
        for (index in 1 .. count) {
            val angle = (startAngle + index * unitAngle)
            val radian = (angle * Math.PI / 180.0f).toFloat()
            circumferencePoints.add(PointF(
                origin.x + radius * cos(radian),
                origin.y + radius * sin(radian),
            ))
        }
        return circumferencePoints
    }
}