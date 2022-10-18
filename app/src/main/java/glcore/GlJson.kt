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

    fun deserializeAnyDict(jsonText: String) : GlAnyDict {
        return deserializeDict(jsonText.trim())
    }

    private fun checkWrapper(trimmedText: String): String {
        return when {
            trimmedText.startsWith("{") && trimmedText.endsWith("}") -> "{}"
            trimmedText.startsWith("[") && trimmedText.endsWith("]") -> "[]"
            trimmedText.startsWith("\"") && trimmedText.endsWith("\"") -> "\"\""
            else -> ""
            }
        }

    private fun unwrapQuotes(trimmedText: String) : String =
        trimmedText.removePrefix("\"").removeSuffix("\"")

/*    private fun splitByComma(jsonText: String) : List< String > {
        // https://stackoverflow.com/a/51356605
        return jsonText.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex(), limit = 2)
    }

    private fun splitByColon(jsonText: String) : List< String > {
        // https://stackoverflow.com/a/17904715
        return jsonText.split("(?:,|\\{)?([^:]*):(\"[^\"]*\"|\\{[^}]*\\}|[^},]*)".toRegex())
    }*/

    private fun unescapeJsonString(text: String): String =
        text.replace("\\t", "\t").
        replace("\\r", "\r").
        replace("\\n", "\n").
        replace("\\f", "\u000C").
        replace("\\b", "\b").
        replace("\\\"", "\"").
        replace("\\\\", "\\")

    private fun jsonTokenSplit(text: String, splitter: Char) : MutableList< String > {
        var inQuote = false
        var inEscape = false
        var curlyBracketsLevel = 0
        var squareBracketsLevel = 0

        var basePos = 0
        val splitItems = mutableListOf< String >()

        for (checkPos in text.indices) {
            val ch: Char = text[checkPos]

            if (inEscape) {
                inEscape = false
                continue
            }
            if (ch == '\\') {
                inEscape = true
                continue
            }
            if (inQuote) {
                if (ch == '\"') {
                    inQuote = false
                }
                continue
            }

            when (ch) {
                splitter -> {
                    if ((curlyBracketsLevel == 0) && (squareBracketsLevel == 0))
                    {
                        if (basePos < checkPos) {
                            splitItems.add(text.substring(basePos, checkPos).trim())
                        } else {
                            splitItems.add("")
                        }
                        basePos = checkPos + 1
                    }
                }
                '"' -> {
                    inQuote = true
                }
                '{' -> curlyBracketsLevel += 1
                '}' -> curlyBracketsLevel -= 1
                '[' -> squareBracketsLevel += 1
                ']' -> squareBracketsLevel -= 1
            }
        }

        splitItems.add(text.substring(basePos, text.length).trim())

/*        if (text.isNotEmpty()) {
            val remainingText = if (basePos < text.length - 1)
                text.substring(basePos, text.length - 1).trim() else ""
            splitItems.add(remainingText)
        }*/

        return splitItems
    }

    private fun deserializeList(trimmedText: String) : GlAnyList {
        val anyList = mutableListOf< Any >()

        if (checkWrapper(trimmedText) != "[]") {
            return mutableListOf()
        }

        val listItems = jsonTokenSplit(
            trimmedText.removePrefix("[").removeSuffix("]"), ',')
        for (listItem in listItems) {
            val v = deserializeAny(listItem)
            v?.run {
                anyList.add(this)
            }
        }
        return anyList
    }

    private fun deserializeDict(trimmedText: String) : GlAnyDict {
        val anyDict = mutableMapOf< String, Any >()

        if (checkWrapper(trimmedText) != "{}") {
            return anyDict
        }

        val listItems = jsonTokenSplit(
            trimmedText.removePrefix("{").removeSuffix("}"), ',')
        for (dictItem in listItems) {
            val dictKv = jsonTokenSplit(dictItem, ':')
            if (dictKv.size != 2) {
                // Error
                System.out.println("The dict k,v item expect 2 but ${dictKv.size}")
            }
            else {
                val k = unescapeJsonString(unwrapQuotes(dictKv[0]))
                val v = deserializeAny(dictKv[1])
                v?.run {
                    anyDict[k] = this
                }
            }
        }
        return anyDict
    }

    private fun deserializeAny(jsonText: String) : Any? {
        val trimmedText = jsonText.trim()
        return when (checkWrapper(trimmedText)) {
            "{}" -> return deserializeDict(trimmedText)
            "[]" -> return deserializeList(trimmedText)
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
