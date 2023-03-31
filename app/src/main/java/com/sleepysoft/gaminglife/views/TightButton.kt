package com.sleepysoft.gaminglife.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton


class TightButton : AppCompatButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val textBounds = Rect()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        paint.getTextBounds(text.toString(), 0, text.length, textBounds)
        var width = textBounds.width() + paddingLeft + paddingRight
        var height = textBounds.height() + paddingTop + paddingBottom

        // Add extra padding to ensure the text is fully visible
        width += 10
        height += 10

        setMeasuredDimension(width, height)
    }
}
