package graphengine

open class GraphInteractiveListener {

    open fun onItemClicked(item: GraphItem) {

    }
    open fun onItemDropped(item: GraphItem, intersectItems: List< GraphItem >) {

    }
    open fun onItemDragging(item: GraphItem, intersectItems: List< GraphItem >) {

    }
    open fun onItemSelected(item: GraphItem) {

    }
    open fun onItemTriggered(item: GraphItem) {

    }
}