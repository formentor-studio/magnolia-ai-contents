package org.formentor.magnolia.ai.application;

import org.formentor.magnolia.ai.domain.TextAiGenerator;

import java.util.concurrent.CompletableFuture;

public class TextAIComplete {
    private final TextAiGenerator textAiGenerator;

    public TextAIComplete(TextAiGenerator textAiGenerator) {
        this.textAiGenerator = textAiGenerator;
    }

    public CompletableFuture<String> complete(String prompt, String model, Integer tokens) {
        return textAiGenerator.complete(prompt, model, tokens);
    }

    public CompletableFuture<String> edit(String prompt, String instruction, String model) {
        return textAiGenerator.edit(prompt, model, instruction);
    }
}
