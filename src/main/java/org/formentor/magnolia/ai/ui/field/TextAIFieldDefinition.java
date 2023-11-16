package org.formentor.magnolia.ai.ui.field;

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.TextFieldDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Field definition for Image AI.
 */
@FieldType("textFieldAI")
@Getter
@Setter
@Slf4j
public class TextAIFieldDefinition extends TextFieldDefinition {
    private Integer words;
    private String performance;
    private String strategy;
    private PromptGeneratorDefinition promptGenerator;

    public TextAIFieldDefinition() {
        setFactoryClass(TextAIFieldFactory.class);
    }

}
