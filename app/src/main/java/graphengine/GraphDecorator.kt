package graphengine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.os.Handler
import android.os.Looper


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

class InteractiveDecorator(decoratedItem: GraphItem) :
    GraphActionDecorator(decoratedItem) {

    companion object {
        var trackingItem: GraphItem? = null
    }

    interface ClickListener {
        fun onItemClicked(clickedItem: GraphItem)
    }
    val clickListener: ClickListener? = null

    override fun onActionUp(pos: PointF): ActionHandler.ACT {
        if (trackingItem == decoratedItem) {
            trackingItem = null
        }
        // Leak this action to other handler avoiding issues
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionMove(posBefore: PointF, posNow: PointF,
                              distanceX: Float,distanceY: Float): ActionHandler.ACT {
        return if (decoratedItem == trackingItem) {
            decoratedItem.shiftItem(-distanceX, -distanceY)

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }

    override fun onActionClick(pos: PointF) : ActionHandler.ACT {
        clickListener?.onItemClicked(decoratedItem)
        return ActionHandler.ACT.HANDLED
    }

    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        return if (decoratedItem.visible &&
                   decoratedItem.boundRect().contains(pos.x, pos.y)) {
            trackingItem = decoratedItem

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }
}


class LongPressProgressDecorator(decoratedItem: GraphItem) :
    GraphActionDecorator(decoratedItem) {

    interface TriggerListener {
        fun onLongPressTriggered(triggerItem: GraphItem)
    }
    val triggerListener: TriggerListener? = null

    private lateinit var mHandler : Handler
    private lateinit var mRunnable : Runnable

    // ---------------------------------------------------------------------

    fun init() {
        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable { doPeriod() }
    }

    private fun doPeriod() {
        mHandler.postDelayed(mRunnable, 100)
    }

    // ---------------------------------------------------------------------

    override fun onActionUp(pos: PointF): ActionHandler.ACT {
        // Leak this action to other handler avoiding issues
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionMove(posBefore: PointF, posNow: PointF,
                              distanceX: Float,distanceY: Float): ActionHandler.ACT {
        return if (decoratedItem == trackingItem) {
            decoratedItem.shiftItem(-distanceX, -distanceY)

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }

    override fun onActionClick(pos: PointF) : ActionHandler.ACT {
        clickListener?.onItemClicked(decoratedItem)
        return ActionHandler.ACT.HANDLED
    }

    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        return if (decoratedItem.visible &&
            decoratedItem.boundRect().contains(pos.x, pos.y)) {
            trackingItem = decoratedItem

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }
}

