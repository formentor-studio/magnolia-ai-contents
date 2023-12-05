package org.formentor.magnolia.ai.infrastructure.openai;

import org.formentor.magnolia.ai.AIContentsModule;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionChoice;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionRequest;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatMessage;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatMessageRole;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenAiApiTest {

    @Disabled // Integration test, requires connection to https://api.openai.com and a TOKEN
    @Test
    void createChatCompletion() {
        OpenAiApi apiClient = new OpenAiApiClientProvider(builAIContentsModule("https://api.openai.com"), mockTokenProvider(System.getenv("OPENAI_API_KEY"))).get();

        final List<ChatMessage> messages = List.of(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are the editor of a website."),
                new ChatMessage(ChatMessageRole.USER.value(), "Write a product description based on the information \n" +
                        "provided in the technical specifications delimited by \n" +
                        "triple backticks." +
                        "```" +
                        "material: 5-wheel plastic coated aluminum base." +
                        "width: 53 cm" +
                        "height: 51 cm" +
                        "```"));

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .temperature(0.5)
                .maxTokens(50)
                .build();

        List<ChatCompletionChoice> choices = apiClient.createChatCompletion(chatCompletionRequest).getChoices();
        assertEquals(1, choices.size());
    }

    private AIContentsModule builAIContentsModule(String host) {
        AIContentsModule aiContentsModule = new AIContentsModule();
        AIContentsModule.OpenAI openAI = new AIContentsModule.OpenAI();
        openAI.setHost(host);
        aiContentsModule.setOpenAI(openAI);

        return aiContentsModule;
    }

    private TokenProviderPasswords mockTokenProvider(String token) {
        TokenProviderPasswords tokenProvider = mock(TokenProviderPasswords.class);
        when(tokenProvider.get()).thenReturn(token);

        return tokenProvider;
    }
}
