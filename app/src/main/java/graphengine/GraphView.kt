package graphengine
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
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

    fun onItemLayout() {

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

    var mObserver: GraphViewObserver? = null
    var mGestureDetector = GestureDetector(context, this)

    // ------------------------------- Window event handler override -------------------------------

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

        mObserver?.onViewSizeChanged(w, h, oldw, oldh)
    }

    // --------------------------------- Action & Gesture Handler ----------------------------------

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_UP -> onUp(event)
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

    private fun onUp(e: MotionEvent) {
        Log.i(DEBUG_TAG, "onUp")
        mSelItem?.apply {
            val itemBound = this.getBoundRect()
            val insectItems = mutableListOf< GraphItem >()

            // Calc dragging intersect
            for (item in mGraphItems) {
                if (item.getBoundRect().intersect(itemBound)) {
                    insectItems.add(item)
                }
            }
            if (insectItems.size > 0) {
                mObserver?.onItemDropIntersecting(this, insectItems)
            }

            // Reset graph item offset and repaint
            this.offsetPixel = PointF(0.0f, 0.0f)
            this@GraphView.invalidate()
        }

        // Clear select item and notify observer
        mSelItem?.run {
            mSelItem = null
            mObserver?.onItemDropped(this)
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        Log.i(DEBUG_TAG, "onDown")
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        Log.i(DEBUG_TAG, "onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.i(DEBUG_TAG, "onSingleTapUp")
        val selItem = itemFromPoint(PointF(e.x, e.y))
        selItem?.run {
            mObserver?.onItemClicked(selItem)
        }
        return true
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent,
                          distanceX: Float,distanceY: Float): Boolean {
        Log.i(DEBUG_TAG, "onScroll - x = $distanceX, y = $distanceY")

        mSelItem?.run {
            this.shiftItem(-distanceX, -distanceY)
            this@GraphView.invalidate()

            val pos = PointF(
                this.getBoundRect().centerX(),
                this.getBoundRect().centerY())
            mObserver?.onItemDragging(this, pos)
        }
        return true
    }

    // https://stackoverflow.com/a/56545079

    override fun onLongPress(e: MotionEvent) {
        mIsLongPressed = true
        Log.i(DEBUG_TAG, "onLongPress - e.x = $e.x, e.y = $e.y")

        mSelItem = itemFromPoint(PointF(e.x, e.y))
        Log.i(DEBUG_TAG, "Adapted item: $mSelItem")

        mSelItem?.run {
            mObserver?.onItemPicked(this)
        }
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent,
                         velocityX: Float, velocityY: Float): Boolean {
        Log.i(DEBUG_TAG, "onFling - x = $velocityX, y = $velocityY")
        return false
    }

    // ------------------------------------- Public functions --------------------------------------

    fun isPortrait(): Boolean {
        return paintArea.height() >= paintArea.width()
    }

    fun addGraphItem(item: GraphItem) {
        mGraphItems.add(item)
    }

    fun setObserver(observer: GraphViewObserver) {
        mObserver = observer
    }

    // ------------------------------------- Private functions -------------------------------------

    private fun renderItems(canvas: Canvas) {
        for (item in mGraphItems) {
            item.render(canvas)
        }
    }

    private fun layoutItems() {
        mObserver?.onItemLayout()
    }

    private fun updateItemProperty() {
        for (item in mGraphItems) {
            item.fontPaint = fontPaint
            item.fontPaint = shapePaint
            item.unitScale = unitScale
        }
    }

    private fun itemFromPoint(pos: PointF): GraphItem? {
        var selItem: GraphItem? = null
        for (item in mGraphItems) {
            if (item.getBoundRect().contains(pos.x, pos.y)) {
                selItem = item
                break
            }
        }
        return selItem
    }
}

