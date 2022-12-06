package com.sleepysoft.gaminglife.views

import android.content.Context
import android.graphics.Canvas
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import graphengine.GraphView


class GlView(
    private val mGraphView: GraphView, context: Context)
    : View(context), GestureDetector.OnGestureListener {

    constructor(context: Context) : this(GraphView(GlControllerContext()), context) {
        // Should no use this constructor
        assert(false)
    }

    private var mIsLongPressed = false
    private val mGestureDetector = GestureDetector(context, this)

    // ------------------------------- Window event handler override -------------------------------

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mGraphView.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mGraphView.onSizeChanged(w, h, oldw, oldh)
    }

    // --------------------------------- Action & Gesture Handler ----------------------------------

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            if (this.actionMasked == MotionEvent.ACTION_UP) {
                onUp(event)
            }

            if (mGestureDetector.onTouchEvent(event)) {
                return true
            }

            if (this.actionMasked == MotionEvent.ACTION_MOVE) {
                if (mIsLongPressed) {
                    mIsLongPressed = false
                    val cancel = MotionEvent.obtain(event)
                    cancel.action = MotionEvent.ACTION_CANCEL
                    mGestureDetector.onTouchEvent(cancel)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun onUp(e: MotionEvent) {
        mGraphView.onUp(e)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return mGraphView.onDown(e)
    }

    override fun performClick(): Boolean {
        super.performClick()
        // TODO: What we do for a blind people?
        // https://stackoverflow.com/a/50343572
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        mGraphView.onShowPress(e)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return mGraphView.onSingleTapUp(e)
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent,
                          distanceX: Float, distanceY: Float): Boolean {
        return mGraphView.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onLongPress(e: MotionEvent) {
        mIsLongPressed = true
        mGraphView.onLongPress(e)
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent,
                         velocityX: Float, velocityY: Float): Boolean {
        return mGraphView.onFling(e1, e2, velocityX, velocityY)
    }
}
