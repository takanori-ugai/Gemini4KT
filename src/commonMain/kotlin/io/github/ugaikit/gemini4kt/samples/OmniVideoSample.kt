package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionGenerationConfig
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.interaction.InteractionVideoConfig
import io.github.ugaikit.gemini4kt.interaction.InteractionVideoTask

/** Builds Omni 1.2 video requests for use with GeminiAI.createInteraction. */
object OmniVideoSample {
    /**
     * Creates a video generation or follow-up editing request.
     *
     * Describe duration and media roles in [prompt]. [media] can contain reference images,
     * first/last frames, or a video to edit. [task] is an optional fallback to prompt inference.
     * Use [previousInteractionId] to continue an existing generation or extend its video.
     */
    fun request(
        prompt: String = "Generate a 15-second video of a marble rolling along a wooden track.",
        media: Array<InteractionContent> = emptyArray(),
        task: InteractionVideoTask? = null,
        previousInteractionId: String? = null,
    ): CreateInteractionRequest {
        require(prompt.isNotBlank()) { "prompt must not be blank." }
        return CreateInteractionRequest(
            model = "gemini-omni-1.2-flash",
            input =
                InteractionInput.InteractionContentList(
                    media + InteractionContent(type = "text", text = prompt),
                ),
            generationConfig = task?.let { InteractionGenerationConfig(videoConfig = InteractionVideoConfig(it)) },
            previousInteractionId = previousInteractionId,
        )
    }
}
