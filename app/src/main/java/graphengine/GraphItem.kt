package graphengine
import android.graphics.*


abstract class GraphObject(
    var id: String = "",
    var visible: Boolean = true) {

    abstract fun getBoundRect() : RectF
    abstract fun render(canvas: Canvas)
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

    var pickable: Boolean = true
    var needRender: Boolean = true

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


class GraphLayer : GraphObject() {
    private var mGraphItems: MutableList< GraphItem > = mutableListOf()

    // -------------------------- Public --------------------------

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

    private fun itemFromPoint(pos: PointF, filter: ) : MutableList< GraphItem > {
        val items = mutableListOf< GraphItem >()
        for (item in mGraphItems) {
            if (item.getBoundRect().contains(pos.x, pos.y)) {
                if (onlyVisible) {
                    if () {

                    }
                }
                else {
                    items.add(item)
                }
            }
        }
        return items
    }

    private fun pickableItemFromPoint(pos: PointF) : GraphItem? {
        var selItem: GraphItem? = null
        val selItems = itemFromPoint(pos)
        for (item in selItems) {
            if (item.pickable) {
                selItem = item
                break
            }
        }
        return selItem
    }

    // -------------------------- Override --------------------------

    override fun getBoundRect(): RectF {
        var rect: RectF = RectF()
        for (item in mGraphItems) {
            if (item.visible) {
                if (rect.isEmpty) {
                    rect = item.getBoundRect()
                }
                else {
                    rect.union(item.getBoundRect())
                }
            }
        }
        return rect
    }

    override fun render(canvas: Canvas) {
        for (item in mGraphItems.reversed()) {
            if (item.visible) {
                item.render(canvas)
            }
        }
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
            val bound = getBoundRect()
            bound.apply {
                val inflateHori = this.width() * 0.2f
                val inflateVert = this.height() * 0.2f
                this.top += inflateVert
                this.left += inflateHori
                this.right -= inflateHori
                this.bottom -= inflateVert
            }
            drawableContainer = rectF2Rect(bound)
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

    override fun getBoundRect() : RectF {
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
}


class GraphCircleProgress(
    private val mWrapCircle: GraphCircle,
    private val mAroundInflatePct: Float) : GraphItem() {

    var progress: Float = 0.0f

    override fun getBoundRect(): RectF {
        return inflateRectF(mWrapCircle.getBoundRect(), mAroundInflatePct)
    }

    override fun render(canvas: Canvas) {
        canvas.drawArc(getBoundRect(), -90.0f, 360.0f * progress, true, shapePaint)
        // canvas.drawRect(getBoundRect(), shapePaint)
    }
}



