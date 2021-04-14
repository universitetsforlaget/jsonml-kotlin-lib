package no.universitetsforlaget.juridika.libraries.jsonml

data class StructuredJsonMLElement(
    val tagName: String,
    val attributes: Map<String, Any>?,
    val children: List<JsonML>
)
