package graphengine

import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import glcore.GlLog
import glcore.LONG_LONG_PRESS_TIMEOUT
import kotlin.math.abs


open class GraphItemDecorator(
    val context: GraphContext,
    val decoratedItem: GraphItem) {

    open fun paintBeforeGraph(canvas: Canvas) {

    }
    open fun paintAfterGraph(canvas: Canvas) {

    }
}


open class GraphActionDecorator(context: GraphContext, decoratedItem: GraphItem)
    : GraphItemDecorator(context, decoratedItem), ActionHandler {

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

class AutoFitTextDecorator(context: GraphContext, decoratedItem: GraphItem)
    : GraphItemDecorator(context, decoratedItem) {

    var mainText: String = ""
        set(value) {
            field = value
        }
    var fontPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var textArea: Rect = Rect()
        private set

    var textBound: Rect = Rect()
        private set

    private var textPrev: String = ""

    override fun paintAfterGraph(canvas: Canvas) {
        val newTextArea = decoratedItem.boundRect().apply { inflate(0.7f) }.toRect()
        if ((textArea != newTextArea) || (textPrev.length != mainText.length)) {
            textPrev = mainText
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

class MultipleProgressDecorator(context: GraphContext, decoratedItem: GraphItem)
    : GraphItemDecorator(context, decoratedItem) {

    // The progress pct records
    data class ProgressData(
        var progressPct: Float,
        var progressPaint: Paint
    )

    var progressEnd: Float = 0.0f
    var progressData = mutableListOf< ProgressData >()

    /*
    The ProgressData records the start of this progress
    The bar before progressData[0].progressPct and after progressEnd will not be filled.

          progressData[0].progressPct       progressData[1].progressPct                         100%
    |     |###################|*************|=======================================|           |
                               progressData[1].progressPct                          progressEnd
    */

    override fun paintAfterGraph(canvas: Canvas) {
        val wholeRect = decoratedItem.boundRect()
        val subRect = RectF(wholeRect).apply { right = left }

        for (i in 0 until progressData.size) {
            subRect.left = pctToX(progressData[i].progressPct)
            subRect.right = pctToX(if (i == progressData.size - 1) {
                progressEnd
            } else {
                progressData[i + 1].progressPct
            })
            canvas.drawRect(subRect, progressData[i].progressPaint)
        }
    }

    private fun pctToX(pct: Float): Float {
        val wholeRect = decoratedItem.boundRect()
        return wholeRect.left + pct * wholeRect.width()
    }
}


// ---------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------

class ClickDecorator(
    context: GraphContext,
    decoratedItem: GraphItem,
    var interactiveListener: GraphInteractiveListener? = null)
    : GraphActionDecorator(context, decoratedItem) {

    override fun onActionClick(pos: PointF): ActionHandler.ACT {
        GlLog.i("ClickDecorator.onActionClick [$decoratedItem]")

        interactiveListener?.onItemClicked(decoratedItem)
        context.refresh()
        return ActionHandler.ACT.HANDLED
    }
}


class InteractiveDecorator(
    context: GraphContext,
    decoratedItem: GraphItem,
    var interactiveListener: GraphInteractiveListener? = null)
    : GraphActionDecorator(context, decoratedItem) {

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
            interactiveListener?.onItemDropped(
                decoratedItem, intersectItems(decoratedItem))
            trackingItem = null
            context.refresh()
        }
        // Leak this action to other handler avoiding issues
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionMove(posBefore: PointF, posNow: PointF,
                              distanceX: Float,distanceY: Float): ActionHandler.ACT {
        return if (decoratedItem == trackingItem) {
            GlLog.i("InteractiveDecorator.onActionMove [$decoratedItem]")

            decoratedItem.shiftItem(-distanceX, -distanceY)
            interactiveListener?.onItemDragging(
                decoratedItem, intersectItems(decoratedItem))
            context.refresh()

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }

/*    override fun onActionClick(pos: PointF) : ActionHandler.ACT {
        GlLog.i("InteractiveDecorator.onActionClick [$decoratedItem]")

        interactiveListener?.onItemClicked(decoratedItem)
        context.invalidate()
        return ActionHandler.ACT.HANDLED
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        return if (decoratedItem.visible &&
                   decoratedItem.boundRect().contains(pos.x, pos.y)) {

            GlLog.i("InteractiveDecorator.onActionSelect [$decoratedItem]")

            trackingItem = decoratedItem
            decoratedItem.inflatePct = 10.0f
            decoratedItem.itemLayer?.bringGraphItemToFront(decoratedItem)
            interactiveListener?.onItemSelected(decoratedItem)

            context.vibrate(100)
            context.refresh()

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }

    // --------------------------------------------------------------

    private fun intersectItems(exceptItem: GraphItem?) : List< GraphItem > =
        decoratedItem.itemLayer?.itemIntersectRect(
            decoratedItem.boundRect()) { it.visible && it != exceptItem } ?: listOf()
}


class LongPressProgressDecorator(
    context: GraphContext, decoratedItem: GraphItem,
    val progressItem: GraphProgress,
    val abandonOffset: Float,
    var triggerListener: GraphInteractiveListener? = null)
    : GraphActionDecorator(context, decoratedItem) {

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
        if (progressItem.visible) {
            val duration: Int = (System.currentTimeMillis() - mPressSince).toInt()
            if (duration >= longLongPressTimeout) {
                progressItem.visible = false
                triggerListener?.onItemTriggered(decoratedItem)
            }
            else {
                progressItem.progress = duration.toFloat() / longLongPressTimeout.toFloat()
            }
            context.refresh()
            mHandler.postDelayed(mRunnable, 100)
        }
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
        progressItem.visible = true
        mPressSince = System.currentTimeMillis()
        mHandler.postDelayed(mRunnable, 100)

        return ActionHandler.ACT.HANDLED
    }
}

