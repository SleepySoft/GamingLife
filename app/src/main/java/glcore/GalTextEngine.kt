package glcore

/*import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.json.JSONException*/

import java.io.StringReader
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import com.google.gson.reflect.TypeToken


// -------------------------------------------------------------------------------------------------

fun parseJson(jsonString: String): Any {
    val gson = Gson()
    val reader = StringReader(jsonString)
    val jsonReader = JsonReader(reader)
    val element: JsonElement

    return try {
        element = gson.fromJson(jsonReader, JsonElement::class.java)
        convertElement(element)
    } catch (e: Exception) {
        jsonString
    }
}

fun convertElement(element: JsonElement): Any {
    return when {
        element.isJsonObject -> {
            val map = LinkedHashMap<String, Any>()
            for ((key, value) in element.asJsonObject.entrySet()) {
                map[key] = convertElement(value)
            }
            map
        }
        element.isJsonArray -> {
            val list = mutableListOf<Any>()
            for (item in element.asJsonArray) {
                list.add(convertElement(item))
            }
            list
        }
        element.isJsonPrimitive && element.asJsonPrimitive.isString -> element.asString
        else -> element.toString()
    }
}


/*fun parseJson(jsonString: String): Any {
    val tokener = JSONTokener(jsonString)
    val value = tokener.nextValue()
    return when (value) {
        is JSONObject -> {
            val map = mutableMapOf<String, Any>()
            val keys = value.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = parseJson(value.get(key).toString())
            }
            map
        }
        is JSONArray -> {
            val list = mutableListOf<Any>()
            for (i in 0 until value.length()) {
                list.add(parseJson(value.get(i).toString()))
            }
            list
        }
        else -> value.toString()
    }
}*/


// -------------------------------------------------------------------------------------------------

data class MarkBlock(
    var markStartPos: Int,
    var markEndPos: Int,
    var markText: String,
    var marks: List< MarkData > = mutableListOf()
)


sealed class MarkDataValue
data class MarkDataString(val value: String) : MarkDataValue()
data class MarkDataEmpty(val value: String = "") : MarkDataValue()
data class MarkDataList(val value: List<Any>) : MarkDataValue()
data class MarkDataDict(val value: LinkedHashMap<String, Any>) : MarkDataValue()

data class MarkData(
    var markBlock: MarkBlock,
    var markName: String,
    var markData: MarkDataValue
)


// -------------------------------------------------------------------------------------------------

fun extractMarksFromText(text: String) : List<MarkBlock> {
    val marks = mutableListOf<MarkBlock>()
    val pattern = "<!--(.*?)-->".toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
    for (match in pattern.findAll(text)) {
        val markStartPos = match.range.first
        val markEndPos = match.range.last
        val markText = match.groupValues[1]
        marks.add(MarkBlock(markStartPos, markEndPos, markText.trim()))
    }
    return marks
}


fun markBlockFromPos(position: Int, markBlockList: List<MarkBlock>): MarkBlock? =
    markBlockList.firstOrNull { it.markStartPos <= position && position <= it.markEndPos }

fun prevMarkBlockOfPos(position: Int, markBlockList: List<MarkBlock>): MarkBlock? =
    markBlockList.lastOrNull { it.markEndPos < position }

fun nextMarkBlockOfPos(position: Int, markBlockList: List<MarkBlock>): MarkBlock? =
    markBlockList.firstOrNull { it.markStartPos > position }


@Suppress("UNCHECKED_CAST")
fun parseMarksFromBlock(markBlocks: List<MarkBlock>): List<MarkData> {
    val result = mutableListOf<MarkData>()
    for (block in markBlocks) {
        val marksInBlock = mutableListOf< MarkData >()
        val lines = block.markText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        for (line in lines) {
            val parts = line.split(":", limit = 2)
            val label = parts[0].trim()
            val labelData = if (parts.size > 1) parts[1].trim() else ""

            val markDataValue = when {
                labelData.isEmpty() -> MarkDataEmpty()
                else -> {
                    when (val value = parseJson(labelData)) {
                        is List<*> -> MarkDataList(value as List<Any>)
                        is LinkedHashMap<*,*> -> MarkDataDict(value as LinkedHashMap<String, Any>)
                        else -> MarkDataString(value.toString())
                    }
                }

/*                labelData.startsWith("{") && labelData.endsWith("}") -> {
                    try {
                        val map = parseJson(labelData) as Map<*, *>
                        MarkDataDict(map as Map< String, Any >)
                    } catch (e: JSONException) {
                        MarkDataString(labelData)
                    }
                }
                labelData.startsWith("[") && labelData.endsWith("]") -> {
                    try {
                        val list = parseJson(labelData) as List< * >
                        MarkDataList(list as List< Any >)
                    } catch (e: JSONException) {
                        MarkDataString(labelData)
                    }
                }
                else -> MarkDataString(labelData)*/
            }
            marksInBlock.add(MarkData(block, label, markDataValue))
        }
        result += marksInBlock
        block.marks = marksInBlock
    }
    return result
}


// -------------------------------------------------------------------------------------------------

open class GalTextHandler(private val gte: GalTextEngine) {

