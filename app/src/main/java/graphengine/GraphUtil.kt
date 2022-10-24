package graphengine

import android.graphics.*
import kotlin.math.sqrt


fun rectF2Rect(rectF: RectF): Rect {
    val rect = Rect()
    rectF.round(rect)
    return rect
}

fun centerFOfRect(rect: Rect) : PointF = PointF(rect.centerX().toFloat(), rect.centerY().toFloat())

fun centerFOfRectF(rectF: RectF) : PointF = PointF(rectF.centerX(), rectF.centerY())

fun inflateRectF(rectF: RectF, inflatePct: Float): RectF {
    val horiInflate: Float = rectF.width() * inflatePct / 2.0f
    val vertInflate: Float = rectF.height() * inflatePct / 2.0f
    return RectF(rectF.left + horiInflate, rectF.top + vertInflate, rectF.bottom - vertInflate)
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
        val itemDist = distanceOf(refPos, centerFOfRectF(item.getBoundRect()))
        if ((selItem == null) || (itemDist < selDist)) {
            selItem = item
            selDist = itemDist
        }
    }
    return selItem
}


