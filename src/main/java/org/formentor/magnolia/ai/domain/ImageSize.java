package org.formentor.magnolia.ai.domain;

public enum ImageSize {
    Size256 ("256x256"),
    Size512 ("512x512"),
    Size1024 ("1024x1024"),
    Size1792 ("1792x1024"),
    Size1024_1792 ("1024x1792");

    public final String value;
    ImageSize (String value) {
        this.value = value;
    }
}
