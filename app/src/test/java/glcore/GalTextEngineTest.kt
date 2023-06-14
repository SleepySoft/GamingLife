package glcore

import org.junit.Test


fun checkMarkText(text: String, markBlock: MarkBlock, expect: String) : Boolean {
    val textWithWrapper = text.substring(markBlock.markStartPos, markBlock.markEndPos + 1).trim()
    val textInWrapper = textWithWrapper.removePrefix("<!--").removeSuffix("-->").trim()
    return (textInWrapper == markBlock.markText) && (textInWrapper == expect)
}


internal class GalTextEngineTest {

    @Test
    fun testExtractMarksFromText1() {
        var text = "This is a <!--comment--> with one mark"
        var marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(checkMarkText(text, marks[0], "comment"))

        text = "This is a <!--comment--> with <!--multiple--> marks"
        marks = extractMarksFromText(text)
        assert(marks.size == 2)
        assert(checkMarkText(text, marks[0], "comment"))
        assert(checkMarkText(text, marks[1], "multiple"))

        text = "This is a <!--nested <!--comment-->--> mark"
        marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(checkMarkText(text, marks[0], "nested <!--comment"))

        text = "This is a <!--comment with no end mark"
        marks = extractMarksFromText(text)
        assert(marks.isEmpty())

        text = "This is a comment with no marks"
        marks = extractMarksFromText(text)
        assert(marks.isEmpty())
    }

    @Test
    fun testExtractMarksFromText2() {
        var text = "<!--comment--> at the beginning"
        var marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(checkMarkText(text, marks[0], "comment"))

        text = "at the end <!--comment-->"
        marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(checkMarkText(text, marks[0], "comment"))

        text = "<!--multiple--><!--marks-->"
        marks = extractMarksFromText(text)
        assert(marks.size == 2)
        assert(checkMarkText(text, marks[0], "multiple"))
        assert(checkMarkText(text, marks[1], "marks"))

        text = "<!--   -->"
        marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(checkMarkText(text, marks[0], ""))
    }

    @Test
    fun testExtractMarksFromText3() {
        var text = "<!--comment--> with spaces <!-- around --> the marks"
        var marks = extractMarksFromText(text)
        assert(marks.size == 2)
        assert(checkMarkText(text, marks[0], "comment"))
        assert(checkMarkText(text, marks[1], "around"))
    }

    @Test
    fun testExtractMarksFromText4() {
        val text = """
        This is a <!--comment
        that spans
        multiple lines--> with one mark
    """.trimIndent()
        val marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(checkMarkText(text, marks[0], "comment\nthat spans\nmultiple lines"))
    }

    @Test
    fun testExtractMarksFromText5() {
        val text = "This is a <!--comment--> with <!--multiple-->\n<!--marks--> on multiple lines"
        val marks = extractMarksFromText(text)
        assert(marks.size == 3)
        assert(checkMarkText(text, marks[0], "comment"))
        assert(checkMarkText(text, marks[1], "multiple"))
        assert(checkMarkText(text, marks[2], "marks"))
    }

    @Test
    fun testExtractMarksFromText6() {
        val text = "This is a <!--comment--> with <!--multiple-->\n<!--marks--> on multiple lines"
        val marks = extractMarksFromText(text)
        assert(marks.size == 3)
        assert(checkMarkText(text, marks[0], "comment"))
        assert(checkMarkText(text, marks[1], "multiple"))
        assert(checkMarkText(text, marks[2], "marks"))
    }

    @Test
    fun testExtractMarksFromText7() {
        val text = "This is a <!--comment with\nnew line--> and <!--multiple\ttabs--> and <!--  spaces  -->"
        val marks = extractMarksFromText(text)
        assert(marks.size == 3)
        assert(checkMarkText(text, marks[0], "comment with\nnew line"))
        assert(checkMarkText(text, marks[1], "multiple\ttabs"))
        assert(checkMarkText(text, marks[2], "spaces"))
    }

    @Test
    fun testParseMarksFromBlock1() {
        val markBlocks = listOf(
            MarkBlock(0, 10, "label: data")
        )
        val result = parseMarksFromBlock(markBlocks)
        assert(result.size == 1)
        val markData = result[0]
        assert(markData.markBlock == markBlocks[0])
        assert(markData.markName == "label")
        assert(markData.markData is MarkDataString)
        assert((markData.markData as MarkDataString).value == "data")
    }

    @Test
    fun testParseMarksFromBlock2() {
        val markBlocks = listOf(
            MarkBlock(0, 10, "label1: data1\nlabel2: data2")
        )
        val result = parseMarksFromBlock(markBlocks)
        assert(result.size == 2)
        val markData1 = result[0]
        assert(markData1.markBlock == markBlocks[0])
        assert(markData1.markName == "label1")
        assert(markData1.markData is MarkDataString)
        assert((markData1.markData as MarkDataString).value == "data1")
        val markData2 = result[1]
        assert(markData2.markBlock == markBlocks[0])
        assert(markData2.markName == "label2")
        assert(markData2.markData is MarkDataString)
        assert((markData2.markData as MarkDataString).value == "data2")
    }

