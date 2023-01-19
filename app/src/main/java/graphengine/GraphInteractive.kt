package graphengine

import android.graphics.PointF


open class GraphInteractiveListener {

    open fun onItemClicked(item: GraphItem) {

    }
    open fun onItemDropped(item: GraphItem, intersectItems: List< GraphItem >, pos: PointF) {

    }
    open fun onItemDragging(item: GraphItem, intersectItems: List< GraphItem >, pos: PointF) {

    }
    open fun onItemSelected(item: GraphItem, pos: PointF) {

    }
    open fun onItemTriggered(item: GraphItem) {

    }
}