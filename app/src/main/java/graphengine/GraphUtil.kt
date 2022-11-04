package graphengine

import android.graphics.*
import kotlin.math.sqrt


fun rectF2Rect(rectF: RectF): Rect {
    val rect = Rect()
    rectF.round(rect)
    return rect
}

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

fun centerFOfRect(rect: Rect) : PointF = PointF(rect.centerX().toFloat(), rect.centerY().toFloat())

fun centerFOfRectF(rectF: RectF) : PointF = PointF(rectF.centerX(), rectF.centerY())

fun inflateRectF(rectF: RectF, inflatePct: Float): RectF {
    val horiInflate: Float = rectF.width() * (inflatePct - 1.0f) / 2.0f
    val vertInflate: Float = rectF.height() * (inflatePct - 1.0f) / 2.0f
    return RectF(rectF.left - horiInflate, rectF.top - vertInflate,
            rectF.right + horiInflate, rectF.bottom + vertInflate)
}


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


fun distanceOf(pos1: PointF, pos2: PointF): Float {
    val dx = pos1.x - pos2.x
    val dy = pos1.y - pos2.y
    return sqrt(dx * dx + dy * dy)
}


fun closestGraphItem(refPos: PointF, graphItems: List< GraphItem >) : GraphItem? {
    var selDist = 0.0f
    var selItem : GraphItem? = null
    for (item in graphItems) {
        val itemDist = distanceOf(refPos, centerFOfRectF(item.boundRect()))
        if ((selItem == null) || (itemDist < selDist)) {
            selItem = item
            selDist = itemDist
        }
    }
    return selItem
}


