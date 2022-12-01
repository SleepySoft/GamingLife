package graphengine
import android.annotation.SuppressLint
import android.graphics.*
import android.view.MotionEvent
import com.sleepysoft.gaminglife.controllers.GlControllerContext


const val DEBUG_TAG = "DefaultDbg"


interface GraphViewObserver {
    fun onItemLayout()

    fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
}


interface ActionHandler {
    enum class ACT {
        IGNORED,
        HANDLED
    }

    fun onActionUp(pos: PointF) : ACT

    fun onActionDown(pos: PointF) : ACT

    fun onActionMove(posBefore: PointF, posNow: PointF, distanceX: Float,distanceY: Float) : ACT

    fun onActionFling(posBefore: PointF, posNow: PointF, velocityX: Float, velocityY: Float) : ACT

    fun onActionClick(pos: PointF) : ACT

    fun onActionSelect(pos: PointF) : ACT

    fun onActionLongPress(pos: PointF) : ACT
}


class GraphShadowView() {

    private var mIsLongPressed = false
    private var mLayers: MutableList< GraphLayer > = mutableListOf()

    var paintArea: RectF = RectF()
        private set
    
    var unitScale: Float = 1.0f
        private set

    val mGraphViewObserver = mutableListOf< GraphViewObserver >()

    // ------------------------------------------------------------------------------------

    fun invalidate() {
        GlControllerContext.view.get()?.invalidate()
    }

    // ------------------------------- Window event handler -------------------------------

    fun onDraw(canvas: Canvas?) {
        canvas?.run {
            for (item in mLayers.reversed()) {
                if (item.visible) {
                    item.render(this)
                }
            }
        }
    }

    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        unitScale = w * 0.01f
        paintArea.set(0.0f, 0.0f, w.toFloat(), h.toFloat())

        // Update Layer cover area
        mLayers.forEach { it.coverArea.set(0.0f, 0.0f, w.toFloat(), h.toFloat()) }

        mGraphViewObserver.forEach { it.onViewSizeChanged(w, h, oldw, oldh) }

        layoutItems()
    }

    // --------------------------------- Action & Gesture Handler ----------------------------------