    @Test
    fun testParseMarksFromBlock3() {
        val markBlocks = listOf(
            MarkBlock(0, 10, "label1: data1\nlabel2:\nlabel3: {}\nlabel4: []")
        )
        val result = parseMarksFromBlock(markBlocks)
        assert(result.size == 4)
        val markData1 = result[0]
        assert(markData1.markBlock == markBlocks[0])
        assert(markData1.markName == "label1")
        assert(markData1.markData is MarkDataString)
        assert((markData1.markData as MarkDataString).value == "data1")
        val markData2 = result[1]
        assert(markData2.markBlock == markBlocks[0])
        assert(markData2.markName == "label2")
        assert(markData2.markData is MarkDataEmpty)
        val markData3 = result[2]
        assert(markData3.markBlock == markBlocks[0])
        assert(markData3.markName == "label3")
        assert(markData3.markData is MarkDataDict)
        assert((markData3.markData as MarkDataDict).value.isEmpty())
        val markData4 = result[3]
        assert(markData4.markBlock == markBlocks[0])
        assert(markData4.markName == "label4")
        assert(markData4.markData is MarkDataList)
        assert((markData4.markData as MarkDataList).value.isEmpty())
    }

    @Test
    fun testParseMarksFromBlock4() {
        val markBlocks = listOf(
            MarkBlock(0, 10, "label1\nlabel2:\nlabel3")
        )
        val result = parseMarksFromBlock(markBlocks)
        assert(result.size == 3)
        val markData1 = result[0]
        assert(markData1.markBlock == markBlocks[0])
        assert(markData1.markName == "label1")
        assert(markData1.markData is MarkDataEmpty)
        val markData2 = result[1]
        assert(markData2.markBlock == markBlocks[0])
        assert(markData2.markName == "label2")
        assert(markData2.markData is MarkDataEmpty)
        val markData3 = result[2]
        assert(markData3.markBlock == markBlocks[0])
        assert(markData3.markName == "label3")
        assert(markData3.markData is MarkDataEmpty)
    }

    @Test
    fun testParseMarksFromBlock5() {
        val markBlocks = listOf(
            MarkBlock(0, 10, "label1: {\"key1\": \"value1\", \"key2\": \"value2\"}")
        )
        val result = parseMarksFromBlock(markBlocks)
        assert(result.size == 1)
        val markData = result[0]
        assert(markData.markBlock == markBlocks[0])
        assert(markData.markName == "label1")
        assert(markData.markData is MarkDataDict)
        val dict = (markData.markData as MarkDataDict).value
        assert(dict.size == 2)
        assert(dict["key1"] == "value1")
        assert(dict["key2"] == "value2")
    }

    @Test
    fun testParseMarksFromBlock6() {
        val markBlocks = listOf(
            MarkBlock(0, 10, "label1: {\"key1\": 123, \"key2\": true}")
        )
        val result = parseMarksFromBlock(markBlocks)
        assert(result.size == 1)
        val markData = result[0]
        assert(markData.markBlock == markBlocks[0])
        assert(markData.markName == "label1")
        assert(markData.markData is MarkDataDict)
        val dict = (markData.markData as MarkDataDict).value
        assert(dict.size == 2)
        assert(dict["key1"] == "123")
        assert(dict["key2"] == "true")
    }

    @Test
    fun testParseMarksFromBlock7() {
        val markBlocks = listOf(
            MarkBlock(0, 10, "label1: [123, true, \"string\"]")
        )
        val result = parseMarksFromBlock(markBlocks)
        assert(result.size == 1)
        val markData = result[0]
        assert(markData.markBlock == markBlocks[0])
        assert(markData.markName == "label1")
        assert(markData.markData is MarkDataList)
        val list = (markData.markData as MarkDataList).value
        assert(list.size == 3)
        assert(list[0] == "123")
        assert(list[1] == "true")
        assert(list[2] == "string")
    }

    @Test
    fun testParseMarksFromBlock8() {
        val markBlocks = listOf(
            MarkBlock(0, 10, "label1: {\"key1\": 123\nlabel2: [123, true\nlabel3: invalid")
        )
        val result = parseMarksFromBlock(markBlocks)
        assert(result.size == 3)
        val markData1 = result[0]
        assert(markData1.markBlock == markBlocks[0])
        assert(markData1.markName == "label1")
        assert(markData1.markData is MarkDataString)
        assert((markData1.markData as MarkDataString).value == "{\"key1\": 123")
        val markData2 = result[1]
        assert(markData2.markBlock == markBlocks[0])
        assert(markData2.markName == "label2")
        assert(markData2.markData is MarkDataString)
        assert((markData2.markData as MarkDataString).value == "[123, true")
        val markData3 = result[2]
        assert(markData3.markBlock == markBlocks[0])
        assert(markData3.markName == "label3")
        assert(markData3.markData is MarkDataString)
        assert((markData3.markData as MarkDataString).value == "invalid")
    }

    @Test
    fun testParseMarksFromBlock9() {
        val markBlocks = listOf(
            MarkBlock(0, 10, "label1: {invalid}\nlabel2: [invalid]")
        )
        val result = parseMarksFromBlock(markBlocks)
        assert(result.size == 2)
        val markData1 = result[0]
        assert(markData1.markBlock == markBlocks[0])
        assert(markData1.markName == "label1")
        assert(markData1.markData is MarkDataString)
        assert((markData1.markData as MarkDataString).value == "{invalid}")
        val markData2 = result[1]
        assert(markData2.markBlock == markBlocks[0])
        assert(markData2.markName == "label2")
        assert(markData2.markData is MarkDataString)
        assert((markData2.markData as MarkDataString).value == "[invalid]")
    }
}