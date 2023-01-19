package graphengine

interface GraphContext {
    fun refresh()
    fun toast(text: String)
    fun vibrate(milliseconds: Long)
}