    // ----- We don't have to override these 2 functions in normal case -----

    open fun onBlockHit(markBlock: MarkBlock) {
        for (mark in markBlock.marks) {
            onMarkHit(mark)
        }
    }

    open fun onMarkHit(markData: MarkData) {
        when (markData.markName) {
            GalTextEngine.MARK_LABEL, GalTextEngine.MARK_COMMENTS -> {
                // Do nothing
            }
            GalTextEngine.MARK_JUMP -> {
                if (markData.markData is MarkDataString) {
                    onMarkJump((markData.markData as MarkDataString).value)
                }
            }
            GalTextEngine.MARK_END -> {
                onMarkEnd(markData.markBlock.markStartPos)
            }
            GalTextEngine.MARK_ACTION -> {
                onMarkAction(markData)
            }
            GalTextEngine.MARK_SELECTION -> {
                if (markData.markData is MarkDataDict) {
                    onMarkSelection(markData.markData as MarkDataDict)
                }
            }
            GalTextEngine.MARK_GLOBAL_STATUS -> {
                // TODO: Reserved
            }
            GalTextEngine.MARK_SESSION_STATUS -> {
                // TODO: Reserved
            }
            else -> {
                if (markData.markName.startsWith("@")) {
                    onMarkCustomize(markData)
                } else {
                    onMarkUnknown(markData)
                }
            }
        }
    }

    // ----------------------------------------------------------------------

    open fun onMarkJump(jumpLabel: String) {
        gte.tpJump(jumpLabel)
    }

    open fun onMarkEnd(blockPos: Int) {
        gte.galTextPosition = -1
    }

    open fun onMarkAction(actionData: MarkData) {
        // Override this function to handle action
    }

    open fun onMarkSelection(selectionData: MarkDataDict) : Int {
        // Override this function to handle selection
        return 0
    }

    open fun onMarkCustomize(markData: MarkData) {
        // Override this function to handle customize mark
    }

    // ------------- Avoid overriding for forbidden mark abuse --------------

    private fun onMarkUnknown(markData: MarkData) {
        GlLog.i("[GalTextEngine] Unknown mark: ${markData.markName}")
    }
}


// -------------------------------------------------------------------------------------------------

class GalTextEngine {
    companion object {
        const val MARK_LABEL = "label"
        const val MARK_JUMP = "jump"
        const val MARK_END = "end"
        const val MARK_ACTION = "action"
        const val MARK_SELECTION = "selection"
        const val MARK_GLOBAL_STATUS = "global_status"
        const val MARK_SESSION_STATUS = "session_status"
        const val MARK_COMMENTS = "comments"
    }

    var markBlocks = mutableListOf< MarkBlock >()
        private set
    var markData = mutableListOf< MarkData >()
        private set
    var galText: String = ""
        private set

    var galTextHandler = GalTextHandler(this)
    var markLabelPosition = mutableMapOf< String, Int >()           // { LabelName: Position }

    // ---------------------------------------------------------------------------------------------

    var galTextPosition: Int = 0
        set(value) {
            // TODO: Check and handle tp changing.
            if (value < 0) {
                field = 0
            } else if (value >= galText.length) {
                field = galText.length - 1
            } else {
                field = value
            }
        }

    fun tpJump(label: String) {
        markLabelPosition[label]?.let {
            galTextPosition = it
        }
    }

    fun tpJumpAfterBlock(markBlock: MarkBlock) {
        galTextPosition = markBlock.markEndPos + 1
    }

    // ---------------------------------------------------------------------------------------------

    fun nextChar() : String {
        var markBlock = markBlockFromPos(galTextPosition)

        while (markBlock != null) {

            // Assign the tp first. Because tp can be updated in GalTextHandler
            galTextPosition = markBlock.markEndPos + 1

            galTextHandler.onBlockHit(markBlock)

            markBlock = markBlockFromPos(galTextPosition)
        }

        val ret = if (galTextPosition >= 0) {
            galText[galTextPosition].toString()
        } else {
            ""
        }
        galTextPosition += 1

        return ret
    }

    fun loadText(text: String) {
        galText = text
        markBlocks = extractMarksFromText(text).toMutableList()
        markData = parseMarksFromBlock(markBlocks).toMutableList()
        
        indexLabelMarks()
    }

    fun markBlockFromPos(position: Int): MarkBlock? = markBlockFromPos(position, markBlocks)

    fun prevMarkBlockOfPos(position: Int): MarkBlock? = prevMarkBlockOfPos(position, markBlocks)

    fun nextMarkBlockOfPos(position: Int): MarkBlock? = nextMarkBlockOfPos(position, markBlocks)

    // ---------------------------------------------------------------------------------------------

    private fun indexLabelMarks() {
        markLabelPosition.clear()
        for (mark in markData) {
            if (mark.markName == MARK_LABEL) {
                if (mark.markData is MarkDataString) {
                    val labelName = (mark.markData as MarkDataString).value
                    val labelBlock = mark.markBlock
                    markLabelPosition[labelName] = labelBlock.markEndPos + 1
                }
            }
        }
    }
}
