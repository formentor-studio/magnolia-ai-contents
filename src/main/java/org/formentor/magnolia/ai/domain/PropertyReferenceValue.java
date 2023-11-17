package org.formentor.magnolia.ai.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyReferenceValue {
    public PropertyReferenceValue() {

    }
    private String targetWorkspace;
    private String targetPropertyName;
}
