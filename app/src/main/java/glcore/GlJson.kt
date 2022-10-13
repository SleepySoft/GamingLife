package glcore


object GlJson {

    // ----------------------------------------- Serialize -----------------------------------------

    fun serializeAnyDict(anyDict: GlAnyDict) : String {
        return serializeDict(anyDict)
    }

    private fun wrapQuotes(text: String) : String = "\"$text\""

    private fun serializeList(data: List< * >) : String {
        val jsonLine = mutableListOf< String >()
        for (v in data) {
            jsonLine.add(serializeAny(v))
        }
        return "[" + jsonLine.joinToString(separator = ", ") + "]"
    }

    private fun serializeDict(data: Map< * , * >) : String {
        val jsonLine = mutableListOf< String >()
        for ((k, v) in data) {
            jsonLine.add("$k: ${serializeAny(v)}")
        }
        return "{" + jsonLine.joinToString(separator = ", ") + "}"
    }

    private fun serializeAny(data: Any?) : String {
        return when (data) {
            is Number -> data.toString()
            is Char -> wrapQuotes(data.toString())
            is String -> wrapQuotes(data)
            is Boolean -> wrapQuotes(if (data) "true" else "false")
            is List< * > -> serializeList(data)
            is Map< *, * > -> serializeDict(data)
            else -> ""
        }
    }

    // ---------------------------------------- Deserialize ----------------------------------------

    fun deseralizeAnyDict(jsonText: String) : GlAnyDict {
        if (checkWrapper(jsonText) != "{}") {
            return mutableMapOf()
        }

        val dictItems = splitByComma(jsonText.removePrefix("{").removeSuffix("}"))
        for (dictItem in dictItems) {
            val dictKv = splitByColon(dictItem)
            if (dictKv.size != 2) {
                // Error
            }
            else {
                
            }
        }

        return mutableMapOf()
    }

    fun checkWrapper(jsonText: String): String {
        val trimText = jsonText.trim()
        return when {
            trimText.startsWith("{") && jsonText.endsWith("}") -> "{}"
            trimText.startsWith("[") && jsonText.endsWith("]") -> "[]]"
            trimText.startsWith("\"") && jsonText.endsWith("\"") -> "\"\""
            else -> ""
            }
        }

    fun splitByComma(jsonText: String) : List< String > {
        // https://stackoverflow.com/a/51356605
        return jsonText.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex())
    }

    fun splitByColon(jsonText: String) : List< String > {
        // Just follow the upper format
        return jsonText.split(":(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex())
    }

    fun deseralizeList(jsonText: String) : List< * > {
        return mutableListOf< Any >()
    }
}
