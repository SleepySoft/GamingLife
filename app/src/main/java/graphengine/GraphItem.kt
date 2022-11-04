package graphengine
import android.R
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.BitmapFactory

import android.graphics.Bitmap





abstract class GraphObject(
    var id: String = "",
    var visible: Boolean = true) {

    abstract fun boundRect() : RectF
    abstract fun render(canvas: Canvas)
    abstract fun moveCenter(pos: PointF)
}


abstract class GraphItem(
    id: String = "",
    visible: Boolean = true) :
    GraphObject(id, visible) {

    var itemData: Any? = null

    var subText: String = ""
        set(value) {
            field = value
            needRender = true
        }
    var mainText: String = ""
        set(value) {
            field = value
            needRender = true
        }

    var needRender: Boolean = true
    var interactive: Boolean = true

    var fontPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var shapePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var inflatePct: Float = 0.0f
        set(value) {
            field = value
            needRender = true
        }

    var offsetPixel: PointF = PointF(0.0f, 0.0f)
        set(value) {
            field = value
            needRender = true
        }

    fun shiftItem(cx: Float, cy: Float) {
        offsetPixel.x += cx
        offsetPixel.y += cy
        needRender = true
    }

    fun cancelShift() {
        offsetPixel.x = 0.0f
        offsetPixel.y = 0.0f
        needRender = true
    }
}


class GraphLayer(id: String, visible: Boolean) : GraphObject(id, visible) {
    private var mGraphItems: MutableList< GraphItem > = mutableListOf()

    private val mBackGroundPaint: Paint = Paint().apply {
        this.color = 0x00FFFFFF
    }

    var coverArea: RectF = RectF()

    // -------------------------- Public --------------------------

    fun setBackgroundColor(color: String) {
        val alpha = mBackGroundPaint.alpha
        mBackGroundPaint.color = Color.parseColor(color)
        mBackGroundPaint.alpha = alpha

    }

    fun setBackgroundAlpha(alpha: Int) {
        mBackGroundPaint.alpha = alpha
    }

    // ---------------- GraphItem Management ----------------

    fun addGraphItem(newItem: GraphItem) : Boolean {
        return insertGraphItem(newItem, 0)
    }

    fun insertGraphItem(newItem: GraphItem, atIndex: Int) : Boolean {
        if (newItem in mGraphItems) {
            return false
        }
        mGraphItems.add(atIndex, newItem)
        return true
    }

    fun insertGraphItemBefore(newItem: GraphItem, refItem: GraphItem) : Boolean {
        val refItemPos = mGraphItems.indexOf(refItem)
        if (refItemPos >= 0) {
            return insertGraphItem(newItem, refItemPos)
        }
        return false
    }

    fun insertGraphItemAfter(newItem: GraphItem, refItem: GraphItem) : Boolean {
        val refItemPos = mGraphItems.indexOf(refItem)
        if (refItemPos >= 0) {
            return insertGraphItem(newItem, refItemPos + 1)
        }
        return false
    }

    fun bringGraphItemToFront(item: GraphItem) {
        if (item in mGraphItems) {
            mGraphItems.remove(item)
            mGraphItems.add(0, item)
        }
    }

    fun sendGraphItemToBack(item: GraphItem) {
        if (item in mGraphItems) {
            mGraphItems.remove(item)
            mGraphItems.add(item)
        }
    }

    fun bringGraphItemToFrontOf(operateItem: GraphItem, refItem: GraphItem) : Boolean {
        if (operateItem in mGraphItems) {
            mGraphItems.remove(operateItem)
            return insertGraphItemBefore(operateItem, refItem)
        }
        return false
    }

    fun sendGraphItemToBackOf(operateItem: GraphItem, refItem: GraphItem) : Boolean {
        if (operateItem in mGraphItems) {
            mGraphItems.remove(operateItem)
            return insertGraphItemAfter(operateItem, refItem)
        }
        return false
    }

    fun pickGraphItems(filter: (input: GraphItem) -> Boolean) : List< GraphItem > {
        return mGraphItems.filter(filter)
    }

    fun removeGraphItem(filter: (input: GraphItem) -> Boolean) {
        val remainingItems = mGraphItems.filter { !filter(it) }
        mGraphItems = remainingItems.toMutableList()
    }

    fun itemFromPoint(pos: PointF) : List< GraphItem > {
        return pickGraphItems() { it.boundRect().contains(pos.x, pos.y) }
    }

    fun itemFromPoint(pos: PointF,
                      filter: (input: GraphItem) -> Boolean) : List< GraphItem > {
        return pickGraphItems() { it.boundRect().contains(pos.x, pos.y) && filter(it) }
    }

    fun itemIntersectRect(rect: RectF): List<GraphItem> {
        return pickGraphItems() {
            val intersect = it.boundRect().intersects(rect.left, rect.top, rect.right, rect.bottom)
            // println("Item $it [${it.id}] bound ${it.boundRect()} intersect with $rect - $intersect")
            intersect
        }
    }

    fun itemIntersectRect(rect: RectF,
                          filter: (input: GraphItem) -> Boolean): List<GraphItem> {
        return pickGraphItems() {
            val intersect = it.boundRect().intersects(rect.left, rect.top, rect.right, rect.bottom)
            // println("Item $it [${it.id}] bound ${it.boundRect()} intersect with $rect - $intersect")
            intersect && filter(it)
        }
    }

    // -------------------------- Override --------------------------

    override fun boundRect(): RectF {
        var rect: RectF = RectF()
        for (item in mGraphItems) {
            if (item.visible) {
                if (rect.isEmpty) {
                    rect = item.boundRect()
                }
                else {
                    rect.union(item.boundRect())
                }
            }
        }
        return rect
    }

