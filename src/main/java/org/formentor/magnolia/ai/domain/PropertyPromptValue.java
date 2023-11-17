package org.formentor.magnolia.ai.domain;

import lombok.Getter;
import lombok.Setter;

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
}
