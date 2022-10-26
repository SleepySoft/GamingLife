package graphengine
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
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


class GraphView(context: Context) :
    View(context), GestureDetector.OnGestureListener {

    private var mIsLongPressed = false
    private var mSelItem: GraphItem? = null;
    private var mLayers: MutableList< GraphLayer > = mutableListOf()

    var paintArea: RectF = RectF()
        private set
    
    var unitScale: Float = 1.0f
        private set

    private var mObserver: GraphViewObserver? = null
    private var mGestureDetector = GestureDetector(context, this)

    // ------------------------------- Window event handler override -------------------------------

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.run {
            for (item in mLayers.reversed()) {
                if (item.visible) {
                    item.render(this)
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        unitScale = w * 0.01f
        paintArea.set(0.0f, 0.0f, w.toFloat(), h.toFloat())

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
            val itemBound = this.boundRect()
            val insectItems = itemsFromLayer() {it.boundRect().contains(e.x, e.y) &&
                    it.boundRect().intersect(itemBound) && it.interactive}

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

        val selItem = itemsFromLayer() {
            it.boundRect().contains(e.x, e.y) && it.visible && it.interactive}
        if (selItem.isNotEmpty()) {
            Log.i(DEBUG_TAG, "Adapted item: ${selItem[0]}")
            mObserver?.onItemPicked(selItem[0])
        }
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.i(DEBUG_TAG, "onSingleTapUp")

        val selItem = itemsFromLayer() {
            it.boundRect().contains(e.x, e.y) && it.visible && it.interactive}
        if (selItem.isNotEmpty()) {
            mObserver?.onItemClicked(selItem[0])
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
                this.boundRect().centerX(),
                this.boundRect().centerY())
            mObserver?.onItemDragging(this, pos)
        }
        return true
    }

    // https://stackoverflow.com/a/56545079

    override fun onLongPress(e: MotionEvent) {
        mIsLongPressed = true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent,
                         velocityX: Float, velocityY: Float): Boolean {
        // Log.i(DEBUG_TAG, "onFling - x = $velocityX, y = $velocityY")
        return false
    }

    // ------------------------------------- Public functions --------------------------------------

    fun addLayer(newLayer: GraphLayer) : Int {
        mLayers.add(newLayer)
        return mLayers.size - 1
    }

    fun setObserver(observer: GraphViewObserver) {
        mObserver = observer
    }

    fun isPortrait(): Boolean {
        return paintArea.height() >= paintArea.width()
    }

    // ------------------------------------- Private functions -------------------------------------

    private fun layoutItems() {
        mObserver?.onItemLayout()
    }

    private fun itemsFromLayer(filter: (input: GraphItem) -> Boolean): MutableList< GraphItem > {
        val items = mutableListOf< GraphItem >()
        for (layer in mLayers) {
            items.addAll(layer.pickItems(filter))
        }
        return items
    }

/*    private fun itemFromPoint(pos: PointF) : MutableList< GraphItem > {
        val items = mutableListOf< GraphItem >()
        for (item in mGraphItems) {
            if (item.boundRect().contains(pos.x, pos.y)) {
                items.add(item)
            }
        }
        return items
    }

    private fun pickableItemFromPoint(pos: PointF) : GraphItem? {
        var selItem: GraphItem? = null
        val selItems = itemFromPoint(pos)
        for (item in selItems) {
            if (item.interactive) {
                selItem = item
                break
            }
        }
        return selItem
    }*/
}