    override fun render(canvas: Canvas) {
        canvas.drawRect(coverArea, mBackGroundPaint)
        for (item in mGraphItems.reversed()) {
            if (item.visible) {
                item.render(canvas)
            }
        }
    }

    override fun moveCenter(pos: PointF) {
        // Not available for Layer
    }
}


class GraphCircle : GraphItem() {
    var radius: Float = 0.0f
        set(value) {
            field = value
            needRender = true
        }

    var origin: PointF = PointF(0.0f, 0.0f)
        set(value) {
            field = value
            needRender = true
        }

    private var boundaryOfText: Rect = Rect()
    private var drawableContainer: Rect = Rect()

    override fun render(canvas: Canvas) {
        val rCenter = realOrigin()
        val rRadius = realRadius()

        canvas.drawCircle(rCenter.x, rCenter.y, rRadius, shapePaint)

        if (needRender) {
            drawableContainer = rectF2Rect(inflateRectF(boundRect(), 0.7f))
            val fontSize = calculateFontSize(boundaryOfText, drawableContainer, mainText)
            fontPaint.setTextSize(fontSize)
            needRender = false
        }

        val halfTextHeight: Float = boundaryOfText.height() / 2.0f
        canvas.drawText(
            mainText,
            drawableContainer.centerX().toFloat(),
            (drawableContainer.centerY().toFloat() + halfTextHeight),
            fontPaint
        )
    }

    override fun boundRect() : RectF {
        val rCenter = realOrigin()
        val rRadius = realRadius()
        return RectF(
            rCenter.x - rRadius, rCenter.y - radius,
            rCenter.x + radius, rCenter.y + radius)
    }

    // ---------------------------------------------------------------------

    private fun realOrigin() : PointF {
        return PointF(origin.x + offsetPixel.x, origin.y + offsetPixel.y)
    }

    private fun realRadius() : Float {
        return radius * (1.0f + inflatePct / 100)
    }

    override fun moveCenter(pos: PointF) {
        origin = pos
    }
}


class GraphCircleProgress(
    private val mWrapCircle: GraphCircle,
    private val mAroundInflatePct: Float) : GraphItem() {

    var progress: Float = 0.0f

    override fun boundRect(): RectF {
        return inflateRectF(mWrapCircle.boundRect(), mAroundInflatePct)
    }

    override fun render(canvas: Canvas) {
        canvas.drawArc(boundRect(), -90.0f, 360.0f * progress, true, shapePaint)
        // canvas.drawRect(getBoundRect(), shapePaint)
    }

    override fun moveCenter(pos: PointF) {

    }
}


class GraphRectangle : GraphItem() {
    var rect: RectF = RectF()
        set(value) {
            field = value
            needRender = true
        }

    var roundRadius: Float = 0.0f
        set(value) {
            field = value
            needRender = true
        }

    private var boundaryOfText: Rect = Rect()
    private var drawableContainer: Rect = Rect()

    // ---------------------------------------------------------------------

    override fun render(canvas: Canvas) {
        val rRect = boundRect()

        if (roundRadius < 0.001f) {
            canvas.drawRect(rRect, shapePaint)
        }
        else {
            canvas.drawRoundRect(rRect, roundRadius, roundRadius, shapePaint)
        }

        if (needRender) {
            drawableContainer = rectF2Rect(inflateRectF(boundRect(), 0.8f))
            val fontSize = calculateFontSize(boundaryOfText, drawableContainer, mainText)
            fontPaint.setTextSize(fontSize)
            needRender = false
        }

        val halfTextHeight: Float = boundaryOfText.height() / 2.0f
        canvas.drawText(
            mainText,
            drawableContainer.centerX().toFloat(),
            (drawableContainer.centerY().toFloat() + halfTextHeight),
            fontPaint
        )
    }

    override fun boundRect() : RectF =
        RectF(rect).apply { this.offset(offsetPixel.x, offsetPixel.y) }

    override fun moveCenter(pos: PointF) {
        rect.offset(pos.x - rect.centerX(), pos.y - rect.centerY())
    }
}


class GraphImage(
    private val mBitmapImage: Bitmap) : GraphItem() {

    var imageBounds: Rect = Rect(
        0, mBitmapImage.width,
        0, mBitmapImage.height)
        private set

    private var mImageArea: RectF = rect2RectF(imageBounds)

    fun scaleReset() {
        mImageArea = rect2RectF(imageBounds)
    }

    fun scaleToFit(refRect: RectF) {
        val ratioHori = refRect.width() / imageBounds.width().toFloat()
        val ratioVert = refRect.height() / imageBounds.height().toFloat()
        val scaleRatio = minOf(ratioHori, ratioVert)

        val imageBoundsF = rect2RectF(imageBounds)
        val centerBackup = PointF(mImageArea.centerX(), mImageArea.centerY())
        mImageArea = RectF(
            imageBoundsF.left, imageBoundsF.left + imageBoundsF.width() * scaleRatio,
            imageBoundsF.top, imageBoundsF.top + imageBoundsF.height() * scaleRatio)
        moveCenter(centerBackup)
    }

    // ---------------------------------------------------------------------

    override fun boundRect(): RectF =
        RectF(mImageArea).apply { offset(offsetPixel.x, offsetPixel.y) }

    override fun render(canvas: Canvas) {
        canvas.drawBitmap(mBitmapImage, imageBounds, mImageArea, shapePaint)
    }

    override fun moveCenter(pos: PointF) {
        mImageArea.offset(pos.x - mImageArea.centerX(), pos.y - mImageArea.centerY())
    }
}
