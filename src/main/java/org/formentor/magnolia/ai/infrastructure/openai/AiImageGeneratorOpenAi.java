package org.formentor.magnolia.ai.infrastructure.openai;

import org.formentor.magnolia.ai.domain.ImageAiGenerator;
import org.formentor.magnolia.ai.domain.ImageFormat;
import org.formentor.magnolia.ai.domain.ImageSize;
import org.formentor.magnolia.ai.infrastructure.openai.api.CreateImageRequest;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of image generator for OpenAI
 */
public class AiImageGeneratorOpenAi implements ImageAiGenerator {
    private final OpenAiApi api;

    @Inject
    public AiImageGeneratorOpenAi(OpenAiApiClientProvider openAiApiClientProvider) {
        api = openAiApiClientProvider.get();
    }

    @Override
    public CompletableFuture<String> generateImage(String prompt, Integer units, ImageSize size, ImageFormat format) {
        CreateImageRequest request = CreateImageRequest.builder()
                .prompt(prompt)
                .n(units)
                .size(size.value)
                .responseFormat(format.toString())
                .build();

        return CompletableFuture.supplyAsync(() -> api.generateImage(request))
                .thenApply(imagesGenerationResponse -> {
                    switch (format) {
                        case url:
                            return imagesGenerationResponse.getData().get(0).getUrl();
                        case base64:
                            return imagesGenerationResponse.getData().get(0).getB64Json();
                        default:
                            return null;
                    }
                });
    }
}
