package org.formentor.magnolia.ai.domain.valueObject;

/**
 * Specifies path and name of a property
 * - e.g. destination/address/number means node path "destination/address" and property name "number" -
 */
public class PropertyPathValue {
    final String value;

    public static PropertyPathValue fromString(String value) {
        return new PropertyPathValue(value);
    }

    public PropertyPathValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public boolean isPath() {
        return value.contains("/");
    }

    public String getNodePath() {
        return value.substring(0, value.lastIndexOf("/"));
    }

    public String getPropertyName() {
        return isPath()? value.substring(value.lastIndexOf("/") + 1): value;
    }

    public String toString() {
        return value;
    }
}
