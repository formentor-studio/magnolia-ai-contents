package org.formentor.magnolia.ai;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.objectfactory.Components;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.formentor.magnolia.ai.domain.TextAiGenerator;
import org.formentor.magnolia.ai.infrastructure.azure.TextAiGeneratorAzure;
import org.formentor.magnolia.ai.infrastructure.openai.TextAiGeneratorOpenAi;

import java.util.List;

/**
 * Configuration for the magnolia-ai-contents module.
 */
@Slf4j
@Setter
@Getter
public class AIContentsModule implements ModuleLifecycle {
    /* you can optionally implement info.magnolia.module.ModuleLifecycle */
    private OpenAI openAI;
    private Azure azure;
    private List<AiModel> models;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {

    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {

    }

    /**
     * Returns TextAiGenerator implementation depending on the configuration
     * @return Implementation of TextAiGenerator
     */
    public TextAiGenerator getTextAiGenerator() {
        if (getOpenAI() != null) {
            return Components.getComponent(TextAiGeneratorOpenAi.class);
        }
        if (getAzure() != null) {
            return Components.getComponent(TextAiGeneratorAzure.class);
        }

        log.error("Missing configuration for OpenAI or Azure, check the configuration of module magnolia-ai-contents");
        return TextAiGenerator.dummy();
    }

    @Getter
    @Setter
    public static class OpenAI {
        private String host;
    }

    @Getter
    @Setter
    public static class AiModel {
        private String label;
        private String name;
    }

    @Getter
    @Setter
    public static class Azure {
        private String host;
        private String resource;
        private String deployment;
        private String apiVersion;
    }

}
