package glcore
/*import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection*/
import java.util.*


// https://kotlinlang.org/docs/multiplatform-connect-to-apis.html#generate-a-uuid
fun randomUUID() = UUID.randomUUID().toString()


fun isNullOrEmpty(str: String?): Boolean {
    if (str != null && str.trim().isNotEmpty())
        return false
    return true
}


class PathDict(attachMap: MutableMap<String, Any>? = null) {
    var rootDict: MutableMap<String, Any> = attachMap ?: mutableMapOf()
        private set

    var hasUpdate: Boolean = false

    val keys: Set< String >
        get() = rootDict.keys

    val values: Collection< Any >
        get() = rootDict.values

    fun isEmpty(): Boolean = rootDict.isEmpty()
    fun clear(): Unit = rootDict.clear()

    var separator: String = "/"

    fun set(key: String, value: Any, forceWrite: Boolean = false): Boolean =
        put(key, value, forceWrite)

    fun put(key: String, value: Any, forceWrite: Boolean = false): Boolean {
        var ret = false
        val keys = splitKey(key)

        if (keys.isNotEmpty()) {
            val dict = parentDictOf(keys, true)
            dict?.run {
                // If the target place already has a dict saved, forceWrite flag has to be specified
                if ((dict[keys.last()] !is MutableMap<*, *>) || forceWrite) {
                    this.set(keys.last(), value)
                    hasUpdate = true
                    ret = true
                }
            }
        }

        return ret
    }

    fun get(key: String, createIfMissing: Boolean = false): Any? {
        var ret: Any? = null
        val keys = splitKey(key)

        if (keys.isNotEmpty()) {
            val dict = parentDictOf(keys, createIfMissing)
            ret = dict?.get(keys.last())
        }

        return ret
    }

    fun isDictNode(key: String) : Boolean {
        return get(key) is Map< * , * >
    }

    fun getDictAny(key: String): MutableMap<String, Any>? {
        val data = get(key)
        @Suppress("UNCHECKED_CAST")
        return if (dictKeysAreStr(data)) data as MutableMap< String, Any > else null
    }

    fun getDictStr(key: String): MutableMap<String, String>? {
        val data = get(key)
        @Suppress("UNCHECKED_CAST")
        return if (dictKeysAreStr(data) && dictValuesAreStr(data))
            data as MutableMap< String, String > else null
    }

    fun remove(key: String) {
        val keys = splitKey(key)
        if (keys.isNotEmpty()) {
            val dict = parentDictOf(keys, false)
            dict?.remove(keys.last())
            hasUpdate = true
        }
    }

    fun attach(newRootDict: MutableMap< String, Any >) {
        rootDict = newRootDict
        hasUpdate = true
    }

    // ------------------------------------- Private Functions -------------------------------------

    private fun splitKey(key: String): List< String > {
        val ckeys = key.split(separator).map { it.trim() }
        val keys = ckeys.toMutableList()
        keys.removeAll(listOf(null,""))
        return keys
    }

    private fun parentDictOf(keys: List< String >, createPath: Boolean): MutableMap< String, Any >? {
        val iterator = keys.iterator()
        var returnDict: MutableMap< String, Any >? = rootDict
        var currentDict: MutableMap< String, Any > = rootDict

        while (iterator.hasNext()) {
            val k = iterator.next()

            // The last key
            if (!iterator.hasNext()) {
                break
            }

            val v = currentDict[k]
            if (v == null) {
                if (createPath) {
                    val newDict = mutableMapOf< String, Any >()
                    currentDict[k] = newDict
                    currentDict = newDict
                    returnDict = newDict
                }
                else {
                    returnDict = null
                    break
                }
            }
            else if (v is MutableMap<*, *>) {
                @Suppress("UNCHECKED_CAST")
                currentDict = v as MutableMap< String, Any >
                returnDict = currentDict
            }
            else {
                // Has no child level
                returnDict = null
                break
            }
        }

        return returnDict
    }

    fun dictKeysAreStr(dict: Any?): Boolean {
        var ret = false
        if (dict is MutableMap< *, * >) {
            ret = true
            for (k in dict.keys) {
                if (k !is String) {
                    ret = false
                    break
                }
            }
        }
        return ret
    }

    fun dictValuesAreStr(dict: Any?): Boolean {
        var ret = false
        if (dict is MutableMap< *, * >) {
            ret = true
            for (k in dict.values) {
                if (k !is String) {
                    ret = false
                    break
                }
            }
        }
        return ret
    }
}


/*


object SortedMapSerializer: KSerializer<Map<String, Int>> {
    private val mapSerializer = MapSerializer(String.serializer(), Int.serializer())

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Map<String, Int>) {
        mapSerializer.serialize(encoder, value.toSortedMap())
    }

    override fun deserialize(decoder: Decoder): Map<String, Int> {
        return mapSerializer.deserialize(decoder)
    }
}


object PathDictSerializer : KSerializer< PathDict > {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("pathdict")

    override fun serialize(encoder: Encoder, value: PathDict) {
        encoder.encodeCollection()
    }

    override fun deserialize(decoder: Decoder): PathDict {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(decoder.decodeLong()), ZoneOffset.UTC)
    }
}
*/

