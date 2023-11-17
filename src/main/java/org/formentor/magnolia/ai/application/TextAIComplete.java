package org.formentor.magnolia.ai.application;

import java.util.concurrent.CompletableFuture;

public class TextAIComplete {
    public CompletableFuture<String> completeText(String prompt, Integer words, String performance) {
        throw new RuntimeException("Unsupported operation completeText(String, Integer)");
    }

    public CompletableFuture<String> editText(String prompt, String instruction) {
        throw new RuntimeException("Unsupported operation editText(String, String)");
    }
}
