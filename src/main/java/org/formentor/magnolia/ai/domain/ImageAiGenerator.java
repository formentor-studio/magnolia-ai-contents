package org.formentor.magnolia.ai.domain;

import java.util.concurrent.CompletableFuture;

/**
 * Domain class that specifies the contract of image generators
 */
public interface ImageAiGenerator {
    CompletableFuture<String> generateImage(String prompt, Integer units, ImageSize size, ImageFormat format);
}
