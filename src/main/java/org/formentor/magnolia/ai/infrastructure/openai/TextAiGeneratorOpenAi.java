package org.formentor.magnolia.ai.infrastructure.openai;

import lombok.extern.slf4j.Slf4j;
import org.formentor.magnolia.ai.domain.TextAiGenerator;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionRequest;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatMessage;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatMessageRole;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class TextAiGeneratorOpenAi implements TextAiGenerator {
    private final OpenAiApi api;

    @Inject
    public TextAiGeneratorOpenAi(OpenAiApiClientProvider openAiApiClientProvider) {
        api = openAiApiClientProvider.get();
    }

    @Override
    public CompletableFuture<String> complete(String prompt, String model, Integer tokens) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(new ChatMessage(ChatMessageRole.USER.value(), prompt)))
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .temperature(0.5)
                .maxTokens(tokens)
                .build();

        return CompletableFuture.supplyAsync(() -> api.createChatCompletion(request))
                .thenApply(completionResult -> completionResult.getChoices().get(0).getMessage().getContent())
                .thenApply(this::removeStartingLineFeeds);
    }

    private String removeStartingLineFeeds(String text) {
        int indexStart = 0;
        for (; indexStart < text.length() && text.charAt(indexStart) == 10 ; indexStart++) {}

        return text.substring(indexStart);
    }
}
