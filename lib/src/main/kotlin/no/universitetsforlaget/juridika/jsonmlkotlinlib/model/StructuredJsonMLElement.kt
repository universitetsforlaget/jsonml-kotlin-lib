package no.universitetsforlaget.juridika.jsonmlkotlinlib.model

data class StructuredJsonMLElement(
    val tagName: String,
    val attributes: Map<String, String>?,
    val children: List<JsonML>
)
