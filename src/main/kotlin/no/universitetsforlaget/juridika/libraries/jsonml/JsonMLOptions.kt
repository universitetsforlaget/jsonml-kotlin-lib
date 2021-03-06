package no.universitetsforlaget.juridika.libraries.jsonml


import org.w3c.dom.Element

data class JsonMLOptions(
    val sanitizeText: Boolean,

    val truncateWhitespace: Boolean,

    /**
     * Ability to filter out certain elements
     */
    val elementFilter: (element: Element) -> Boolean = { true }
)
