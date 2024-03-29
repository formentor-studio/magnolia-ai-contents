package org.formentor.magnolia.ai.infrastructure.openai;

import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionRequest;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionResult;
import org.formentor.magnolia.ai.infrastructure.openai.api.CompletionRequest;
import org.formentor.magnolia.ai.infrastructure.openai.api.CompletionResult;
import org.formentor.magnolia.ai.infrastructure.openai.api.CreateImageRequest;
import org.formentor.magnolia.ai.infrastructure.openai.api.ImageResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Client interface of OpenAI API Rest
 */
public interface OpenAiApi {
    @Deprecated
    @POST
    @Path("/v1/completions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    CompletionResult createCompletion(CompletionRequest request);

    @POST
    @Path("/v1/chat/completions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    ChatCompletionResult createChatCompletion(ChatCompletionRequest request);

    @POST
    @Path("/v1/images/generations")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    ImageResult generateImage(CreateImageRequest request);

}
