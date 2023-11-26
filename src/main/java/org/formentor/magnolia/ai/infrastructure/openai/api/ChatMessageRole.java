package org.formentor.magnolia.ai.infrastructure.openai.api;

/**
 * see {@link ChatMessage} documentation.
 */
public enum ChatMessageRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    TOOL("tool");

    private final String value;

    ChatMessageRole(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
