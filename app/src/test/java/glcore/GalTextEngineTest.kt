package glcore

import org.junit.Test
import org.junit.jupiter.api.Assertions.*


fun checkMarkText(text: String, markSub: MarkSub, expect: String) : Boolean {
    val textWithWrapper = text.substring(markSub.markStartPos, markSub.markEndPos + 1).trim()
    val textInWrapper = textWithWrapper.removePrefix("<!--").removeSuffix("-->").trim()
    return (textInWrapper == markSub.markText) && (textInWrapper == expect)
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
        var text = "This is a <!--comment--> with <!--multiple-->\n<!--marks--> on multiple lines"
        var marks = extractMarksFromText(text)
        assert(marks.size == 3)
        assert(checkMarkText(text, marks[0], "comment"))
        assert(checkMarkText(text, marks[1], "multiple"))
        assert(checkMarkText(text, marks[2], "marks"))
    }

}