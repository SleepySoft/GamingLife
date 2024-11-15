package graphengine

import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
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

    // --------------------------------------------------------------

    protected fun intersectItems(exceptItem: GraphItem?) : List< GraphItem > =
        decoratedItem.itemLayer?.itemIntersectRect(
            decoratedItem.boundRect()) { it.visible && it != exceptItem } ?: listOf()
}


// ---------------------------------------------------------------------------------------------

class AutoFitTextDecorator(context: GraphContext, decoratedItem: GraphItem) :
    GraphItemDecorator(context, decoratedItem) {

    init {
        decoratedItem.service.serviceRegister("AutoFitTextDecorator") {
            return@serviceRegister this
        }
        decoratedItem.service.serviceRegister("AutoFitTextDecorator.setText") { text: String ->
            mainText = text
            return@serviceRegister true
        }
    }

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

class CornerTextDecorator(context: GraphContext, decoratedItem: GraphItem,
                          var corner: Int = CORNER_UPPER_RIGHT,
                          var ratio: Float = 0.4f)
    : GraphItemDecorator(context, decoratedItem) {

    init {
        decoratedItem.service.serviceRegister("CornerTextDecorator") {
            return@serviceRegister this
        }
        decoratedItem.service.serviceRegister("CornerTextDecorator.setText") { text: String ->
            mainText = text
            return@serviceRegister true
        }
        decoratedItem.service.serviceRegister("CornerTextDecorator.setVisible") { vis: Boolean ->
            visible = vis
            return@serviceRegister true
        }
    }

    companion object {
        const val CORNER_UPPER_LEFT = 0b00
        const val CORNER_UPPER_RIGHT = 0b01
        const val CORNER_BOTTOM_LEFT = 0b10
        const val CORNER_BOTTOM_RIGHT = 0b11
    }

    var visible = true

    var mainText: String = ""
        set(value) {
            field = value
        }
    var fontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

    var textBkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED }

    override fun paintAfterGraph(canvas: Canvas) {
        if (visible) {
            val boundRect = decoratedItem.boundRect()
            val textBkHeight = boundRect.height() * ratio

            val textHeight = textBkHeight * 0.7f
            fontPaint.textSize = textHeight
            val textWidth = fontPaint.measureText(mainText)

            var textBkWidth = textWidth * 1.2f
            if (textBkWidth < textBkHeight) {
                textBkWidth = textBkHeight
            }

            val textBkRect = RectF().apply {
                left = if (corner and 0b01 == 0) boundRect.left else boundRect.right - textBkWidth
                top = if (corner and 0b10 == 0) boundRect.top else boundRect.bottom - textBkHeight
                right = left + textBkWidth
                bottom = top + textBkHeight
            }

            canvas.drawRoundRect(
                textBkRect, textBkRect.height() / 2, textBkRect.height() / 2, textBkPaint)
            canvas.drawText(
                mainText, textBkRect, ALIGN_HORIZON_MIDDLE, ALIGN_VERTICAL_CENTER,  fontPaint)
        }
    }
}


// ---------------------------------------------------------------------------------------------

class MultipleSectionProgressDecorator(context: GraphContext, decoratedItem: GraphItem)
    : GraphItemDecorator(context, decoratedItem) {

    // The progress pct records
    data class ProgressData(
        var progressPct: Float,
        var progressPaint: Paint,
        var userData: Any? = null
    )

    data class ProgressScale(
        var progressPct: Float,
        var scaleText: String
    )

    // TODO: Relative text size
    var fontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 35.0f
    }

    var progressEnd: Float = 0.0f
    var progressData = mutableListOf< ProgressData >()
    var progressScale = mutableListOf< ProgressScale >()

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

        for (scale in progressScale) {
            val rectTextBounds = Rect()
            fontPaint.getTextBounds(scale.scaleText, 0, scale.scaleText.length, rectTextBounds)

            val scaleTextCenter = PointF(
                pctToX(scale.progressPct),
                wholeRect.top - 10 - rectTextBounds.height()
            )

            val rectTextPaint = rectTextBounds.toRectF()
            rectTextPaint.moveCenter(scaleTextCenter)

            canvas.drawLine(
                scaleTextCenter.x,
                wholeRect.top,

                scaleTextCenter.x,
                wholeRect.top - 10.0f,

                Paint().apply {
                    strokeWidth = 2.0f
                }
            )

            canvas.drawText(
                scale.scaleText,
                rectTextPaint.left,
                rectTextPaint.centerY(),
                fontPaint
            )
        }
    }

    fun xToPct(x: Float) : Float {
        val wholeRect = decoratedItem.boundRect()
        return (x - wholeRect.left) / wholeRect.width()
    }

    fun pctToX(pct: Float) : Float {
        val wholeRect = decoratedItem.boundRect()
        return wholeRect.left + pct * wholeRect.width()
    }

    fun pctToProgressData(pct: Float) : ProgressData? {
        var data: ProgressData? = null
        for (i in 0 .. progressData.size) {
            val judgePct = if (i == progressData.size) progressEnd else progressData[i].progressPct
            if (judgePct >= pct) {
                data = if (i > 0) progressData[i - 1] else null
                break
            }
        }
        return data
    }
}


