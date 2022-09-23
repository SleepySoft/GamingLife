package glcore
import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.reflect.typeOf


// https://kotlinlang.org/docs/multiplatform-connect-to-apis.html#generate-a-uuid
fun randomUUID() = UUID.randomUUID().toString()


fun isNullOrEmpty(str: String?): Boolean {
    if (str != null && str.trim().isNotEmpty())
        return false
    return true
}


class PathDict() {
    var mDict = mutableMapOf< String, Any >()

    val keys: Set< String >
        get() = mDict.keys

    val values: Collection< Any >
        get() = mDict.values

    fun isEmpty(): Boolean = mDict.isEmpty()
    fun clear(): Unit = mDict.clear()

    var separator: String = "/"

    fun put(key: String, value: Any): Boolean {
        var ret = false
        val keys = splitKey(key)

        if (keys.isNotEmpty()) {
            val dict = parentDictOf(keys, true)
            dict?.run {
                this.set(keys.last(), value)
                ret = true
            }
        }

        return ret
    }

    fun get(key: String): Any? {
        var ret: Any? = null
        val keys = splitKey(key)

        if (keys.isNotEmpty()) {
            val dict = parentDictOf(keys, true)
            ret = dict?.get(keys.last())
        }

        return ret
    }

    fun remove(key: String) {
        val keys = splitKey(key)
        if (keys.isNotEmpty()) {
            val dict = parentDictOf(keys, true)
            dict?.remove(keys.last())
        }
    }

    // ------------------------------------- Private Functions -------------------------------------

    private fun splitKey(key: String): List< String > {
        return if (key.trim().endsWith(separator)) {
            listOf(key.substring(0, key.length - 1))
        } else {
            val ckeys = key.split(separator).map { it.trim() }
            val keys = ckeys.toMutableList()
            keys.removeAll(listOf(null,""))
            keys
        }
    }

    private fun parentDictOf(keys: List< String >, create: Boolean): MutableMap< String, Any >? {
        val iterator = keys.iterator()
        var returnDict: MutableMap< String, Any >? = null
        var currentDict: MutableMap< String, Any > = mDict

        while (iterator.hasNext()) {
            val k = iterator.next()

            // The last key
            if (!iterator.hasNext()) {
                break
            }

            val v = currentDict[k]
            if (v == null) {
                if (create) {
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



