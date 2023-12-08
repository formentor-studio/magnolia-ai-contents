package org.formentor.magnolia.ai.infrastructure.azure;

import org.apache.commons.lang3.StringUtils;
import org.formentor.magnolia.ai.AIContentsModule;
import org.formentor.magnolia.ai.domain.TextAiGenerator;
import org.formentor.magnolia.ai.infrastructure.azure.api.CompletionRequest;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class TextAiGeneratorAzure implements TextAiGenerator {

    private final int AZURE_OPENAI_MAX_TOKENS = 4096;
    private final String apiVersion;
    private final AzureOpenAiApi api;

    @Inject
    public TextAiGeneratorAzure(AzureOpenAiApiClientProvider apiClientProvider, AIContentsModule definition) {
        this(apiClientProvider, definition.getAzure() == null? null: definition.getAzure().getApiVersion());
    }

    public TextAiGeneratorAzure(AzureOpenAiApiClientProvider apiClientProvider, String apiVersion) {
        this.api = apiClientProvider.get();
        this.apiVersion = apiVersion;
    }

    @Override
    public CompletableFuture<String> complete(String prompt, String model, Integer tokens) {
        CompletionRequest request = CompletionRequest.builder()
                .prompt(prompt)
                .frequency_penalty(0.0)
                .presence_penalty(0.0)
                .temperature(0.5)
                .max_tokens(AZURE_OPENAI_MAX_TOKENS - estimateTokensCount(prompt))
                .build();

        return CompletableFuture.supplyAsync(() -> api.createCompletion(apiVersion, request))
                .thenApply(completionResult -> completionResult.getChoices().get(0).getText())
                .thenApply(text -> StringUtils.normalizeSpace(text));
    }

    @Override
    public CompletableFuture<String> edit(String prompt, String model, String instruction) {
        /*
        Translate the following from slang to a business letter:
        'Dude, This is Joe, check out this spec on this standing lamp.'
         */
        String promptWithInstruction = String.format("%s:\n%s", instruction, prompt);

        return complete(promptWithInstruction, model, null);
    }

    private int estimateTokensCount(String prompt) {
        /**
         * At 2023-06-14 based on https://help.openai.com/en/articles/4936856-what-are-tokens-and-how-to-count-them
         *
         * 1 token ~= 4 chars in English
         * 1 token ~= ¾ words
         * 100 tokens ~= 75 words
         * Or
         * 1-2 sentence ~= 30 tokens
         * 1 paragraph ~= 100 tokens
         * 1,500 words ~= 2048 tokens
         */

        return (int)(prompt.length()/4 * 1.45);  // Add 45% for security
    }
}
