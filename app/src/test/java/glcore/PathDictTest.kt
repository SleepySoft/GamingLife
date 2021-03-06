package glcore
import org.junit.Test


internal class PathDictTest {

    @Test
    fun testBasicPutGet() {
        val pathDict = PathDict()

        assert(pathDict.put("A/B/C/D/data1", 10))
        assert(pathDict.get("A/B/C/D/data1") == 10)

        assert(pathDict.put("A/B/C/D/data2", 2.2f))
        assert(pathDict.get("A/B/C/D/data2") == 2.2f)

        assert(pathDict.put("A/B/C/D/data3", "Text"))
        assert(pathDict.get("A/B/C/D/data3") == "Text")

        assert(pathDict.put("A/B/dataA", 333.33))
        assert(pathDict.get("A/B/dataA") == 333.33)

        assert(pathDict.put("A/B/dataB", 10))
        assert(pathDict.get("A/B/dataB") == 10)

        assert(pathDict.put("A/B/dataC", 10))
        assert(pathDict.get("A/B/dataC") == 10)

        assert(!pathDict.put("A", 1))
        assert(!pathDict.put("A/B", 2))
        assert(!pathDict.put("A/B/C", 3))
        assert(!pathDict.put("A/B/C/D", 4))

        assert(pathDict.put("A/B/C/D", 4, true))
        assert(pathDict.put("A/B/C", 3, true))
        assert(pathDict.put("A/B", 2, true))
        assert(pathDict.put("A", 1, true))

        assert(pathDict.get("A/B/C/D/data1") == null)
        assert(pathDict.get("A/B/C/D") == null)
        assert(pathDict.get("A/B/C") == null)
        assert(pathDict.get("A/B") == null)
        
        print(pathDict.rootDict)
    }

    @Test
    fun testPutDifferentType() {
        val pathDict = PathDict()

        assert(pathDict.put("A/B/C/D/data1", 10))
        assert(pathDict.get("A/B/C/D/data1") == 10)

        assert(pathDict.put("A/B/C/D/data1", 10.0f))
        assert(pathDict.get("A/B/C/D/data1") == 10.0f)

        assert(pathDict.put("A/B/C/D/data1", "ABC"))
        assert(pathDict.get("A/B/C/D/data1") == "ABC")

        print(pathDict.rootDict)
    }

    @Test
    fun testSpecialPath() {
        val pathDict = PathDict()

        assert(pathDict.put("/A/B/C/D/data1", 10))
        assert(pathDict.get("A/B/C/D/data1") == 10)

        assert(pathDict.put("/A/B/C/D/data1/", 101))
        assert(pathDict.get("A/B/C/D/data1") == 101)

        assert(pathDict.put("/A//B/C//D///data1/", 1001))
        assert(pathDict.get("A/B/C/D/data1") == 1001)

        print(pathDict.rootDict)
    }

    @Test
    fun testDictOperation() {
        val pathDict = PathDict()

        assert(pathDict.put("/A/B/C/X/data1", 10))
        assert(pathDict.put("/A/B/C/X/data2", 20))
        assert(pathDict.put("/A/B/C/X/data3", 30))

        val xt = pathDict.get("/A/B/C/X")
        assert(xt is MutableMap< *, * >)

        @Suppress("UNCHECKED_CAST")
        val x = xt as MutableMap< String, Any >

        assert(x["data1"] == 10)
        assert(x["data2"] == 20)
        assert(x["data3"] == 30)

        // ------------------------------------------------

        assert(pathDict.put("/A/B/Y/data1", "A"))
        assert(pathDict.put("/A/B/Y/data2", "B"))
        assert(pathDict.put("/A/B/Y/data3", "C"))

        val yt = pathDict.get("/A/B/Y")
        assert(yt is MutableMap< *, * >)

        @Suppress("UNCHECKED_CAST")
        val y = yt as MutableMap< String, Any >

        assert(y["data1"] == "A")
        assert(y["data2"] == "B")
        assert(y["data3"] == "C")

        print(pathDict.rootDict)
    }

    @Test
    fun testAutoConvertMapToMutable() {
        val pathDict = PathDict()

        assert(pathDict.put("/A/B/C", mapOf(
            "X" to 1,
            "Y" to "foo",
            "Z" to 3.14f
        )))

        assert(pathDict.get("/A/B/C") is MutableMap<*, *>)
        assert(pathDict.get("/A/B/C/X") == 1)
        assert(pathDict.set("/A/B/C/X", 2))
        assert(pathDict.get("/A/B/C/X") == 2)

        val dict = mapOf< String, Any >(
            "A" to mapOf< String, Any >(
                "B" to mapOf< String, Any >(
                    "C" to mapOf< String, Any >(
                        "X" to 100,
                        "Y" to "bar",
                        "Z" to 1.66f
                    )
                )
            )
        )

        pathDict.attach(dict.toMutableMap())

        assert(pathDict.get("/A") is MutableMap<*, *>)
        assert(pathDict.get("/A/B") is MutableMap<*, *>)
        assert(pathDict.get("/A/B/C") is MutableMap<*, *>)

        assert(pathDict.get("/A/B/C/Y") == "bar")
        assert(pathDict.set("/A/B/C/Y", "foobar"))
        assert(pathDict.get("/A/B/C/Y") == "foobar")
    }

    @Test
    fun testPathDictDeepCopy() {
        val pathDict: PathDict = generateSystemGeneralPathDict()

        val originalDict = pathDict.rootDict
        val shallowCopy = originalDict.toMutableMap()
        val deepCopy = originalDict.deepCopy()

        assert(shallowCopy == originalDict)
        assert(shallowCopy !== originalDict)
        assert(shallowCopy["Daily"] === originalDict["Daily"])
        assert(shallowCopy["Config"] === originalDict["Config"])
        assert(shallowCopy["Runtime"] === originalDict["Runtime"])

        assert(deepCopy == originalDict)
        assert(deepCopy !== originalDict)
        assert(deepCopy["Daily"] !== originalDict["Daily"])
        assert(deepCopy["Config"] !== originalDict["Config"])
        assert(deepCopy["Runtime"] !== originalDict["Runtime"])
    }
}