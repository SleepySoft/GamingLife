package glcore


data class MarkSub(
    var markStartPos: Int,
    var markEndPos: Int,
    var markText: String
)


fun extractMarksFromText(text: String) : List<MarkSub> {
    val marks = mutableListOf<MarkSub>()
    val pattern = "<!--(.*?)-->".toRegex()
    for (match in pattern.findAll(text)) {
        val markStartPos = match.range.first
        val markEndPos = match.range.last
        val markText = match.groupValues[1]
        marks.add(MarkSub(markStartPos, markEndPos, markText.trim()))
    }
    return marks
}


class GalTextEngine {
}