package org.formentor.magnolia.ai.ui.field;

import lombok.Data;
import org.formentor.magnolia.ai.domain.PropertyPromptValue;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromptGeneratorDefinition {
    private String template;
    private List<PropertyPromptValue> properties = new ArrayList<>();
}
