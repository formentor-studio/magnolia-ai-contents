package org.formentor.magnolia.ai.infrastructure.openai;

import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionRequest;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionResult;
import org.formentor.magnolia.ai.infrastructure.openai.api.CompletionRequest;
import org.formentor.magnolia.ai.infrastructure.openai.api.CompletionResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public interface OpenAiApi {
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

}
