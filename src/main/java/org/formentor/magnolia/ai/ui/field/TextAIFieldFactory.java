package org.formentor.magnolia.ai.ui.field;

import com.vaadin.ui.Component;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.field.TextFieldDefinition;
import info.magnolia.ui.field.factory.TextFieldFactory;

/**
 * Factory for field textFieldAI
 */
public class TextAIFieldFactory extends TextFieldFactory {
    public TextAIFieldFactory(TextFieldDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
    }
    @Override
    public Component createFieldComponent() {
        return componentProvider.newInstance(TextAIField.class, getDefinition(), super.createFieldComponent());
    }
}
