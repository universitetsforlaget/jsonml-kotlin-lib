package no.universitetsforlaget.juridika.jsonml.library.jsonml

data class StructuredJsonMLElement(
    val tagName: String,
    val attributes: Map<String, String>?,
    val children: List<JsonML>
)
