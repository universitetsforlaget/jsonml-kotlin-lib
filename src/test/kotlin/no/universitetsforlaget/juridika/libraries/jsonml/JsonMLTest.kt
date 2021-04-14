package no.universitetsforlaget.juridika.libraries.jsonml

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class JsonMLTest {
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @Test
    fun `serializes full element`() {
        val input = JsonML.createElement(
            tagName = "foo",
            attributes = mapOf("bar" to "baz"),
            children = listOf(JsonML.createElement("qux"), JsonML.Text("text"))
        )

        MatcherAssert.assertThat(
            objectMapper.writeValueAsString(input.jsonValue),
            CoreMatchers.equalTo("""["foo",{"bar":"baz"},["qux"],"text"]""")
        )
    }

    @Test
    fun `serializes many attributes`() {
        val input = JsonML.createElement(
            tagName = "foo",
            attributes = mapOf(
                "bar" to "baz",
                "baz" to "qux"
            ),
            children = listOf(JsonML.createElement("flopf"), JsonML.Text("gkæmt"))
        )

        MatcherAssert.assertThat(
            objectMapper.writeValueAsString(input),
            CoreMatchers.equalTo("""["foo",{"bar":"baz","baz":"qux"},["flopf"],"gkæmt"]""")
        )
    }

    @Test
    fun `should (de)serialize special unicode characters, etc`() {
        val input =
            JsonML.createElement(tagName = "foo", children = listOf(JsonML.Text("\tlolæ\n")))
        val json = objectMapper.writeValueAsString(input)

        MatcherAssert.assertThat(json, CoreMatchers.equalTo("""["foo","\tlolæ\n"]"""))

        MatcherAssert.assertThat(
            JsonML.fromRaw(objectMapper.readValue(json, List::class.java)),
            CoreMatchers.equalTo(input)
        )
    }

    @Test
    fun `should join adjacent text children`() {
        val input = JsonML.createElement(
            tagName = "foo",
            children = listOf(
                JsonML.Text("a"),
                JsonML.Text("b"),
                JsonML.Text("c")
            )
        )
        val json = objectMapper.writeValueAsString(input)

        MatcherAssert.assertThat(json, CoreMatchers.equalTo("""["foo","abc"]"""))

        MatcherAssert.assertThat(
            JsonML.fromRaw(objectMapper.readValue(json, List::class.java)),
            CoreMatchers.equalTo(input)
        )
    }

    @Test
    fun `should deserialize with jackson`() {
        val json = """
            {
                "jsonml": [
                    "book",
                    {"attribute": "value"},
                    "child"
                ]
            }
        """

        val testObject = objectMapper.readValue(json, TestObject::class.java)

        assertThat(
            testObject,
            equalTo(
                TestObject(JsonML.createElement(
                    tagName = "book",
                    attributes = mapOf("attribute" to "value"),
                    children = listOf(JsonML.Text("child"))
                ).jsonValue as List<Any>))
        )

        val node = JsonML.fromRaw(testObject.jsonml)
        val structured = when (node) {
            is JsonML.Element -> node.structured()
            else -> throw Exception("Expected a node")
        }

        assertThat(structured.tagName, equalTo("book"))
        assertThat(structured.attributes, equalTo(mapOf("attribute" to "value")))
        assertThat(structured.children[0], equalTo(JsonML.Text("child")))
    }

    data class TestObject(
        val jsonml: List<Any>
    )
}
