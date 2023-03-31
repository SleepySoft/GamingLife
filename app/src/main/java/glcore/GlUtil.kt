package glcore

import java.io.*
import java.util.*
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * Use ?. operation on boolean expression with this function.
 *
 * @param v The value for judgement。
 * @return Return true if v is true else null
 */

fun trueOrNull(v: Boolean) : Boolean? = if (v) true else null


/**
 * Wrap a block which has no return value to handle its exception。
 *
 * @param silent If true. There's no exception information output。
 * @param block The block to call
 * @return UNIT
 */

@OptIn(ExperimentalContracts::class)
fun ex(silent: Boolean=false, block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    try {
        block()
    } catch (e: Exception) {
        if (!silent) {
            println("ex - Exception: $e")
        }
    } finally {

    }
}


/**
 * Wrap a block which has return value to handle its exception。
 *
 * @param returnOnException The return value if this block gets exception。
 * @param silent If true. There's no exception information output。
 * @param block The block to call
 * @return The block's return value if no exception else returnOnException
 */

@OptIn(ExperimentalContracts::class)
fun < T > exr(returnOnException: T, silent: Boolean=false, block: () -> T) : T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block()
    } catch (e: Exception) {
        if (!silent) {
            println("exr - Exception: $e")
        }
        returnOnException
    } finally {

    }
}


// https://kotlinlang.org/docs/multiplatform-connect-to-apis.html#generate-a-uuid
fun randomUUID() = UUID.randomUUID().toString()


// https://stackoverflow.com/a/52225984
@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHexString() =
    asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }


// https://stackoverflow.com/a/60910036
fun Map< String, Any >.deepCopy() : MutableMap< String, Any > {
    return HashMap< String, Any >(this).mapValues {
        if (it.value is Map<*, *>) (it.value as Map<*, *>).toMutableMap() else it.value
    }.toMutableMap()
}


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
                val v = if (value is Map<*, *>) value.toMutableMap() else value

                // If the target place already has a dict saved, forceWrite flag has to be specified
                if ((dict[keys.last()] !is MutableMap<*, *>) || forceWrite) {
                    this.set(keys.last(), v)
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

    fun getListDictAny(key: String): GlAnyStructList {
        val data = get(key) as List< * >?
        @Suppress("UNCHECKED_CAST")
        return (data ?: mutableListOf< GlAnyStruct >()) as GlAnyStructList
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
            else if (v is Map<*, *>) {
                currentDict[k] = v.toMutableMap()
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


fun savePathDict(fileName: String, pathDict: PathDict, force: Boolean = false) : Boolean {
    var ret = false
    if (pathDict.hasUpdate || force) {
        val fileContent: String = GlJson.serializeAnyDict(pathDict.rootDict)
        ret = GlFile.saveFile(fileName, fileContent.toByteArray(Charsets.UTF_8))
        pathDict.hasUpdate = !ret
    }
    return ret
}


fun loadPathDict(fileName: String, pathDict: PathDict) : Boolean {
    val fileContent: String = GlFile.loadFile(fileName).toString(Charsets.UTF_8)
    pathDict.attach(GlJson.deserializeAnyDict(fileContent))
    pathDict.hasUpdate = false
    return fileContent.isNotEmpty()
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


fun compress(data: ByteArray): ByteArray {
    val baos = ByteArrayOutputStream()
    val out: OutputStream = DeflaterOutputStream(baos)
    out.write(data)
    out.close()
    return baos.toByteArray()
}

fun decompress(data: ByteArray): ByteArray {
    val inputStream: InputStream = InflaterInputStream(ByteArrayInputStream(data))
    val baos = ByteArrayOutputStream()
    val buffer = ByteArray(2048)
    var len: Int
    while (inputStream.read(buffer).also { len = it } > 0) {
        baos.write(buffer, 0, len)
    }
    return baos.toByteArray()
}

