package glcore
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Test


fun isJSONValid(test: String): Boolean {
    try {
        JSONObject(test);
    } catch (ex: JSONException) {
        try {
            JSONArray(test);
        } catch (ex1: JSONException) {
            return false;
        }
    }
    return true;
}


fun verifyJsonText(text: String) {
    System.out.println(text)
    assert(isJSONValid(text))
}



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
    fun testBasicSerializeAndDeserialize() {
        val pathDict = PathDict()
        pathDict.set(PATH_TASK_GROUP_TOP, TASK_GROUP_TOP_PRESET)
        pathDict.set(PATH_CURRENT_TASK, TASK_RECORD_TEMPLATE)
        pathDict.set(PATH_TASK_HISTORY, listOf(
            TASK_RECORD_TEMPLATE,
            TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_ENJOY },
            TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_LIFE },
            TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_WORK },
            TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_STUDY },
            TASK_RECORD_TEMPLATE.toMutableMap().apply { this["groupID"] = GROUP_ID_CREATE },
        ))

        val jsonText1 = GlJson.serializeAnyDict(pathDict.rootDict)
        verifyJsonText(jsonText1)

/*        val jsonElements: Map<String, JsonElement> = Json.parseToJsonElement(jsonText).jsonObject

        for ((k, v) in jsonElements) {
            print(k)
            print(v)
        }*/

        val anyDict = GlJson.deserializeAnyDict(jsonText1)
        val jsonText2 = GlJson.serializeAnyDict(anyDict)

        assert(jsonText1 == jsonText2)
    }
}