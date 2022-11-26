package graphengine
import android.graphics.*
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
    var itemLayer: GraphLayer? = null
    var shapePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val graphItemDecorator = mutableListOf< GraphItemDecorator >()
    val graphActionDecorator = mutableListOf< GraphActionDecorator >()

    var inflatePct: Float = 0.0f
        set(value) {
            field = value
        }

    var offsetPixel: PointF = PointF(0.0f, 0.0f)
        set(value) {
            field = value
        }

    fun shiftItem(cx: Float, cy: Float) {
        offsetPixel.x += cx
        offsetPixel.y += cy
    }

    fun cancelShift() {
        offsetPixel.x = 0.0f
        offsetPixel.y = 0.0f
    }

    // ---------------------------------------------------------------------------------------------

    open fun renderWithDecorator(canvas: Canvas) {

    }

    // ---------------------------------------------------------------------------------------------

    override fun render(canvas: Canvas) {
        graphItemDecorator.map { it.paintBeforeGraph(canvas) }
        renderWithDecorator(canvas)
        graphItemDecorator.map { it.paintAfterGraph(canvas) }
    }
}


class GraphLayer(id: String, visible: Boolean,
    val graphView: GraphView) : GraphObject(id, visible) {
    var graphItems: MutableList< GraphItem > = mutableListOf()
        private set

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
        newItem.itemLayer = this
        return insertGraphItem(newItem, 0)
    }

    fun insertGraphItem(newItem: GraphItem, atIndex: Int) : Boolean {
        if (newItem in graphItems) {
            return false
        }
        newItem.itemLayer = this
        graphItems.add(atIndex, newItem)
        return true
    }

    fun insertGraphItemBefore(newItem: GraphItem, refItem: GraphItem) : Boolean {
        val refItemPos = graphItems.indexOf(refItem)
        if (refItemPos >= 0) {
            return insertGraphItem(newItem, refItemPos)
        }
        return false
    }

    fun insertGraphItemAfter(newItem: GraphItem, refItem: GraphItem) : Boolean {
        val refItemPos = graphItems.indexOf(refItem)
        if (refItemPos >= 0) {
            return insertGraphItem(newItem, refItemPos + 1)
        }
        return false
    }

    fun bringGraphItemToFront(item: GraphItem) {
        if (item in graphItems) {
            graphItems.remove(item)
            graphItems.add(0, item)
        }
    }

    fun sendGraphItemToBack(item: GraphItem) {
        if (item in graphItems) {
            graphItems.remove(item)
            graphItems.add(item)
        }
    }

    fun bringGraphItemToFrontOf(operateItem: GraphItem, refItem: GraphItem) : Boolean {
        if (operateItem in graphItems) {
            graphItems.remove(operateItem)
            return insertGraphItemBefore(operateItem, refItem)
        }
        return false
    }

    fun sendGraphItemToBackOf(operateItem: GraphItem, refItem: GraphItem) : Boolean {
        if (operateItem in graphItems) {
            graphItems.remove(operateItem)
            return insertGraphItemAfter(operateItem, refItem)
        }
        return false
    }

    fun pickGraphItems(filter: (input: GraphItem) -> Boolean) : List< GraphItem > {
        return graphItems.filter(filter)
    }

    fun removeGraphItem(filter: (input: GraphItem) -> Boolean) {
        graphItems.filter { filter(it) }.map { it.itemLayer = null }
        graphItems = graphItems.filter { !filter(it) }.toMutableList()
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
        for (item in graphItems) {
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
        for (item in graphItems.reversed()) {
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
    var origin: PointF = PointF(0.0f, 0.0f)

    override fun renderWithDecorator(canvas: Canvas) {
        val rCenter = realOrigin()
        val rRadius = realRadius()
        canvas.drawCircle(rCenter.x, rCenter.y, rRadius, shapePaint)
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


open class GraphProgress(
    protected val mWrapItem: GraphItem,
    protected val mAroundInflatePct: Float) : GraphItem() {

    var progress: Float = 0.0f

    override fun boundRect(): RectF {
        return mWrapItem.boundRect().apply { inflate(mAroundInflatePct) }
    }

    override fun moveCenter(pos: PointF) {

    }
}


class GraphCircleProgress(
    wrapItem: GraphCircle, aroundInflatePct: Float) :
    GraphProgress(wrapItem, aroundInflatePct) {

    override fun render(canvas: Canvas) {
        canvas.drawArc(boundRect(), -90.0f, 360.0f * progress, true, shapePaint)
    }
}


class GraphRectangle : GraphItem() {
    var rect: RectF = RectF()
    var roundRadius: Float = 0.0f

    // ---------------------------------------------------------------------

    override fun renderWithDecorator(canvas: Canvas) {
        val rRect = boundRect()

        if (roundRadius < 0.001f) {
            canvas.drawRect(rRect, shapePaint)
        }
        else {
            canvas.drawRoundRect(rRect, roundRadius, roundRadius, shapePaint)
        }
    }

    override fun boundRect() : RectF =
        RectF(rect).apply { this.offset(offsetPixel.x, offsetPixel.y) }

    override fun moveCenter(pos: PointF) {
        rect.offset(pos.x - rect.centerX(), pos.y - rect.centerY())
    }
}


class GraphImage(
    private val mBitmapImage: Bitmap) : GraphItem() {

    enum class PAINT_MODE {
        SCALE, ORIGIN, STRETCH
    }

    var blitArea: RectF = RectF()
        private set

    var imageBounds: Rect = Rect(0, 0, mBitmapImage.width, mBitmapImage.height)
        private set

    var paintMode: PAINT_MODE = PAINT_MODE.SCALE
        set(value) {
            field = value
            updateBlitArea()
        }

    var paintArea: RectF = imageBounds.toRectF()
        set(value) {
            field = value
            updateBlitArea()
        }

    private fun updateBlitArea() {
        val centerBackup = paintArea.centerPoint()

        when (paintMode) {
            PAINT_MODE.SCALE -> {
                val ratioHori = paintArea.width() / imageBounds.width().toFloat()
                val ratioVert = paintArea.height() / imageBounds.height().toFloat()
                val scaleRatio = minOf(ratioHori, ratioVert)

                val imageBoundsF = imageBounds.toRectF()
                blitArea = RectF(
                    imageBoundsF.left, imageBoundsF.top,
                    imageBoundsF.left + imageBoundsF.width() * scaleRatio,
                    imageBoundsF.top + imageBoundsF.height() * scaleRatio)
            }
            PAINT_MODE.ORIGIN -> {
                blitArea = RectF(imageBounds)
            }
            PAINT_MODE.STRETCH -> {
                blitArea = paintArea
            }
        }

        blitArea.moveCenter(centerBackup)
    }

    // ---------------------------------------------------------------------

    override fun boundRect(): RectF =
        RectF(blitArea).apply { offset(offsetPixel.x, offsetPixel.y) }

    override fun render(canvas: Canvas) {
        if (blitArea.isEmpty) {
            updateBlitArea()
        }
        canvas.drawBitmap(mBitmapImage, imageBounds, boundRect(), shapePaint)
    }

    override fun moveCenter(pos: PointF) {
        paintArea.moveCenter(pos)
        updateBlitArea()
    }
}
