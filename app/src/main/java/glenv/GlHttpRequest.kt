package glenv

import glcore.GlStrStruct
import okhttp3.*
import java.io.IOException


class GlHttpRequest(
    private val baseUrl: String) {
    private val httpClient: OkHttpClient = OkHttpClient().newBuilder().build()

    companion object {
        fun joinUrl(baseUrl: String, vararg subPaths: String) : String {
            var joined = baseUrl
            for (s in subPaths) {
                joined = joined.removeSuffix("/") + "/" + s.removeSurrounding("/")
            }
            return joined
        }

        fun joinParameters(params: Map<String, String>): String {
            val paramList = mutableListOf<String>()
            params.forEach { (k, v) ->
                paramList.add("%s=%s".format(k, v))
            }
            return paramList.joinToString("&")
        }

        fun catUrlParameter(url: String, param: String) =
            if (param.isNotBlank()) (url.removeSuffix("/") + "?" + param) else url
    }

    fun get(relativeUrl: String, params: GlStrStruct,
            callback: ((call: Call, response: Response) -> Unit)?) : Response {
        val paramString = joinParameters(params)
        val relativeUrlWithParams = catUrlParameter(relativeUrl, paramString)
        return doRequest(relativeUrlWithParams, requestBody = null, callback)
    }

    fun postJson(relativeUrl: String, jsonStr: String,
                 callback: ((call: Call, response: Response) -> Unit)?) : Response {
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"), jsonStr)
        return doRequest(relativeUrl, requestBody, callback)
    }

    fun postParams(relativeUrl: String, params: GlStrStruct,
                   callback: ((call: Call, response: Response) -> Unit)?) : Response {
        val requestBody: RequestBody = FormBody.Builder().run {
            for ((k, v) in params) {
                add(k, v)
            }
            build()
        }
        return doRequest(relativeUrl, requestBody, callback)
    }

    // ---------------------------------------------------------------------------------------------

    private fun doRequest(relativeUrl: String, requestBody: RequestBody?,
                          callback: ((call: Call, response: Response) -> Unit)?) : Response {

        val request: Request = Request.Builder().run {
            url(joinUrl(baseUrl, relativeUrl))
            requestBody?.let { post(it) }
            build()
        }

        val call = httpClient.newCall(request)

        return if (callback == null) {
            val response = call.execute()
            response
        } else {
            call.enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    // val result: String = response.body()?.string() ?: ""
                    callback(call, response)
                }

                override fun onFailure(call: Call, e: IOException) {

                }
            })
            emptyResponse(request)
        }
    }

    private fun emptyResponse(request: Request?) : Response {
        return Response.Builder().run {

            request(request ?: Request.Builder().run {
                url("https://sleepysoft.org")
                build()
            })

            protocol(Protocol.HTTP_2)
            code(401)
            message("")
            body(ResponseBody.create(MediaType.get("application/json; charset=utf-8"), "{}"))
            build()
        }
    }
}