package graphengine

import android.graphics.*
import kotlin.math.sqrt


/*fun rectF2Rect(rectF: RectF): Rect {
    val rect = Rect()
    rectF.round(rect)
    return rect
}

fun rect2RectF(rect: Rect): RectF {
    return RectF(
        rect.left.toFloat(),
        rect.top.toFloat(),
        rect.right.toFloat(),
        rect.bottom.toFloat()
    )
}*/

// https://www.geeksforgeeks.org/find-two-rectangles-overlap/

fun rectOverlap(rect1: RectF, rect2: RectF): Boolean {
    if (rect1.isEmpty || rect2.isEmpty) {
        println("false - 1")
        return false
    }
    if ((rect1.left > rect2.right) || (rect2.left > rect1.right)) {
        println("false - 2")
        return false
    }
    if ((rect1.bottom < rect2.top) || (rect2.bottom < rect1.top)) {
        println("false - 3")
        return false
    }
    return true
}

/*fun centerFOfRectF(rectF: RectF) : PointF = PointF(rectF.centerX(), rectF.centerY())*/

/*fun inflateRectF(rectF: RectF, inflatePct: Float): RectF {
    val horiInflate: Float = rectF.width() * (inflatePct - 1.0f) / 2.0f
    val vertInflate: Float = rectF.height() * (inflatePct - 1.0f) / 2.0f
    return RectF(rectF.left - horiInflate, rectF.top - vertInflate,
        rectF.right + horiInflate, rectF.bottom + vertInflate)
}*/


// ---------------------------------------- Extend of Rect -----------------------------------------

fun Rect.toRectF() : RectF {
    return RectF(
        left.toFloat(),
        top.toFloat(),
        right.toFloat(),
        bottom.toFloat()
    )
}


// ---------------------------------------- Extend of RectF ----------------------------------------

fun RectF.fromCenterSides(center: PointF, vertLen: Float, horiLen: Float) {
    left = center.x - vertLen / 2
    top = center.y - horiLen / 2
    right = left + vertLen
    bottom = bottom + horiLen
}

fun RectF.centerPoint() : PointF = PointF(centerX(), centerY())

fun RectF.moveCenter(center: PointF) {
    this.offset(center.x - this.centerX(), center.y - this.centerY())
}

fun RectF.inflate(inflatePct: Float) {
    val horiInflate: Float = width() * (inflatePct - 1.0f) / 2.0f
    val vertInflate: Float = height() * (inflatePct - 1.0f) / 2.0f

    left -= horiInflate
    top -= vertInflate

    right += horiInflate
    bottom += vertInflate
}

fun RectF.inflate(l: Float, t: Float, r: Float, b: Float) {
    left -= l
    top -= t
    right += r
    bottom += b
}

fun RectF.toRect() : Rect {
    val rect = Rect()
    round(rect)
    return rect
}


// -------------------------------------------------------------------------------------------------

fun calculateFontSize(textBounds: Rect, textContainer: Rect, text: String): Float {
    val textPaint = Paint()
    var stage = 1
    var textSize = 0.0f
    if (text.isNotEmpty()) {
        while (stage < 3) {
            if (stage == 1) textSize += 10f else if (stage == 2) textSize -= 1f
            textPaint.textSize = textSize
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            textBounds.offsetTo(textContainer.left, textContainer.top)
            val fits: Boolean = textContainer.contains(textBounds)
            if (((stage == 1) && !fits) || ((stage == 2) && fits)) {
                stage++
            }
        }
    }
    return textSize
}


const val ALIGN_HORIZON_LEFT = 0
const val ALIGN_HORIZON_MIDDLE = 1
const val ALIGN_HORIZON_RIGHT = 2

const val ALIGN_VERTICAL_TOP = 4
const val ALIGN_HORIZON_CENTER = 8
const val ALIGN_VERTICAL_BOTTOM = 16


fun Canvas.drawText(text: String, rect: RectF, horizonAlign: Int, verticalAlign: Int, paint: Paint) {
    val textBounds = Rect()
    paint.getTextBounds(text, 0, text.length, textBounds)

    val cx = when (horizonAlign) {
        ALIGN_HORIZON_LEFT -> rect.left
        ALIGN_HORIZON_MIDDLE -> rect.left + (rect.width() - textBounds.width().toFloat()) / 2
        ALIGN_HORIZON_RIGHT -> rect.right - textBounds.width().toFloat()
        else -> rect.left + (rect.width() - textBounds.width().toFloat()) / 2
    }

    val cy = when (verticalAlign) {
        ALIGN_VERTICAL_TOP -> rect.top + textBounds.height()
        ALIGN_HORIZON_CENTER -> rect.centerY() + textBounds.height() / 2
        ALIGN_VERTICAL_BOTTOM -> rect.bottom - textBounds.height()
        else -> rect.centerY() + textBounds.height() / 2
    }

    this.drawText(text, cx, cy, paint)
}


fun distanceOf(pos1: PointF, pos2: PointF): Float {
    val dx = pos1.x - pos2.x
    val dy = pos1.y - pos2.y
    return sqrt(dx * dx + dy * dy)
}


fun closestGraphItem(refPos: PointF, graphItems: List< GraphItem >) : GraphItem? {
    var selDist = 0.0f
    var selItem : GraphItem? = null
    for (item in graphItems) {
        val itemDist = distanceOf(refPos, item.boundRect().centerPoint())
        if ((selItem == null) || (itemDist < selDist)) {
            selItem = item
            selDist = itemDist
        }
    }
    return selItem
}


