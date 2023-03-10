package org.formentor.magnolia.ai.ui.field;

import info.magnolia.ui.field.ConfiguredFieldDefinition;
import info.magnolia.ui.field.FieldType;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * Field definition for Image AI.
 */
@FieldType("imageAI")
@Getter
@Setter
public class ImageAIFieldDefinition extends ConfiguredFieldDefinition<File> {
    private String promptProperty;

    public ImageAIFieldDefinition() {
        setType(File.class);
        setFactoryClass(ImageAIFieldFactory.class);
    }
}
