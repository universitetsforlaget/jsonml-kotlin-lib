package no.universitetsforlaget.juridika.libraries.jsonml

import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.Text
import javax.xml.parsers.DocumentBuilderFactory

fun convertDomDocumentToJsonML(document: Document, options: JsonMLOptions): JsonML {
    return convertDomElementToJsonML(document.documentElement, options)
}

private const val LSEP = "\u2028"
private val INTO_WHITESPACE_REGEX = "[$LSEP]".toRegex()
private val WHITESPACE_START_REGEX = """^[\p{Space}]+""".toRegex()
private val WHITESPACE_END_REGEX = """[\p{Space}]+$""".toRegex()

fun convertDomNodeToJsonML(node: Node, options: JsonMLOptions): JsonML? {
    return when (node) {
        is Text -> {
            var value = node.nodeValue

            if (options.sanitizeText) {
                value = value.replace(INTO_WHITESPACE_REGEX, " ")
            }

            if (options.truncateWhitespace) {
                value = value
                    .replace(WHITESPACE_START_REGEX, " ")
                    .replace(WHITESPACE_END_REGEX, " ")
            }

            JsonML.Text(value)
        }
        is Element -> {
            if (options.elementFilter(node)) {
                convertDomElementToJsonML(node, options)
            } else {
                null
            }
        }
        else -> null
    }
}

fun convertDomElementToJsonML(element: Element, options: JsonMLOptions): JsonML {
    val children = mutableListOf<JsonML>()
    var domChild = element.firstChild
    while (domChild != null) {
        convertDomNodeToJsonML(domChild, options)?.let {
            children.add(it)
        }
        domChild = domChild.nextSibling
    }

    val domAttributes = element.attributes
    val attributes: Map<String, Any>? = if (domAttributes.length > 0) {
        val attributes = mutableMapOf<String, Any>()
        for (index in 0 until domAttributes.length) {
            val item = domAttributes.item(index)
            if (item !is Attr) continue

            attributes[item.name] = item.value
        }
        attributes
    } else null

    return JsonML.createElement(
        tagName = element.tagName,
        attributes = attributes,
        children = if (children.isEmpty()) null else children
    )
}

/**
 * convert a JsonML node to a org.w3c.dom Document.
 * @param documentNamespaces a map of qualified namespace name to namespace URL.
 */
fun convertBitsJsonMLToDomDocument(
    node: JsonML,
    documentNamespaces: Map<String, String>
): Document {
    val factory = DocumentBuilderFactory.newInstance()
    factory.isNamespaceAware = true
    val implementation = factory.newDocumentBuilder().domImplementation

    val structuredElement = when (node) {
        is JsonML.Element -> node.structured()
        else -> throw InternalError("Unable to convert hast to XML: No root element")
    }

    val documentTagName = when (structuredElement.tagName) {
        "fragment" -> "bits-fragment"
        else -> structuredElement.tagName
    }

    val document = implementation.createDocument("", documentTagName, null)
    val documentElement = document.documentElement

    for ((qualifiedName, url) in documentNamespaces) {
        documentElement.setAttributeNS(
            "http://www.w3.org/2000/xmlns/",
            "xmlns:${qualifiedName}",
            url
        )
    }

    populateDomElement(structuredElement, documentElement, document)
    return document
}

fun convertJsonMLToDom(node: JsonML, document: Document): Node? {
    return when (node) {
        is JsonML.Text -> document.createTextNode(node.value)
        is JsonML.Element -> {
            val structuredElement = node.structured()
            val domElement = document.createElement(structuredElement.tagName)

            populateDomElement(structuredElement, domElement, document)
            domElement
        }
    }
}

private fun populateDomElement(structuredElement: StructuredJsonMLElement, domElement: Element, document: Document) {
    if (structuredElement.attributes != null) {
        for (entry in structuredElement.attributes) {
            val value = entry.value
            if (value is String) {
                domElement.setAttribute(entry.key, value)
            }
        }
    }

    for (jsonmlChild in structuredElement.children) {
        val domNode = convertJsonMLToDom(jsonmlChild, document)
        if (domNode != null) {
            domElement.appendChild(domNode)
        }
    }
}
