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
    fun testBasicSerialize() {
        val pathDict = PathDict()
        val jsonText = GlJson.serializeAnyDict(pathDict.rootDict)
    }

}