package glcore


class GlContext {

}


open class GlObject {

    var uuid: String = randomUUID()
        private set

    fun serialize(): String {
        return ""
    }
    fun deserialize(data: String) {

    }
}


class GlPair(first: Any, second: Any): GlObject() {

}


