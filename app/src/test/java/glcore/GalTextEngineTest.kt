package glcore

import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class GalTextEngineTest {

    @Test
    fun testExtractMarksFromText() {
        var text = "This is a <!--comment--> with one mark"
        var marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(marks[0].markStartPos == 10)
        assert(marks[0].markEndPos == 22)
        assert(marks[0].markText == "comment")

        text = "This is a <!--comment--> with <!--multiple--> marks"
        marks = extractMarksFromText(text)
        assert(marks.size == 2)
        assert(marks[0].markStartPos == 10)
        assert(marks[0].markEndPos == 22)
        assert(marks[0].markText == "comment")
        assert(marks[1].markStartPos == 33)
        assert(marks[1].markEndPos == 45)
        assert(marks[1].markText == "multiple")

        text = "This is a <!--nested <!--comment-->--> mark"
        marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(marks[0].markStartPos == 10)
        assert(marks[0].markEndPos == 32)
        assert(marks[0].markText == "nested <!--comment")

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
        assert(marks[0].markStartPos == 0)
        assert(marks[0].markEndPos == 12)
        assert(marks[0].markText == "comment")

        text = "at the end <!--comment-->"
        marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(marks[0].markStartPos == 12)
        assert(marks[0].markEndPos == 24)
        assert(marks[0].markText == "comment")

        text = "<!--multiple--><!--marks-->"
        marks = extractMarksFromText(text)
        assert(marks.size == 2)
        assert(marks[0].markStartPos == 0)
        assert(marks[0].markEndPos == 12)
        assert(marks[0].markText == "multiple")
        assert(marks[1].markStartPos == 14)
        assert(marks[1].markEndPos == 26)
        assert(marks[1].markText == "marks")

        text = "<!--   -->"
        marks = extractMarksFromText(text)
        assert(marks.size == 1)
        assert(marks[0].markStartPos == 0)
        assert(marks[0].markEndPos == 14)
        assert(marks[0].markText.isEmpty())
    }

}