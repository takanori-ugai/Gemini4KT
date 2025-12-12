package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ToolBuilderTest {
    /**
     * Tests the `ToolBuilder` with a single function declaration.
     *
     * This test verifies that a `ToolBuilder` can correctly create a tool with a
     * single function declaration. The function declaration includes a name,
     * description, and parameters. The parameters include properties for
     * "location" and "description", with "description" being required.
     */
    @Test
    fun `test ToolBuilder with single function declaration`() {
        val tool =
            tool {
                functionDeclaration {
                    name = "testFunction"
                    description = "find movie titles currently playing in theaters" +
                        " based on any description, genre, title words, etc."
                    parameters {
                        type = "object"
                        property("location") {
                            type = "string"
                            description = "The city and state, e.g. San Francisco, CA" +
                                " or a zip code e.g. 95616"
                        }
                        property("description") {
                            type = "string"
                            description = "Any kind of description including category or genre"
                        }
                        required("description")
                    }
                }
            }
        assertEquals(1, tool.functionDeclarations!!.size)
        assertEquals("testFunction", tool.functionDeclarations[0].name)
        assertEquals("object", tool.functionDeclarations[0].parameters.type)
    }

    @Test
    fun `test ToolBuilder with multiple function declarations`() {
        val tool =
            tool {
                functionDeclaration {
                    name = "functionOne"
                    description = "find movie titles currently playing in theaters" +
                        " based on any description, genre, title words, etc."
                    parameters {
                        type = "object"
                        property("location") {
                            type = "string"
                            description = "The city and state, e.g. San Francisco, CA" +
                                " or a zip code e.g. 95616"
                        }
                        property("description") {
                            type = "string"
                            description = "Any kind of description including category or genre"
                        }
                        required("description")
                    }
                }

                functionDeclaration {
                    name = "functionTwo"
                    description = "find movie titles currently playing in theaters" +
                        " based on any description, genre, title words, etc."
                    parameters {
                        type = "object"
                        property("location") {
                            type = "string"
                            description = "The city and state, e.g. San Francisco, CA" +
                                " or a zip code e.g. 95616"
                        }
                        property("description") {
                            type = "string"
                            description = "Any kind of description including category or genre"
                        }
                        required("description")
                    }
                }
            }

        assertEquals(2, tool.functionDeclarations!!.size)
        assertEquals("functionOne", tool.functionDeclarations[0].name)
        assertEquals("object", tool.functionDeclarations[0].parameters.type)
        assertEquals("functionTwo", tool.functionDeclarations[1].name)
        assertEquals("object", tool.functionDeclarations[1].parameters.type)
    }

    /**
     * Tests the `ToolBuilder` with no function declarations.
     *
     * This test verifies that a `ToolBuilder` can correctly create a tool without
     * any function declarations. It ensures that the list of function declarations
     * is empty when no declarations are provided.
     */
    @Test
    fun `test ToolBuilder with no function declarations`() {
        val tool = tool {}

        assertEquals(0, tool.functionDeclarations!!.size)
    }

    @Test
    fun `test ToolBuilder with googleSearch`() {
        val tool =
            tool {
                googleSearch()
            }
        assertNotNull(tool.googleSearch)
    }
}
