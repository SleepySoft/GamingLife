package graphengine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import glcore.GlLog
import glcore.LONG_LONG_PRESS_TIMEOUT
import kotlin.math.abs


open class GraphItemDecorator(
    val decoratedItem: GraphItem) {

    open fun paintBeforeGraph(canvas: Canvas) {

    }
    open fun paintAfterGraph(canvas: Canvas) {

    }
}


open class GraphActionDecorator(decoratedItem: GraphItem) :
    GraphItemDecorator(decoratedItem), ActionHandler {

    override fun onActionUp(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionDown(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionMove(
        posBefore: PointF,
        posNow: PointF,
        distanceX: Float,
        distanceY: Float
    ): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionFling(
        posBefore: PointF,
        posNow: PointF,
        velocityX: Float,
        velocityY: Float
    ): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionClick(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionLongPress(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

}


// ---------------------------------------------------------------------------------------------

class AutoFitTextDecorator(decoratedItem: GraphItem) : GraphItemDecorator(decoratedItem) {

    var mainText: String = ""
        set(value) {
            field = value
        }
    var fontPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var textArea: Rect = Rect()
    var textBound: Rect = Rect()

    override fun paintAfterGraph(canvas: Canvas) {
        val newTextArea = decoratedItem.boundRect().apply { inflate(0.7f) }.toRect()
        if (textArea != newTextArea) {
            textArea = newTextArea
            textBound = decoratedItem.boundRect().apply { inflate(0.7f) }.toRect()
            val fontSize = calculateFontSize(textBound, textArea, mainText)
            fontPaint.textSize = fontSize
        }
        val halfTextHeight: Float = textBound.height() / 2.0f
        canvas.drawText(
            mainText,
            textArea.centerX().toFloat(),
            (textArea.centerY().toFloat() + halfTextHeight),
            fontPaint
        )
    }
}


// ---------------------------------------------------------------------------------------------

class InteractiveDecorator(
    decoratedItem: GraphItem,
    var interactiveListener: GraphInteractiveListener? = null)
    : GraphActionDecorator(decoratedItem) {

    companion object {
        var trackingItem: GraphItem? = null
            private set

        fun changeTrackingItem(item: GraphItem) {
            trackingItem = item
        }
    }


    override fun onActionUp(pos: PointF): ActionHandler.ACT {
        if (trackingItem == decoratedItem) {
            GlLog.i("InteractiveDecorator.onActionUp [$decoratedItem]")

            decoratedItem.inflatePct = 0.0f
            interactiveListener?.onItemDropped(decoratedItem, intersectItems())
            trackingItem = null
        }
        // Leak this action to other handler avoiding issues
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionMove(posBefore: PointF, posNow: PointF,
                              distanceX: Float,distanceY: Float): ActionHandler.ACT {
        return if (decoratedItem == trackingItem) {
            GlLog.i("InteractiveDecorator.onActionMove [$decoratedItem]")

            decoratedItem.shiftItem(-distanceX, -distanceY)
            interactiveListener?.onItemDropped(decoratedItem, intersectItems())
            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }

    override fun onActionClick(pos: PointF) : ActionHandler.ACT {
        GlLog.i("InteractiveDecorator.onActionClick [$decoratedItem]")

        interactiveListener?.onItemClicked(decoratedItem)
        return ActionHandler.ACT.HANDLED
    }

    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        return if (decoratedItem.visible &&
                   decoratedItem.boundRect().contains(pos.x, pos.y)) {

            GlLog.i("InteractiveDecorator.onActionSelect [$decoratedItem]")
            trackingItem = decoratedItem
            decoratedItem.inflatePct = 10.0f
            decoratedItem.itemLayer?.bringGraphItemToFront(decoratedItem)
            interactiveListener?.onItemSelected(decoratedItem)

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }

    // --------------------------------------------------------------

    private fun intersectItems() : List< GraphItem > =
        decoratedItem.itemLayer?.itemIntersectRect(
            decoratedItem.boundRect()) { it.visible } ?: listOf()
}


class LongPressProgressDecorator(decoratedItem: GraphItem,
    val progressItem: GraphProgress,
    val abandonOffset: Float,
    var triggerListener: GraphInteractiveListener? = null)
    : GraphActionDecorator(decoratedItem) {

    var longLongPressTimeout = LONG_LONG_PRESS_TIMEOUT

    private var mPressSince: Long = 0
    private lateinit var mHandler : Handler
    private lateinit var mRunnable : Runnable

    // ---------------------------------------------------------------------

    fun init() {
        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable { doPeriod() }
    }

    private fun doPeriod() {
        val duration: Int = (System.currentTimeMillis() - mPressSince).toInt()
        if (duration >= longLongPressTimeout) {
            progressItem.visible = true
            triggerListener?.onItemTriggered(decoratedItem)
        }
        else {
            progressItem.progress = duration.toFloat() / longLongPressTimeout.toFloat()
        }
        mHandler.postDelayed(mRunnable, 100)
    }

    private fun endLongLongPress() {
        mPressSince = 0
        progressItem.visible = false
        progressItem.progress = 0.0f
    }

    // ---------------------------------------------------------------------

    override fun onActionUp(pos: PointF): ActionHandler.ACT {
        endLongLongPress()
        // Leak this action to other handler avoiding issues
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionMove(posBefore: PointF, posNow: PointF,
                              distanceX: Float,distanceY: Float): ActionHandler.ACT {

        if ((abs(decoratedItem.offsetPixel.x) > abandonOffset) ||
            (abs(decoratedItem.offsetPixel.y) > abandonOffset)) {
            endLongLongPress()
        }
        // Leak this action for ?
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        decoratedItem.visible = true
        mPressSince = System.currentTimeMillis()

        return ActionHandler.ACT.HANDLED
    }
}

