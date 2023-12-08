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
                .build();

        return CompletableFuture.supplyAsync(() -> api.createChatCompletion(request))
                .thenApply(completionResult -> completionResult.getChoices().get(0).getMessage().getContent());
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
}
