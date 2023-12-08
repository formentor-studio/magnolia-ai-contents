package org.formentor.magnolia.ai.ui.field;

import com.vaadin.ui.Component;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.field.factory.AbstractFieldFactory;

import java.io.File;

public class ImageAIFieldFactory extends AbstractFieldFactory<File, ImageAIFieldDefinition> {
    public ImageAIFieldFactory(ImageAIFieldDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
    }

    @Override
    protected Component createFieldComponent() {
        return componentProvider.newInstance(ImageAIField.class, getDefinition());
    }
}
