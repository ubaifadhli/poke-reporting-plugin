package com.github.ubaifadhli.constant;

import org.apache.maven.plugin.MojoExecutionException;

public enum SupportedMIMEType {
    PLAIN_TEXT("text/plain"),
    IMAGE_PNG("image/png");

    public final String mimeType;

    SupportedMIMEType(String name) {
        this.mimeType = name;
    }

    public static SupportedMIMEType valueOfMIMEType(String mimeType) throws MojoExecutionException{
        for (SupportedMIMEType supportedMIMEType : SupportedMIMEType.values())
            if (mimeType.equals(supportedMIMEType.mimeType))
                return supportedMIMEType;

        throw new MojoExecutionException("Specified MIME type is currently not supported : " + mimeType);
    }

    String getName() {
        return this.mimeType;
    }
}
