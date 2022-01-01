package com.github.ubaifadhli.exceptions;

import com.github.ubaifadhli.util.FilePath;
import org.apache.maven.plugin.MojoExecutionException;

public class CucumberFileNotFoundException extends MojoExecutionException {
    public CucumberFileNotFoundException(Throwable cause) {
        super("Cucumber JSON could not be loaded. Please make sure that the JSON exist in " + FilePath.DEFAULT_CUCUMBER_PATH, cause);
    }
}
