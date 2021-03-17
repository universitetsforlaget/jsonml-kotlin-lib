package no.universitetsforlaget.juridika.jsonmlkotlinlib.model

import com.fasterxml.jackson.annotation.JsonValue
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
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
        val jsonArray: JSONArray
    ) : JsonML() {
        @JsonValue
        override val jsonValue = jsonArray

        fun structured(): StructuredJsonMLElement {
            val tagName = jsonArray[0] as String
            var childStartIndex = 1

            val attributes: Map<String, String>? = if (jsonArray.size > 1 && jsonArray[1] is JSONObject) {
                childStartIndex = 2
                val jsonmlAttributes = jsonArray[1] as JSONObject
                val attributes = mutableMapOf<String, String>()

                for (entry in jsonmlAttributes.entries) {
                    attributes[entry.key] = entry.value as String
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
                is JSONArray -> Element(jsonArray = jsonml)
                else -> throw RuntimeException("Unable to interpret jsonml value $jsonml")
            }
        }

        fun createElement(
            tagName: String,
            jsonAttributes: JSONObject? = null,
            attributes: Map<String, String>? = null,
            children: List<JsonML>? = null
        ): JsonML {
            val jsonml = JSONArray()
            jsonml.appendElement(tagName)

            if (jsonAttributes != null && jsonAttributes.size > 0) {
                jsonml.appendElement(jsonAttributes)
            } else if (attributes != null && attributes.isNotEmpty()) {
                val attributeObject = JSONObject()
                for (entry in attributes.entries) {
                    attributeObject[entry.key] = entry.value
                }
                jsonml.appendElement(attributeObject)
            }

            var prevChild: JsonML? = null

            if (children != null) {
                for (child in children) {
                    prevChild = if (child is Text && prevChild is Text) {
                        jsonml.removeAt(jsonml.lastIndex)
                        val nextValue = prevChild.value + child.value
                        jsonml.appendElement(nextValue)
                        Text(nextValue)
                    } else {
                        jsonml.appendElement(child.jsonValue)
                        child
                    }
                }
            }
            return Element(jsonml)
        }
    }
}
