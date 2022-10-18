package glcore
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Test


internal class GlJsonTest {

    @Test
    fun testEmptySerialize() {
        val pathDict = PathDict()
        val jsonText = GlJson.serializeAnyDict(pathDict.rootDict)
        assert(jsonText == "{}")
    }

    @Test
    fun testEmptyDeserialize() {
        assert(GlJson.deserializeAnyDict("").isEmpty())
        assert(GlJson.deserializeAnyDict("  ").isEmpty())
        assert(GlJson.deserializeAnyDict("{}").isEmpty())
        assert(GlJson.deserializeAnyDict("  { }  ").isEmpty())
    }

    @Test
    fun testEscapeCharacters() {
        val escapeChars = "\b\u000C\n\r\t\"\\"
        val escapedEscapeChars = "\\b\\f\\n\\r\\t\\\"\\\\"
        val mixedEscapeChars = "a\bb\u000Cc\nd\re\tf\"g\\"
        val mixedEscapedEscapeChars = "u\\bv\\u000Cw\\nx\\ry\\tz\\z\"a\\b\\c"

        val dict = mutableMapOf< String, Any >(
            "EscapeChars" to escapeChars,
            "EscapedEscapeChars" to escapedEscapeChars,

            "MixedEscapeChars" to mixedEscapeChars,
            "MixedEscapedEscapeChars" to mixedEscapedEscapeChars
        )
        val jsonText1 = GlJson.serializeAnyDict(dict)
        verifyJsonText(jsonText1)

        val anyDict = GlJson.deserializeAnyDict(jsonText1)

        assert(anyDict["EscapeChars"] as String == escapeChars)
        assert(anyDict["EscapedEscapeChars"] as String == escapedEscapeChars)

        assert(anyDict["MixedEscapeChars"] as String == mixedEscapeChars)
        assert(anyDict["MixedEscapedEscapeChars"] as String == mixedEscapedEscapeChars)

        val jsonText2 = GlJson.serializeAnyDict(anyDict)

        assert(jsonText1 == jsonText2)
    }

    @Test
    fun testNormalSerializeAndDeserialize() {
        val pathDict = generateSystemGeneralPathDict()

        val jsonText1 = GlJson.serializeAnyDict(pathDict.rootDict)
        verifyJsonText(jsonText1)

        val anyDict = GlJson.deserializeAnyDict(jsonText1)
        val jsonText2 = GlJson.serializeAnyDict(anyDict)

        assert(jsonText1 == jsonText2)
    }
}
