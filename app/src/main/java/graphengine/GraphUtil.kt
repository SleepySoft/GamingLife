package graphengine

import android.graphics.*


fun RectF2Rect(rectF: RectF): Rect {
    val rect = Rect()
    rectF.round(rect)
    return rect
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