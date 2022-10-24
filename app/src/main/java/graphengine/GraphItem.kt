package graphengine
import android.graphics.*


abstract class GraphItem {
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

    var visible: Boolean = true
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

    abstract fun getBoundRect() : RectF
    abstract fun render(canvas: Canvas)

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



