package glcore
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

    val keys: Set< String >
        get() = rootDict.keys

    val values: Collection< Any >
        get() = rootDict.values

    fun isEmpty(): Boolean = rootDict.isEmpty()
    fun clear(): Unit = rootDict.clear()

    var separator: String = "/"

    fun put(key: String, value: Any, forceWrite: Boolean = false): Boolean {
        var ret = false
        val keys = splitKey(key)

        if (keys.isNotEmpty()) {
            val dict = parentDictOf(keys, true)
            dict?.run {
                // If the target place already has a dict saved, forceWrite flag has to be specified
                if ((dict[keys.last()] !is MutableMap<*, *>) || forceWrite) {
                    this.set(keys.last(), value)
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

    fun remove(key: String) {
        val keys = splitKey(key)
        if (keys.isNotEmpty()) {
            val dict = parentDictOf(keys, false)
            dict?.remove(keys.last())
        }
    }

    fun attach(newRootDict: MutableMap< String, Any >) {
        rootDict = newRootDict
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
}


