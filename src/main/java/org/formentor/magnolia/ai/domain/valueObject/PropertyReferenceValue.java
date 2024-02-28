package org.formentor.magnolia.ai.domain.valueObject;

import lombok.Getter;
import lombok.Setter;

/**
 * Definition of a property reference
 */
@Getter
@Setter
public class PropertyReferenceValue {
    public PropertyReferenceValue() {

    }
    private String targetWorkspace;
    private String targetPropertyName;
}
