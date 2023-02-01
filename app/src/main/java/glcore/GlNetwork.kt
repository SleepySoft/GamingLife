package glcore

import java.io.*
import java.net.URL
import java.net.HttpURLConnection


// Ref: https://juejin.cn/post/6844903828538523655

class GlNetwork {

    var lastRequest = ""
    var responseBodyData = ByteArray(0)
    var responseHeaderDict = mapOf< String , String >()

    fun getData(url: String, data: Map< String , String >): Boolean {
        val fullUrl = data.map { (key, value) ->
            "$key=$value"
        }.joinToString("&", prefix=url )

        val connection = (URL(fullUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            useCaches = false
            doOutput = true
        }

        lastRequest = "GET"
        responseBodyData = getResponseBodyData(connection)
        responseHeaderDict = getResponseHeaderDict(connection)

        return responseBodyData.size != 0
    }

    fun postDict(url: String, data: Map< String , String >): Boolean {
        val anyDict = data as GlAnyDict
        val dictJson = GlJson.serializeAnyDict(anyDict)
        return postData(url, dictJson.toByteArray(Charsets.UTF_8))
    }

    fun postData(url: String, data: ByteArray): Boolean {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
        }

        connection.outputStream.run {
            write(data)
            flush()
            close()
        }

        lastRequest = "POST"
        responseBodyData = getResponseBodyData(connection)
        responseHeaderDict = getResponseHeaderDict(connection)

        return responseBodyData.size != 0
    }

    // ---------------------------------------------------------------------------------------------

    private fun getResponseBodyData(connection: HttpURLConnection): ByteArray {
        return connection.inputStream.let {
            val buffer = ByteArray(10 * 1024)
            val baoStream = ByteArrayOutputStream()
            val biStream = BufferedInputStream(it)
            val boStream = BufferedOutputStream(baoStream)

            try {
                while (true) {
                    val length = biStream.read(buffer)
                    if (length > 0) {
                        boStream.write(buffer, 0, length)
                    } else {
                        break
                    }
                }
                boStream.flush()
                baoStream.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
                ByteArray(0)
            } finally {
                try {
                    boStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    biStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getResponseHeaderDict(connection: HttpURLConnection): Map< String , String > {
        val headerDict = mutableMapOf< String , String >()
        for (i in 0 until connection.headerFields.size) {
            val responseHeaderKey = connection.getHeaderFieldKey(i)
            val responseHeaderValue = connection.getHeaderField(i)
            responseHeaderKey?.let { key ->
                responseHeaderValue?.let { value ->
                    headerDict[key] = value
                }
            }
        }
        return headerDict
    }
}