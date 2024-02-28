package org.formentor.magnolia.ai.infrastructure.openai.api;

import lombok.Data;

import java.util.List;

/**
 * An object with a list of image results.
 */
@Data
public class ImageResult {

    /**
     * The creation time in epoch seconds.
     */
    Long created;

    /**
     * List of image results.
     */
    List<Image> data;
}
