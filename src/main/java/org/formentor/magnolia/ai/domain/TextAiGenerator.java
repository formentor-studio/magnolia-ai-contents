package org.formentor.magnolia.ai.domain;

import java.util.concurrent.CompletableFuture;

public interface TextAiGenerator {
    default CompletableFuture<String> complete(String prompt, String model, Integer tokens) {
        throw new RuntimeException("Unsupported operation completeText(String, String, Integer)");
    }
    default CompletableFuture<String> edit(String prompt, String model, String instruction) {
        throw new RuntimeException("Unsupported operation completeText(String, String, String)");
    }

    static TextAiGenerator dummy() {
        return new TextAiGenerator() {
            @Override
            public CompletableFuture<String> complete(String prompt, String model, Integer tokens) {
                return CompletableFuture.completedFuture(prompt);
            }

            @Override
            public CompletableFuture<String> edit(String prompt, String model, String instruction) {
                return CompletableFuture.completedFuture(prompt);
            }
        };
    }
}
