package glcore
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
        assert(GlJson.deseralizeAnyDict("").isEmpty())
        assert(GlJson.deseralizeAnyDict("  ").isEmpty())
        assert(GlJson.deseralizeAnyDict("{}").isEmpty())
        assert(GlJson.deseralizeAnyDict("  { }  ").isEmpty())
    }

    @Test
    fun testBasicSerializeAndDeserialize() {
        val pathDict = PathDict()
        pathDict.set(PATH_TASK_GROUP_TOP, TASK_GROUP_TOP_PRESET)
        val jsonText = GlJson.serializeAnyDict(pathDict.rootDict)
    }

}