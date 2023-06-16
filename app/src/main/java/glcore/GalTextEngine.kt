package glcore

import kotlinx.coroutines.yield
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


data class MarkBlock(
    var markStartPos: Int,
    var markEndPos: Int,
    var markText: String,
    var marks: List< MarkData > = mutableListOf()
)


sealed class MarkDataValue
data class MarkDataString(val value: String) : MarkDataValue()
data class MarkDataEmpty(val value: String = "") : MarkDataValue()
data class MarkDataList(val value: List<String>) : MarkDataValue()
data class MarkDataDict(val value: Map<String, String>) : MarkDataValue()

data class MarkData(
    var markBlock: MarkBlock,
    var markName: String,
    var markData: MarkDataValue
)


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
                labelData.startsWith("{") && labelData.endsWith("}") -> {
                    try {
                        val json = JSONObject(labelData)
                        val map = mutableMapOf<String, String>()
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            map[key] = json.get(key).toString()
                        }
                        MarkDataDict(map)
                    } catch (e: JSONException) {
                        MarkDataString(labelData)
                    }
                }
                labelData.startsWith("[") && labelData.endsWith("]") -> {
                    try {
                        val json = JSONArray(labelData)
                        val list = mutableListOf<String>()
                        for (i in 0 until json.length()) {
                            list.add(json.get(i).toString())
                        }
                        MarkDataList(list)
                    } catch (e: JSONException) {
                        MarkDataString(labelData)
                    }
                }
                else -> MarkDataString(labelData)
            }
            marksInBlock.add(MarkData(block, label, markDataValue))
        }
        result += marksInBlock
        block.marks = marksInBlock
    }
    return result
}


open class GalTextHandler {

    fun onBlockHit(markBlock: MarkBlock) {

    }

    fun onMarkHit(markData: MarkData) {

    }

    // --------------------------------------------------------

    fun onMarkJump(jumpLabel: String) {

    }

    fun onMarkEnd(blockPos: Int) {

    }

    fun onMarkAction(actionData: MarkData) {

    }

    fun onMarkSelection(selectionData: MarkDataDict) {

    }

    fun onMarkCustomize(jumpLabel: String) {

    }

    fun onMarkUnknown(markData: MarkData) {

    }
}


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

    private var markBlocks = mutableListOf< MarkBlock >()
    private var markData = mutableListOf< MarkData >()
    private var galText: String = ""

    var galTextHandler = GalTextHandler()
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
        return ""
    }

    suspend fun yieldText() = sequence {
        val markBlock = markBlockFromPos(galTextPosition)
        markBlock?.run {
            // Assign the tp first. Because tp can be updated in GalTextHandler
            galTextPosition = markBlock.markEndPos + 1

            galTextHandler.onBlockHit(this)
            for (mark in markBlock.marks) {
                galTextHandler.onMarkHit(mark)
            }
        }
        if (galTextPosition >= 0) {
            yield(galText[galTextPosition])
            galTextPosition += 1
        } else {
            yield("")
        }
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