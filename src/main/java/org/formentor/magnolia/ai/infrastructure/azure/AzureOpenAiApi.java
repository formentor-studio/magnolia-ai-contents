package org.formentor.magnolia.ai.infrastructure.azure;

import org.formentor.magnolia.ai.infrastructure.azure.api.CompletionRequest;
import org.formentor.magnolia.ai.infrastructure.azure.api.CompletionResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Client interface of Azure OpenAI API Rest
 */
public interface AzureOpenAiApi {
    @POST
    @Path("/completions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    CompletionResult createCompletion(@QueryParam("api-version") String apiVersion, CompletionRequest request);
}
