package glcore


object GlLog {

    // info
    fun i(vararg text: String, ) {
        println(text.joinToString(" "))
    }

    // error
    fun e(vararg text: String, ) {
        println(text.joinToString(" "))
    }

    // critical
    fun c(vararg text: String, ) {
        println(text.joinToString(" "))
    }
}
