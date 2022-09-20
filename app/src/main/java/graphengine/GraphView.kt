package graphengine
import android.annotation.SuppressLint
import android.view.View
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.Log
import android.graphics.*


const val DEBUG_TAG = "DefaultDbg"


interface GraphViewObserver {
    fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

    }

    fun onItemPicked(pickedItem: GraphItem) {

    }

    fun onItemDropped(droppedItem: GraphItem) {

    }

    fun onItemClicked(clickedItem: GraphItem) {

    }

    fun onItemDragging(draggingItem: GraphItem, pos: PointF) {

    }

    fun onItemDropIntersecting(droppedItem: GraphItem, intersectingItems: List< GraphItem >) {

    }
}


open class GraphView(context: Context) : View(context) {

    private var mGraphItems: MutableList< GraphItem > = mutableListOf()

    var paintArea: RectF = RectF()
        private set
    
    var unitScale: Float = 1.0f
        private set

    var fontPaint = Paint(ANTI_ALIAS_FLAG).apply {
        this.setARGB(0xFF, 0x00, 0x00, 0x00)
        this.textAlign = Paint.Align.CENTER
    }
        set(value) {
            field = value
            updateItemProperty()
        }

    var shapePaint = Paint(ANTI_ALIAS_FLAG).apply {
        this.setARGB(0xFF, 0x00, 0x00, 0x00)
        this.style = Paint.Style.STROKE
        this.strokeWidth = unitScale * 0.5f
    }
        set(value) {
            field = value
            updateItemProperty()
        }

    // -------------------------------------------------------------------------

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.run {
            renderItems(this)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        unitScale = w * 0.01f
        paintArea.set(0.0f, 0.0f, w.toFloat(), h.toFloat())

        shapePaint.strokeWidth = unitScale * 1.0f

        updateItemProperty()
        layoutItems()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.actionMasked ?: 0xFF) {

            MotionEvent.ACTION_DOWN -> {
                Log.d(DEBUG_TAG, "Action was DOWN")
                true
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(DEBUG_TAG, "Action was MOVE")
                true
            }
            MotionEvent.ACTION_UP -> {
                Log.d(DEBUG_TAG, "Action was UP")
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.d(DEBUG_TAG, "Action was CANCEL")
                true
            }
            MotionEvent.ACTION_OUTSIDE -> {
                Log.d(DEBUG_TAG, "Movement occurred outside bounds of current screen element")
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    // -------------------------------------------------------------------------

    open fun layoutItems() {

    }

    // -------------------------------------------------------------------------

    fun isPortrait(): Boolean {
        return paintArea.height() >= paintArea.width()
    }

    fun addGraphItem(item: GraphItem) {
        mGraphItems.add(item)
    }

    // -------------------------------------------------------------------------

    private fun renderItems(canvas: Canvas) {
        for (item in mGraphItems) {
            item.render(canvas)
        }
    }

    private fun updateItemProperty() {
        for (item in mGraphItems) {
            item.fontPaint = fontPaint
            item.fontPaint = shapePaint
            item.unitScale = unitScale
        }
    }
}

