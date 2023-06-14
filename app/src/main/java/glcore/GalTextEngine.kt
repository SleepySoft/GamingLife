package glcore

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


data class MarkBlock(
    var markStartPos: Int,
    var markEndPos: Int,
    var markText: String
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


fun parseMarksFromBlock(markBlocks: List<MarkBlock>): List<MarkData> {
    val result = mutableListOf<MarkData>()
    for (block in markBlocks) {
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
                            map[key] = json.getString(key)
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
                            list.add(json.getString(i))
                        }
                        MarkDataList(list)
                    } catch (e: JSONException) {
                        MarkDataString(labelData)
                    }
                }
                else -> MarkDataString(labelData)
            }
            result.add(MarkData(block, label, markDataValue))
        }
    }
    return result
}


class GalTextEngine {
}