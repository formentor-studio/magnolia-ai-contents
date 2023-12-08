package org.formentor.magnolia.ai.domain;

import java.util.concurrent.CompletableFuture;


public interface ImageAiGenerator {
    CompletableFuture<String> generateImage(String prompt, Integer units, ImageSize size, ImageFormat format);
}
