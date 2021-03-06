package glcore


class GlContext {

}

/*


open class GlObject {
    var uuid: String = randomUUID()
        private set

    open fun pack(): Map< String, Any> {
        return mapOf("UUID" to uuid)
    }

    open fun unpack(pack: Map< String, Any>): Boolean {
        uuid = pack["UUID"] as String ?: uuid
        return true
    }

    open fun serialize(): String {
        val dictPack = pack()
        // jsonString variant for debug
        val jsonString= Json.encodeToString(dictPack)
        return jsonString
    }

    @kotlinx.serialization.ExperimentalSerializationApi
    open fun deserialize(jsonString: String): Boolean {
        val dictPack = Json.decodeFromString< Map< String, Any> >(jsonString)
        return unpack(dictPack)
    }
}


class GlPair(var first: Any, var second: Any): GlObject() {

    override fun pack(): Map< String, Any> {
        val dictPack = super.pack().toMutableMap()
        dictPack["first"] = first
        dictPack["second"] = second
        return dictPack
    }

    override fun unpack(pack: Map< String, Any>): Boolean {
        if (super.unpack(pack)) {
            first = pack["first"] ?: first
            first = pack["second"] ?: second
        }
        return true
    }
}

class GlList(var internalList: MutableList< Any > = mutableListOf()): GlObject() {

    override fun pack(): Map< String, Any> {
        val dictPack = super.pack().toMutableMap()
        dictPack["internalList"] = internalList
        return dictPack
    }

    override fun unpack(pack: Map< String, Any>): Boolean {
        var ret = false
        if (super.unpack(pack)) {
            val unpackData = pack["internalList"]
            if (unpackData is MutableList<*>) {
                ret = true
                @Suppress("UNCHECKED_CAST")
                internalList = unpackData as MutableList< Any >
            }
        }
        return ret
    }
}
*/
