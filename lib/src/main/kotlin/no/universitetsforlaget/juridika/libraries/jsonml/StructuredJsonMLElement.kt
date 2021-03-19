package no.universitetsforlaget.juridika.libraries.jsonml

data class StructuredJsonMLElement(
    val tagName: String,
    val attributes: Map<String, String>?,
    val children: List<JsonML>
)
