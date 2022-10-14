package glcore




// ============================================================================ //
// I don't use existing JSon library because they are not fit for this program  //
// ============================================================================ //


object GlJson {

    // ----------------------------------------- Serialize -----------------------------------------

    fun serializeAnyDict(anyDict: GlAnyDict) : String {
        return serializeDict(anyDict)
    }

    private fun wrapQuotes(text: String) : String = "\"$text\""

    // https://stackoverflow.com/a/58689286

    private fun escapeJsonString(text: String): String =
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
                jsonLine.add("${wrapQuotes(escapeJsonString(k))}: ${serializeAny(v)}")
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

    private fun checkWrapper(trimmedText: String): String {
        return when {
            trimmedText.startsWith("{") && trimmedText.endsWith("}") -> "{}"
            trimmedText.startsWith("[") && trimmedText.endsWith("]") -> "[]]"
            trimmedText.startsWith("\"") && trimmedText.endsWith("\"") -> "\"\""
            else -> ""
            }
        }

    private fun unwrapQuotes(trimmedText: String) : String =
        trimmedText.removePrefix("\"").removeSuffix("\"")

    private fun splitByComma(jsonText: String) : List< String > {
        // https://stackoverflow.com/a/51356605
        return jsonText.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex(), limit = 2)
    }

    private fun splitByColon(jsonText: String) : List< String > {
        // https://stackoverflow.com/a/17904715
        return jsonText.split("(?:,|\\{)?([^:]*):(\"[^\"]*\"|\\{[^}]*\\}|[^},]*)".toRegex())
    }

    private fun unescapeJsonString(text: String): String =
        text.replace("\\t", "\t").
        replace("\\r", "\r").
        replace("\\n", "\n").
        replace("\\f", "\u000C").
        replace("\\b", "\b").
        replace("\\\"", "\"").
        replace("\\\\", "\\")

    private fun deseralizeList(trimmedText: String) : GlAnyList {
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

    private fun deseralizeDict(trimmedText: String) : GlAnyDict {
        val anyDict = mutableMapOf< String, Any >()

        if (checkWrapper(trimmedText) != "{}") {
            return anyDict
        }

        val dictItems = splitByColon(trimmedText)
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

    private fun deseralizeAny(jsonText: String) : Any? {
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
