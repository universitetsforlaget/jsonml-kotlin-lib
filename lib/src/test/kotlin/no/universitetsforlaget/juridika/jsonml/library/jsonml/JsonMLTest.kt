package no.universitetsforlaget.juridika.jsonml.library.jsonml

import com.fasterxml.jackson.databind.ObjectMapper
import net.minidev.json.JSONArray
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test

class JsonMLTest {
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @Test
    fun `minidev JSONArray supports equal-comparison`() {
        val input = JsonML.createElement("foo")
        val expected = JsonML.createElement("foo")

        val notExpected = JsonML.createElement("baz")

        MatcherAssert.assertThat(input.jsonValue, CoreMatchers.equalTo(expected.jsonValue))
        MatcherAssert.assertThat(input.jsonValue, CoreMatchers.not(CoreMatchers.equalTo(notExpected.jsonValue)))

        MatcherAssert.assertThat(objectMapper.writeValueAsString(input), CoreMatchers.equalTo("""["foo"]"""))
    }

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
        val input = JsonML.createElement(tagName = "foo", children = listOf(JsonML.Text("\tlolæ\n")))
        val json = objectMapper.writeValueAsString(input)

        MatcherAssert.assertThat(json, CoreMatchers.equalTo("""["foo","\tlolæ\n"]"""))

        MatcherAssert.assertThat(
            JsonML.fromRaw(objectMapper.readValue(json, JSONArray::class.java)),
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
            JsonML.fromRaw(objectMapper.readValue(json, JSONArray::class.java)),
            CoreMatchers.equalTo(input)
        )
    }

    @Test
    fun `should deserialize with jackson`() {
        val json = """
            {
                "jsonml": [
                    "book"
                ]
            }
        """

        val testObject = objectMapper.readValue(json, TestObject::class.java)

        MatcherAssert.assertThat(
            testObject,
            CoreMatchers.equalTo(TestObject(JsonML.createElement("book").jsonValue as JSONArray))
        )
    }

    data class TestObject(
        val jsonml: JSONArray
    )
}
