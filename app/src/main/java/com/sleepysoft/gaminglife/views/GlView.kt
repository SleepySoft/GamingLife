package com.sleepysoft.gaminglife.views

import android.content.Context
import android.graphics.Canvas
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import graphengine.GraphView


class GlView(context: Context) : View(context), GestureDetector.OnGestureListener {

    var graphView: GraphView? = null

    private var mIsLongPressed = false
    private val mGestureDetector = GestureDetector(context, this)

    // ------------------------------- Window event handler override -------------------------------

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        graphView?.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        graphView?.onSizeChanged(w, h, oldw, oldh)
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
        graphView?.onUp(e)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return graphView?.onDown(e) ?: true
    }

    override fun performClick(): Boolean {
        super.performClick()
        // TODO: What we do for a blind people?
        // https://stackoverflow.com/a/50343572
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        graphView?.onShowPress(e)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return graphView?.onSingleTapUp(e) ?: true
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent,
                          distanceX: Float, distanceY: Float): Boolean {
        return graphView?.onScroll(e1, e2, distanceX, distanceY) ?: true
    }

    override fun onLongPress(e: MotionEvent) {
        mIsLongPressed = true
        graphView?.onLongPress(e)
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent,
                         velocityX: Float, velocityY: Float): Boolean {
        return graphView?.onFling(e1, e2, velocityX, velocityY) ?: true
    }
}
