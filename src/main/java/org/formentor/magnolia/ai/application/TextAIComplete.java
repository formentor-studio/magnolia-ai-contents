package org.formentor.magnolia.ai.application;

import org.formentor.magnolia.ai.AIContentsModule;
import org.formentor.magnolia.ai.domain.TextAiGenerator;

import java.util.concurrent.CompletableFuture;

/**
 * Implement uses cases to create text from given prompt
 */
public class TextAIComplete {
    private final TextAiGenerator textAiGenerator;

    public TextAIComplete(AIContentsModule aiContentsModule) {
        textAiGenerator = aiContentsModule.getTextAiGenerator();
    }

    /**
     * Creates text content from a given prompt
     * @param prompt The prompt used to create the text
     * @param model The model name used to create the text
     * @param tokens The max number of tokens
     * @return Text created from the prompt using the specified model
     */
    public CompletableFuture<String> complete(String prompt, String model, Integer tokens) {
        return textAiGenerator.complete(prompt, model, tokens);
    }

    public CompletableFuture<String> edit(String prompt, String instruction, String model) {
        return textAiGenerator.edit(prompt, model, instruction);
    }
}
