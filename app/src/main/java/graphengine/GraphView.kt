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

/*    fun onItemDropIntersecting(droppedItem: GraphItem, intersectingItems: List< GraphItem >) {

    }*/

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

    private var mObserverStack: MutableList< GraphViewObserver > = mutableListOf()
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

        // Update Layer cover area
        mLayers.map { it.coverArea.set(0.0f, 0.0f, w.toFloat(), h.toFloat()) }

        layoutItems()

        topObserver()?.onViewSizeChanged(w, h, oldw, oldh)
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

/*        mSelItem?.apply {
            val itemBound = this.boundRect()
            val insectItems = itemsFromLayer() {
                it.boundRect().intersect(itemBound) && it.interactive}

            if (insectItems.size > 0) {
                topObserver()?.onItemDropIntersecting(this, insectItems)
            }

            // Reset graph item offset and repaint
            this.offsetPixel = PointF(0.0f, 0.0f)
        }*/

        // Clear select item and notify observer
        mSelItem?.run {
            this@GraphView.invalidate()
            this.offsetPixel = PointF(0.0f, 0.0f)
            topObserver()?.onItemDropped(this)
        }

        mSelItem = null
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
            mSelItem = selItem[0]
            Log.i(DEBUG_TAG, "Adapted item: $mSelItem")
            topObserver()?.onItemPicked(selItem[0])
        }
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.i(DEBUG_TAG, "onSingleTapUp")

        val selItem = itemsFromLayer() {
            it.boundRect().contains(e.x, e.y) && it.visible && it.interactive}
        if (selItem.isNotEmpty()) {
            topObserver()?.onItemClicked(selItem[0])
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
            topObserver()?.onItemDragging(this, pos)
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

    fun pickLayer(filter: (input: GraphLayer) -> Boolean): MutableList<GraphLayer> {
        val layers: MutableList< GraphLayer > = mutableListOf()
        for (layer in mLayers) {
            if (filter(layer)) {
                layers.add(layer)
            }
        }
        return layers
    }

    fun bringLayerToFront(layer: GraphLayer) {
        if (layer in mLayers) {
            mLayers.remove(layer)
            mLayers.add(0, layer)
        }
    }

    fun pushObserver(observer: GraphViewObserver) {
        mObserverStack.add(observer)
    }

    fun popObserver() : GraphViewObserver? {
        var poppedLayer: GraphViewObserver? = null
        if (mObserverStack.isNotEmpty()) {
            poppedLayer = mObserverStack[mObserverStack.size - 1]
            mObserverStack.removeAt(mObserverStack.size - 1)
        }
        return poppedLayer
    }

    fun isPortrait(): Boolean {
        return paintArea.height() >= paintArea.width()
    }

    fun specifySelItem(item: GraphItem) {
        mSelItem = item
    }

    // ------------------------------------- Private functions -------------------------------------

    private fun layoutItems() {
        topObserver()?.onItemLayout()
    }
    
    private fun topObserver() : GraphViewObserver? {
        return if (mObserverStack.isNotEmpty()) mObserverStack[mObserverStack.size - 1] else null
    }

    private fun itemsFromLayer(filter: (input: GraphItem) -> Boolean): MutableList< GraphItem > {
        val items = mutableListOf< GraphItem >()
        for (layer in mLayers) {
            if (layer.visible) {
                items.addAll(layer.pickGraphItems(filter))
            }
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

