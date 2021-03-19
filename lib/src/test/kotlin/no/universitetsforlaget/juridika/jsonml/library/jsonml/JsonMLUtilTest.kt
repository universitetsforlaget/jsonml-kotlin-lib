package no.universitetsforlaget.juridika.jsonml.library.jsonml

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class JsonMLUtilTest {
    @Test
    fun `should convert BITS to JsonML`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <book xmlns:xlink="http://www.w3.org/1999/xlink" foo="bar">
                test
                <a href="foo"/>
            </book>
        """.trimIndent()

        val document = parseStringAsXml(xml)

        assertThat(
            convertDomDocumentToJsonML(document, JsonMLOptions(sanitizeText = false, truncateWhitespace = false)),
            equalTo(
                JsonML.createElement(
                    tagName = "book",
                    attributes = mapOf(
                        "xmlns:xlink" to "http://www.w3.org/1999/xlink",
                        "foo" to "bar"
                    ),
                    children = listOf(
                        JsonML.Text("\n    test\n    "),
                        JsonML.createElement(
                            tagName = "a",
                            attributes = mapOf(
                                "href" to "foo"
                            ),
                            children = null
                        ),
                        JsonML.Text("\n")
                    )
                )
            )
        )

        assertThat(
            convertDomDocumentToJsonML(document, JsonMLOptions(sanitizeText = false, truncateWhitespace = true)),
            equalTo(
                JsonML.createElement(
                    tagName = "book",
                    attributes = mapOf(
                        "xmlns:xlink" to "http://www.w3.org/1999/xlink",
                        "foo" to "bar"
                    ),
                    children = listOf(
                        JsonML.Text(" test "),
                        JsonML.createElement(
                            tagName = "a",
                            attributes = mapOf(
                                "href" to "foo"
                            ),
                            children = null
                        ),
                        JsonML.Text(" ")
                    )
                )
            )
        )
    }

    @Test
    fun `should sanitize text`() {
        val document = parseStringAsXml("<lol>  A  B\u2028C  </lol>")
        val options = JsonMLOptions(sanitizeText = true, truncateWhitespace = true)
        assertThat(
            convertDomDocumentToJsonML(document, options),
            equalTo(
                JsonML.createElement(
                    tagName = "lol",
                    children = listOf(JsonML.Text(" A  B C "))
                ))
        )
    }

    @Test
    fun `should filter DOM elements`() {
        assertThat(
            convertDomDocumentToJsonML(
                document = parseStringAsXml("<a><b><c>foo</c></b><d>bar</d></a>"),
                options = JsonMLOptions(
                    sanitizeText = false,
                    truncateWhitespace = false,
                    elementFilter = { element ->
                        element.tagName != "b"
                    }
                )
            ),
            equalTo(
                JsonML.createElement(
                    tagName = "a",
                    children = listOf(
                        JsonML.createElement(
                            tagName = "d",
                            children = listOf(
                                JsonML.Text("bar")
                            )
                        )
                    )
                )
            )
        )
    }

    companion object {
        fun parseStringAsXml(string: String): Document {
            return DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(ByteArrayInputStream(string.toByteArray()))
        }
    }
}

