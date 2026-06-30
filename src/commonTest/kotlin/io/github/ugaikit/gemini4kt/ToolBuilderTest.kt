package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Represents the tool builder test.
 */
class ToolBuilderTest {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Tests the `ToolBuilder` with a single function declaration.
     *
     * This test verifies that a `ToolBuilder` can correctly create a tool with a
     * single function declaration. The function declaration includes a name,
     * description, and parameters. The parameters include properties for
     * "location" and "description", with "description" being required.
     */
    @Test
    fun testToolBuilderWithSingleFunctionDeclaration() {
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

    /**
     * Tests test tool builder with multiple function declarations.
     */
    @Test
    fun testToolBuilderWithMultipleFunctionDeclarations() {
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
    fun testToolBuilderWithNoFunctionDeclarations() {
        val tool = tool {}

        assertEquals(0, tool.functionDeclarations!!.size)
    }

    /**
     * Tests test tool builder with google search.
     */
    @Test
    fun testToolBuilderWithGoogleSearch() {
        val tool =
            tool {
                googleSearch()
            }
        assertNotNull(tool.googleSearch)
    }

    @Test
    fun testToolBuilderWithAdvancedNestedConfigurations() {
        val builtGoogleSearch =
            googleSearch {
                timeRangeFilter = Interval(startTime = "2026-01-01T00:00:00Z", endTime = "2026-01-31T00:00:00Z")
                searchTypes = SearchTypes(webSearch = WebSearch(), imageSearch = ImageSearch())
            }

        val tool =
            tool {
                googleSearch {
                    timeRangeFilter = builtGoogleSearch.timeRangeFilter
                    searchTypes = builtGoogleSearch.searchTypes
                }
                fileSearch {
                    fileSearchStoreName("store-a")
                    metadataFilter("author=alice")
                }
                googleSearchRetrieval(
                    GoogleSearchRetrieval(
                        dynamicRetrievalConfig =
                            DynamicRetrievalConfig(
                                mode = DynamicRetrievalMode.MODE_DYNAMIC,
                                dynamicThreshold = 0.5,
                            ),
                    ),
                )
                computerUse {
                    environment = Environment.ENVIRONMENT_BROWSER
                    excludedPredefinedFunction("CLICK")
                }
                mcpServer(
                    McpServer(
                        name = "mcp-server",
                        streamableHttpTransport =
                            StreamableHttpTransport(
                                url = "https://mcp.example.com",
                                headers = mapOf("Authorization" to "Bearer token"),
                                timeout = "30s",
                                sseReadTimeout = "120s",
                                terminateOnClose = true,
                            ),
                    ),
                )
                googleMaps(GoogleMaps(enableWidget = true))
            }

        val encoded = json.encodeToString(tool)
        val decoded = json.decodeFromString<Tool>(encoded)

        assertNotNull(decoded.googleSearch)
        assertEquals("author=alice", decoded.fileSearch?.metadataFilter)
        assertEquals("store-a", decoded.fileSearch?.fileSearchStoreNames?.first())
        assertEquals(DynamicRetrievalMode.MODE_DYNAMIC, decoded.googleSearchRetrieval?.dynamicRetrievalConfig?.mode)
        assertEquals(Environment.ENVIRONMENT_BROWSER, decoded.computerUse?.environment)
        assertEquals("CLICK", decoded.computerUse?.excludedPredefinedFunctions?.first())
        assertEquals("mcp-server", decoded.mcpServers?.first()?.name)
        assertEquals(true, decoded.googleMaps?.enableWidget)
        assertEquals(true, encoded.contains("\"computer_use\""))
        assertEquals(true, encoded.contains("\"excluded_predefined_functions\""))
    }

    @Test
    fun testComputerUseBuilder() {
        val computerUse =
            computerUse {
                environment = Environment.ENVIRONMENT_BROWSER
                excludedPredefinedFunctions("CLICK", "TYPE")
            }

        assertEquals(Environment.ENVIRONMENT_BROWSER, computerUse.environment)
        assertEquals(2, computerUse.excludedPredefinedFunctions?.size)
        assertEquals("CLICK", computerUse.excludedPredefinedFunctions?.first())
        assertEquals("TYPE", computerUse.excludedPredefinedFunctions?.last())
    }
}
