package no.universitetsforlaget.juridika.libraries.jsonml

import com.fasterxml.jackson.annotation.JsonValue
import java.lang.RuntimeException

/**
 * Simple wrapper class for a JsonML value.
 * This exists in order to encapsulate the Any type needed to represent it.
 *
 * http://www.jsonml.org/syntax/
 */
sealed class JsonML {
    abstract val jsonValue: Any

    data class Text(
        val value: String
    ) : JsonML() {
        @JsonValue
        override val jsonValue = value
    }

    data class Element(
        val jsonArray: List<*>
    ) : JsonML() {
        @JsonValue
        override val jsonValue = jsonArray

        fun structured(): StructuredJsonMLElement {
            val tagName = jsonArray[0] as String
            var childStartIndex = 1

            val attributes: Map<String, Any>? = if (jsonArray.size > 1 && jsonArray[1] is Map<*, *>) {
                childStartIndex = 2
                val jsonmlAttributes = jsonArray[1] as Map<*, *>
                val attributes = mutableMapOf<String, Any>()

                for (entry in jsonmlAttributes.entries) {
                    val key = entry.key
                    if (key is String) {
                        attributes[key] = entry.value as Any
                    } else {
                        throw Exception("expected key to be a String")
                    }
                }

                attributes
            } else {
                null
            }

            val children = mutableListOf<JsonML>()

            for (childIndex in childStartIndex until jsonArray.size) {
                children.add(fromRaw(jsonArray[childIndex]))
            }

            return StructuredJsonMLElement(
                tagName = tagName,
                attributes = attributes,
                children = children
            )
        }
    }

    companion object {
        fun fromRaw(jsonml: Any?): JsonML {
            return when (jsonml) {
                is String -> Text(value = jsonml)
                is List<*> -> Element(jsonArray = jsonml)
                else -> throw RuntimeException("Unable to interpret jsonml value $jsonml")
            }
        }

        fun createElement(
            tagName: String,
            attributes: Map<String, Any>? = null,
            children: List<JsonML>? = null
        ): JsonML {
            val jsonml = mutableListOf<Any>()
            jsonml.add(tagName)

            if (attributes != null && attributes.isNotEmpty()) {
                jsonml.add(attributes)
            }

            var prevChild: JsonML? = null

            if (children != null) {
                for (child in children) {
                    prevChild = if (child is Text && prevChild is Text) {
                        jsonml.removeAt(jsonml.lastIndex)
                        val nextValue = prevChild.value + child.value
                        jsonml.add(nextValue)
                        Text(nextValue)
                    } else {
                        jsonml.add(child.jsonValue)
                        child
                    }
                }
            }
            return Element(jsonml)
        }
    }
}
