package com.sleepysoft.gaminglife

import android.annotation.SuppressLint
import android.view.View
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.Log
import graphengine.*


const val DEBUG_TAG = "DefaultDbg"


class GlTimeView(context: Context) : View(context) {

    private var mUnitScale: Float = 1.0f
    private var mPaintArea: RectF = RectF()
    private var mGraphItems: MutableList< GraphItem > = mutableListOf()

    private val fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
        this.setARGB(0xFF, 0x00, 0x00, 0x00)
        this.textAlign = Paint.Align.CENTER
    }

    private val shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
        this.setARGB(0xFF, 0x00, 0x00, 0x00)
        this.style = Paint.Style.STROKE
        this.strokeWidth = mUnitScale * 0.5f
    }

    // -------------------------------------------------------------------------

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.run {
            renderItems(this)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mUnitScale = w * 0.01f
        mPaintArea.set(0.0f, 0.0f, w.toFloat(), h.toFloat())

        shapePaint.strokeWidth = mUnitScale * 0.5f

        updateItemProperty()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.actionMasked ?: 0xFF) {

            MotionEvent.ACTION_DOWN -> {
                Log.d(DEBUG_TAG, "Action was DOWN")
                true
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(DEBUG_TAG, "Action was MOVE")
                true
            }
            MotionEvent.ACTION_UP -> {
                Log.d(DEBUG_TAG, "Action was UP")
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.d(DEBUG_TAG, "Action was CANCEL")
                true
            }
            MotionEvent.ACTION_OUTSIDE -> {
                Log.d(DEBUG_TAG, "Movement occurred outside bounds of current screen element")
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    // -------------------------------------------------------------------------

    private fun init() {
        mGraphItems.add(GraphCircle().apply {
            this.itemData = "Center"
            this.mainText = "Center"
            this.fontPaint = this@GlTimeView.fontPaint
            this.shapePaint = this@GlTimeView.shapePaint
        })

        for (i in 0 .. 5) {
            mGraphItems.add(GraphCircle().apply {
                this.itemData = "$i"
                this.mainText = "Item $i"
                this.fontPaint = this@GlTimeView.fontPaint
                this.shapePaint = this@GlTimeView.shapePaint
            })
        }
    }

    private fun renderItems(canvas: Canvas) {
        for (item in mGraphItems) {
            item.render(canvas)
        }
    }

    private fun layoutItems() {

    }

    private fun updateItemProperty() {
        for (item in mGraphItems) {
            item.fontPaint = fontPaint
            item.fontPaint = shapePaint
            item.unitScale = mUnitScale
        }
    }
}