package Example
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View


// https://sketchingdev.co.uk/blog/resizing-text-to-fit-into-a-container-on-android.html

class ExampleView(context: Context) : View(context) {
    private val textPaint: Paint = Paint().apply {
        this.setTextAlign(Paint.Align.CENTER)
    }
    private var drawableContainer: Rect = Rect()
    private val boundaryOfText: Rect = Rect()
    private val text = "Hello World"

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            drawableContainer.set(0, 0, w, h)
            val fontSize = calculateFontSize(boundaryOfText, drawableContainer, text)
            textPaint.setTextSize(fontSize)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipRect(0, 0, canvas.getWidth(), canvas.getHeight())
        val halfTextHeight: Float = boundaryOfText.height() / 2f
        canvas.drawText(
            text,
            drawableContainer.centerX().toFloat(),
            drawableContainer.centerY().toFloat() + halfTextHeight,
            textPaint
        )
    }

    companion object {
        private fun calculateFontSize(textBounds: Rect, textContainer: Rect, text: String): Float {
            val textPaint = Paint()
            var stage = 1
            var textSize = 0f
            while (stage < 3) {
                if (stage == 1) textSize += 10f else if (stage == 2) textSize -= 1f
                textPaint.setTextSize(textSize)
                textPaint.getTextBounds(text, 0, text.length, textBounds)
                textBounds.offsetTo(textContainer.left, textContainer.top)
                val fits: Boolean = textContainer.contains(textBounds)
                if (stage == 1 && !fits) stage++ else if (stage == 2 && fits) stage++
            }
            return textSize
        }
    }
}