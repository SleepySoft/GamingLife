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
}


class GraphCircle : GraphItem() {
    private var mRadius: Float = 0.0f
    private var mCenter: PointF = PointF(0.0f, 0.0f)

    override fun render(canvas: Canvas) {
        val rCenter = realCenter()
        val rRadius = realRadius()
        canvas.drawCircle(rCenter.x, rCenter.y, rRadius, shapePaint)
    }

    override fun getBoundRect() : RectF {
        val rCenter = realCenter()
        val rRadius = realRadius()
        return RectF(
            rCenter.x - rRadius, rCenter.y - mRadius,
            rCenter.x + mRadius, rCenter.y + mRadius)
    }

    // ---------------------------------------------------------------------

    private fun realCenter() : PointF {
        return PointF(mCenter.x + offsetPixel.x, mCenter.y + offsetPixel.y)
    }

    private fun realRadius() : Float {
        return mRadius * (1.0f + inflatePct)
    }
}




