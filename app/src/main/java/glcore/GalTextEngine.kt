package glcore


data class MarkBlock(
    var markStartPos: Int,
    var markEndPos: Int,
    var markText: String
)


sealed class MarkDataValue
data class MarkDataString(val value: String) : MarkDataValue()
data class MarkDataList(val value: List<String>) : MarkDataValue()
data class MarkDataDict(val value: Map<String, String>) : MarkDataValue()

data class MarkData(
    var belongsMarkBlock: MarkBlock,
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


class GalTextEngine {
}