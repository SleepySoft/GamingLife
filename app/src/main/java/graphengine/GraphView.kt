package graphengine
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View


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


open class GraphView(context: Context) :
    View(context), GestureDetector.OnGestureListener {

    private var mIsLongPressed = false
    private var mSelItem: GraphItem? = null;
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

    var mGestureDetector = GestureDetector(context, this)

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
        event ?: return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_UP -> mSelItem = null
        }

        if (mGestureDetector.onTouchEvent(event)) {
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> if (mIsLongPressed) {
                mIsLongPressed = false
                val cancel = MotionEvent.obtain(event)
                cancel.action = MotionEvent.ACTION_CANCEL
                mGestureDetector.onTouchEvent(cancel)
            }
        }
        return super.onTouchEvent(event)
    }

    // -------------------------------------------------------------------------

    override fun onDown(e: MotionEvent): Boolean {
        Log.i("Default", "onDown")
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        Log.i("Default", "onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.i("Default", "onSingleTapUp")
        return true
    }

    override fun onScroll(
        e1: MotionEvent, e2: MotionEvent, distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.i("Default", "onScroll - x = $distanceX, y = $distanceY")

        mSelItem?.run {
            this.shiftItem(-distanceX, -distanceY)
            this@GraphView.invalidate()
        }
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        // https://stackoverflow.com/a/56545079

        Log.i("Default", "onLongPress - e.x = $e.x, e.y = $e.y")

        mSelItem = null
        mIsLongPressed = true

        for (item in mGraphItems) {
            if (item.getBoundRect().contains(e.x, e.y)) {
                mSelItem = item
                Log.i("Default", "Adapted item: $mSelItem")
                break
            }
        }
    }

    override fun onFling(
        e1: MotionEvent, e2: MotionEvent, velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.i("Default", "onFling - x = $velocityX, y = $velocityY")
        return false
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

    private fun itemFromPoint() {

    }

    private fun handleActionDown(event: MotionEvent) {

    }

    private fun handleActionMove(event: MotionEvent) {

    }

    private fun handleActionUp(event: MotionEvent) {

    }
}

