package graphengine
import android.graphics.*


abstract class GraphItem {
    var itemData: Any? = null
    var mainText: String = ""
    var needRender: Boolean = true

    var fontPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var shapePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var unitScale: Float = 1.0f
        set(value) {
            field = value
            needRender = true
        }

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

    override fun render(canvas: Canvas) {
        val rCenter = realOrigin()
        val rRadius = realRadius()
        canvas.drawCircle(rCenter.x, rCenter.y, rRadius, shapePaint)
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
        return radius * (1.0f + inflatePct)
    }
}




