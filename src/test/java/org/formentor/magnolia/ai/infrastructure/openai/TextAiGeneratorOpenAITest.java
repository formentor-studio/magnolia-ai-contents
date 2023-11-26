package org.formentor.magnolia.ai.infrastructure.openai;

import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionChoice;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionRequest;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatCompletionResult;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatMessage;
import org.formentor.magnolia.ai.infrastructure.openai.api.ChatMessageRole;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TextAiGeneratorOpenAITest {

    private final static String COMPLETION_MODEL_EXAMPLE = "gpt-3.5-turbo-1106";
    @Test
    void CompleteConsumesOpenAiApi() {
        final String prompt = "i-am-a-prompt";
        final OpenAiApi openAiApi = mockOpenAiApi("nonce");
        final OpenAiApiClientProvider openAiApiClientProvider = mock(OpenAiApiClientProvider.class);
        when(openAiApiClientProvider.get()).thenReturn(openAiApi);

        TextAiGeneratorOpenAi textAiGeneratorOpenAi = new TextAiGeneratorOpenAi(openAiApiClientProvider);
        textAiGeneratorOpenAi.complete(prompt, COMPLETION_MODEL_EXAMPLE, 250).join();

        ArgumentCaptor<ChatCompletionRequest> chatCompletionRequestArgumentCaptor = ArgumentCaptor.forClass(ChatCompletionRequest.class);
        verify(openAiApi, times(1)).createChatCompletion(chatCompletionRequestArgumentCaptor.capture());
        assertEquals(prompt, chatCompletionRequestArgumentCaptor.getValue().getMessages().get(0).getContent());
    }

    private OpenAiApi mockOpenAiApi(String completionText) {
        OpenAiApi api = mock(OpenAiApi.class);

        ChatCompletionResult chatCompletionResult = new ChatCompletionResult();
        ChatCompletionChoice chatCompletionChoice = new ChatCompletionChoice();
        chatCompletionChoice.setMessage(new ChatMessage(ChatMessageRole.SYSTEM.value(), completionText));
        chatCompletionResult.setChoices(List.of(chatCompletionChoice));
        when(api.createChatCompletion(any())).thenReturn(chatCompletionResult);

        return api;
    }
}
