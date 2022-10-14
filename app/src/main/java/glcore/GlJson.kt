package glcore


object GlJson {

    // ----------------------------------------- Serialize -----------------------------------------

    fun serializeAnyDict(anyDict: GlAnyDict) : String {
        return serializeDict(anyDict)
    }

    private fun wrapQuotes(text: String) : String = "\"$text\""

    fun escapeJsonString(text: String): String =
        text.replace("\\", "\\\\").
             replace("\"", "\\\"").
             replace("\b", "\\b").
             replace("\u000C", "\\f").
             replace("\n", "\\n").
             replace("\r", "\\r").
             replace("\t", "\\t")

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
            if (k is String) {
                jsonLine.add("${escapeJsonString(k)}: ${serializeAny(v)}")
            }
            else {
                // Error case
            }
        }
        return "{" + jsonLine.joinToString(separator = ", ") + "}"
    }

    private fun serializeAny(data: Any?) : String {
        return when (data) {
            is Number -> data.toString()
            is Char -> wrapQuotes(escapeJsonString(data.toString()))
            is String -> wrapQuotes(escapeJsonString(data))
            is Boolean -> wrapQuotes(if (data) "true" else "false")
            is List< * > -> serializeList(data)
            is Map< *, * > -> serializeDict(data)
            else -> wrapQuotes("null")
        }
    }

    // ---------------------------------------- Deserialize ----------------------------------------

    fun deseralizeAnyDict(jsonText: String) : GlAnyDict {
        return deseralizeDict(jsonText.trim())
    }

    fun checkWrapper(trimmedText: String): String {
        return when {
            trimmedText.startsWith("{") && trimmedText.endsWith("}") -> "{}"
            trimmedText.startsWith("[") && trimmedText.endsWith("]") -> "[]]"
            trimmedText.startsWith("\"") && trimmedText.endsWith("\"") -> "\"\""
            else -> ""
            }
        }

    private fun unwrapQuotes(trimmedText: String) : String =
        trimmedText.removePrefix("\"").removeSuffix("\"")

    fun splitByComma(jsonText: String) : List< String > {
        // https://stackoverflow.com/a/51356605
        return jsonText.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex())
    }

    fun splitByColon(jsonText: String) : List< String > {
        // Just follow the upper format
        return jsonText.split(":(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex())
    }

    fun unescapeJsonString(text: String): String =
        text.replace("\\t", "\t").
        replace("\\r", "\r").
        replace("\\n", "\n").
        replace("\\f", "\u000C").
        replace("\\b", "\b").
        replace("\\\"", "\"").
        replace("\\\\", "\\")

    fun deseralizeList(trimmedText: String) : GlAnyList {
        val anyList = mutableListOf< Any >()

        if (checkWrapper(trimmedText) != "[]") {
            return mutableListOf()
        }

        val listItems = splitByComma(trimmedText.removePrefix("\"").removeSuffix("\""))
        for (listItem in listItems) {
            val v = deseralizeAny(listItem)
            v?.run {
                anyList.add(this)
            }
        }
        return anyList
    }

    fun deseralizeDict(trimmedText: String) : GlAnyDict {
        val anyDict = mutableMapOf< String, Any >()

        if (checkWrapper(trimmedText) != "{}") {
            return anyDict
        }

        val dictItems = splitByComma(trimmedText.removePrefix("{").removeSuffix("}"))
        for (dictItem in dictItems) {
            val dictKv = splitByColon(dictItem)
            if (dictKv.size != 2) {
                // Error
            }
            else {
                val k = unescapeJsonString(dictKv[0])
                val v = deseralizeAny(dictKv[1])
                v?.run {
                    anyDict[k] = this
                }
            }
        }
        return anyDict
    }

    fun deseralizeAny(jsonText: String) : Any? {
        val trimmedText = jsonText.trim()
        val wrapper = checkWrapper(trimmedText)
        return when (wrapper) {
            "{}" -> return deseralizeDict(trimmedText)
            "[]" -> return deseralizeList(trimmedText)
            "\"\"" -> return unescapeJsonString(unwrapQuotes(trimmedText))
            else -> {
                trimmedText.toIntOrNull() ?:
                trimmedText.toLongOrNull() ?:
                trimmedText.toFloatOrNull() ?:
                trimmedText.toDoubleOrNull()
            }
        }
    }
}
