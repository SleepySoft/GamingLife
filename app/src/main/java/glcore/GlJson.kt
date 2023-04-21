package glcore


// ============================================================================ //
// I don't use existing JSon library because they are not fit for this program  //
// ============================================================================ //

// 在 Kotlin 中，除了 UInt 之外，还有 UByte、UShort 和 ULong 这些无符号整数类型也不是 Number 类型的子类。
//这些无符号整数类型不是 Number 类型的子类，是因为它们是在 Kotlin 1.3 版本中引入的实验性功能，目前仍处于实验阶段。
// 由于这些类型与其他数字类型在语义和行为上有所不同，因此它们并未被设计为 Number 类型的子类。

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
            else -> {
                println("Warning: Unknown data type in serialize any")
                wrapQuotes("null")
            }
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

    val DUMMY_CHARS = "\\u2222\\u3333\\u5555\\u7777"

    private fun unescapeJsonString(text: String): String =
        text.replace("\\\\", DUMMY_CHARS).
             replace("\\t", "\t").
             replace("\\r", "\r").
             replace("\\n", "\n").
             replace("\\f", "\u000C").
             replace("\\b", "\b").
             replace("\\\"", "\"").
             replace(DUMMY_CHARS, "\\")

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
