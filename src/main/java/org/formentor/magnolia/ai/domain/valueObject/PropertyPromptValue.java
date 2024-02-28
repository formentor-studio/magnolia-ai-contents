package org.formentor.magnolia.ai.domain.valueObject;

import lombok.Getter;
import lombok.Setter;
import org.formentor.magnolia.ai.domain.valueObject.PropertyPathValue;

/**
 * Definition of a value used to generate a prompt
 */
@Getter
@Setter
public class PropertyPromptValue {
    public PropertyPromptValue() {

    }

    private String name;
    private Integer limit;
    private String targetWorkspace;
    private String targetPropertyName;

    public boolean isReference() {
        return (targetWorkspace != null && targetPropertyName != null);
    }

    public PropertyPathValue getPropertyPath() {
        return PropertyPathValue.fromString(name);
    }
}