class MultipleHorizonStatisticsBarDecorator(context: GraphContext, decoratedItem: GraphItem)
    : GraphItemDecorator(context, decoratedItem) {

    /*
    * Draw multiple horizon bars, which layout from left to barRightLimitPct (of the whole paint width).
    *     Multiple bars stack vertical.
    * If the largest value is less than defaultMaxValue, the whole bar length means defaultMaxValue.
    *     else the the whole bar length is the max value.
    */

    data class BarData(
        var text: String,
        var value: Float,
        var barPaint: Paint,
        var textPaint: Paint
    )

    var emptyAreaPaint = Paint().apply {
        color = Color.parseColor("#FFFFFF")
    }

    var barDatas = mutableListOf< BarData >()
    var defaultMaxValue: Float = 100.0f
    var barRightLimitPct: Float = 0.70f
    var barTextFormatter: ((BarData) -> String)? = null

    override fun paintAfterGraph(canvas: Canvas) {
        val wholeRect = decoratedItem.boundRect()
        val heightDivide = wholeRect.height() / barDatas.size
        val horizonLimit = barRightLimitPct * wholeRect.width()

        var barMaxValue = barDatas.maxOf { it.value }
        if (barMaxValue < defaultMaxValue) { barMaxValue = defaultMaxValue }

        val gap = wholeRect.width() * barRightLimitPct
        for (i in 0 until barDatas.size) {
            val top = wholeRect.top + i * heightDivide
            val bottom = top + heightDivide
            val fullRect = RectF(wholeRect.left, top, gap, bottom)
            val textRect = RectF(gap, top, wholeRect.right, bottom)
            val barRect = RectF(fullRect).apply {
                // inflate(-3.0f, -3.0f, -3.0f, -3.0f)
                right = fullRect.left + barDatas[i].value * horizonLimit / barMaxValue
            }
            val displayText = (barTextFormatter ?: { barData -> barData.text  })(barDatas[i])

            canvas.drawRect(fullRect, emptyAreaPaint)
            canvas.drawRect(barRect, barDatas[i].barPaint)
            canvas.drawText(displayText, textRect, ALIGN_HORIZON_RIGHT, ALIGN_VERTICAL_CENTER, barDatas[i].textPaint)
        }
    }

    private fun defaultBarTextFormatter(barData: BarData) : String = barData.text
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


/*class ActionPenetrateDecorator(
    context: GraphContext,
    decoratedItem: GraphItem,
    var interactiveListener: GraphInteractiveListener? = null)
    : GraphActionDecorator(context, decoratedItem) {

    override fun onActionUp(pos: PointF): ActionHandler.ACT {
        interactiveListener?.onItemDropped(decoratedItem, intersectItems(decoratedItem), pos)
        context.refresh()
        return ActionHandler.ACT.HANDLED
    }

*//*    override fun onActionDown(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }*//*

    override fun onActionMove(
        posBefore: PointF,
        posNow: PointF,
        distanceX: Float,
        distanceY: Float
    ): ActionHandler.ACT {
        interactiveListener?.onItemDragging(decoratedItem, intersectItems(decoratedItem))
        context.refresh()
        return ActionHandler.ACT.IGNORED
    }

*//*    override fun onActionFling(
        posBefore: PointF,
        posNow: PointF,
        velocityX: Float,
        velocityY: Float
    ): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }*//*

    override fun onActionClick(pos: PointF): ActionHandler.ACT {
        interactiveListener?.onItemClicked(decoratedItem)
        context.refresh()
        return ActionHandler.ACT.HANDLED
    }

    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionLongPress(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }
}*/


class InteractiveDecorator(
    context: GraphContext,
    decoratedItem: GraphItem,
    var autoHandleDragging: Boolean = true,
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

            if (autoHandleDragging) {
                decoratedItem.inflatePct = 0.0f
            }

            interactiveListener?.onItemDropped(
                decoratedItem, intersectItems(decoratedItem), pos)
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

            if (autoHandleDragging) {
                decoratedItem.shiftItem(-distanceX, -distanceY)
            }
            interactiveListener?.onItemDragging(
                decoratedItem, intersectItems(decoratedItem), posNow)
            context.refresh()

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }

    override fun onActionClick(pos: PointF) : ActionHandler.ACT {
        return if (decoratedItem.visible &&
            decoratedItem.boundRect().contains(pos.x, pos.y)) {

            GlLog.i("InteractiveDecorator.onActionClick [$decoratedItem]")

            interactiveListener?.onItemClicked(decoratedItem)

            ActionHandler.ACT.HANDLED
        } else {
            ActionHandler.ACT.IGNORED
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        return if (decoratedItem.visible &&
                   decoratedItem.boundRect().contains(pos.x, pos.y)) {

            GlLog.i("InteractiveDecorator.onActionSelect [$decoratedItem]")

            trackingItem = decoratedItem

            if (autoHandleDragging) {
                decoratedItem.inflatePct = 10.0f
                decoratedItem.itemLayer?.bringGraphItemToFront(decoratedItem)
            }

            context.vibrate(100)
            interactiveListener?.onItemSelected(decoratedItem, pos)
            context.refresh()

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }
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

