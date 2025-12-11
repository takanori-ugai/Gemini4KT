package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.tool
import java.util.Properties

suspend fun main() {
    val path = Gemini::class.java.getResourceAsStream("/prop.properties")
    val prop =
        Properties().also {
            it.load(path)
        }
    val apiKey = prop.getProperty("apiKey")
    val gemini = Gemini(apiKey)

    val text =
        "Compare the ingredients and cooking times from the recipes" +
            " at https://www.foodnetwork.com/recipes/ina-garten/perfect-roast-chicken-recipe-1940592 " +
            "and https://www.allrecipes.com/recipe/70679/simple-whole-roasted-chicken/"

    val inputJson =
        GenerateContentRequest(
            listOf(Content(listOf(Part(text = text)))),
            tools =
                listOf(
                    tool {
                        urlContext()
                    },
                ),
        )
    val response =
        gemini.generateContent(
            inputJson,
            model = "gemini-2.5-flash",
        )

    println("Response Text:")
    response.candidates[0].content.parts?.forEach { part ->
        part.text?.let { println(it) }
    }

    println("\nURL Context Metadata:")
    response.candidates[0].urlContextMetadata?.urlMetadata?.forEach { meta ->
        println("URL: ${meta.retrievedUrl}")
        println("Status: ${meta.urlRetrievalStatus}")
    }
}

class UrlContextSample