/*    fun onTouchEvent(event: MotionEvent?): Boolean {
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
    }*/

    fun onUp(e: MotionEvent) {
        invokeActionHandler(visibleItems()) {
            it.onActionUp(PointF(e.x, e.y))
        }

/*        forTopLayerItem {
            it.onActionUp(PointF(e.x, e.y))
        }*/

        // Clear select item and notify observer
/*        mSelItem?.run {
            topObserver()?.onItemDropped(this)
            this.offsetPixel = PointF(0.0f, 0.0f)
            this@GraphView.invalidate()
        }

        mSelItem = null*/
    }

    fun onDown(e: MotionEvent): Boolean {
        invokeActionHandler(visibleItemFromPoint(e.x, e.y)) {
            it.onActionDown(PointF(e.x, e.y))
        }
/*        forTopLayerItemActionHandler {
            it.onActionDown(PointF(e.x, e.y))
        }*/
        return true
    }

    fun onShowPress(e: MotionEvent) {
        invokeActionHandler(visibleItemFromPoint(e.x, e.y)) {
            it.onActionSelect(PointF(e.x, e.y))
        }
/*        forTopLayerItemActionHandler {
            it.onActionSelect(PointF(e.x, e.y))
        }*/

/*        val selItem = itemsFromLayer() {
            it.boundRect().contains(e.x, e.y) && it.visible && it.interactive}
        if (selItem.isNotEmpty()) {
            mSelItem = selItem[0]
            // Log.i(DEBUG_TAG, "Adapted item: $mSelItem")
            topObserver()?.onItemPicked(selItem[0])
        }*/
    }

    fun onSingleTapUp(e: MotionEvent): Boolean {
        invokeActionHandler(visibleItemFromPoint(e.x, e.y)) {
            it.onActionClick(PointF(e.x, e.y))
        }
/*        forTopLayerItemActionHandler {
            it.onActionClick(PointF(e.x, e.y))
        }*/

/*        val selItem = itemsFromLayer() {
            it.boundRect().contains(e.x, e.y) && it.visible && it.interactive}
        if (selItem.isNotEmpty()) {
            topObserver()?.onItemClicked(selItem[0])
        }*/

        return true
    }

    fun onScroll(e1: MotionEvent, e2: MotionEvent,
                          distanceX: Float,distanceY: Float): Boolean {
        invokeActionHandler(visibleItems()) {
            it.onActionMove(
                PointF(e1.x, e1.y), PointF(e2.x, e2.y), distanceX, distanceY)
        }

/*        forTopLayerItemActionHandler {
            it.onActionMove(
                PointF(e1.x, e1.y), PointF(e2.x, e2.y), distanceX, distanceY)
        }*/

/*        mSelItem?.run {
            this.shiftItem(-distanceX, -distanceY)
            this@GraphView.invalidate()

            val pos = PointF(
                this.boundRect().centerX(),
                this.boundRect().centerY())
            topObserver()?.onItemDragging(this, pos)
        }*/

        return true
    }

    fun onLongPress(e: MotionEvent) {
        invokeActionHandler(visibleItemFromPoint(e.x, e.y)) {
            it.onActionLongPress(PointF(e.x, e.y))
        }
/*        forTopLayerItemActionHandler {
            it.onActionLongPress(PointF(e.x, e.y))
        }*/
        // https://stackoverflow.com/a/56545079
        mIsLongPressed = true
    }

    fun onFling(e1: MotionEvent, e2: MotionEvent,
                         velocityX: Float, velocityY: Float): Boolean {
        invokeActionHandler(visibleItems()) {
            it.onActionFling(PointF(e1.x, e1.y), PointF(e2.x, e2.y), velocityX, velocityY)
        }
/*        forTopLayerItemActionHandler {
            it.onActionFling(PointF(e1.x, e1.y), PointF(e2.x, e2.y), velocityX, velocityY)
        }*/
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

/*    fun pushObserver(observer: GraphViewObserver) {
        mObserverStack.add(observer)
    }

    fun popObserver() : GraphViewObserver? {
        var poppedLayer: GraphViewObserver? = null
        if (mObserverStack.isNotEmpty()) {
            poppedLayer = mObserverStack[mObserverStack.size - 1]
            mObserverStack.removeAt(mObserverStack.size - 1)
        }
        return poppedLayer
    }*/

    fun isPortrait(): Boolean {
        return paintArea.height() >= paintArea.width()
    }

/*    fun specifySelItem(item: GraphItem) {
        mSelItem = item
    }*/

    fun forEachItem(action: (GraphItem) -> Unit) {
        mLayers.forEach { layer ->
            layer.graphItems.forEach {item ->
                action(item)
            }
        }
    }

    fun visibleItems() = itemsFromVisibleLayers {it.visible }

    fun visibleItemFromPoint(x: Float, y: Float) =
        itemsFromVisibleLayers {it.visible && it.boundRect().contains(x, y) }


/*    fun forTopLayerItem(action: (GraphItem) -> Unit) {
        for (layer in mLayers) {
            layer.graphItems.forEach {item ->
                action(item)
            }
            break
        }
    }*/

    // ------------------------------------- Private functions -------------------------------------

    private fun layoutItems() {
        mGraphViewObserver.forEach { it.onItemLayout() }
    }

    private fun itemsFromVisibleLayers(
        filter: (input: GraphItem) -> Boolean): MutableList< GraphItem > {
        val items = mutableListOf< GraphItem >()
        for (layer in mLayers) {
            if (layer.visible) {
                items.addAll(layer.pickGraphItems(filter))
            }
        }
        return items
    }

/*    fun forAllItemActionHandler(
        action: (GraphActionDecorator) -> ActionHandler.ACT) {

        val adaptItems = itemsFromVisibleLayers {
            it.visible
        }
        invokeActionHandler(adaptItems, action)
    }

    fun forPointAdaptItemActionHandler(
        pos: PointF, action: (GraphActionDecorator) -> ActionHandler.ACT) {

        val adaptItems = itemsFromVisibleLayers {
            it.visible && it.boundRect().contains(pos.x, pos.y)
        }
        invokeActionHandler(adaptItems, action)
    }*/

    private fun invokeActionHandler(
        items: List< GraphItem >,
        action: (GraphActionDecorator) -> ActionHandler.ACT) {

        var skip = false
        items.forEach { item->
            if (!skip) {
                item.graphActionDecorator.forEach {
                    // Not skip actions in the same item
                    if (action(it) == ActionHandler.ACT.HANDLED) {
                        skip = true
                    }
                }
            }
        }
    }

/*    private fun forTopLayerItemActionHandler(action: (GraphActionDecorator) -> ActionHandler.ACT) {
        var skip = false
        forTopLayerItem { item->
            if (!skip) {
                item.graphActionDecorator.forEach {
                    // Not skip actions in the same item
                    if (action(it) == ActionHandler.ACT.HANDLED) {
                        skip = true
                    }
                }
            }
        }
    }*/
}

