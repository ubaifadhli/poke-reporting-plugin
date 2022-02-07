package com.github.ubaifadhli.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JSONUtil {
    public static <T> List<T> loadMultipleJSON(String jsonDirectory, Class<T[]> arrayClassToCast) throws MojoExecutionException {
        List<T> castedObjectList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        File folder = Paths.get(jsonDirectory).toFile();
        List<File> files = folder.listFiles() != null ? Arrays.asList(folder.listFiles()) : new ArrayList<>();

        if (files.size() == 0)
            throw new MojoExecutionException("No Cucumber JSON files found in specified directory : " + jsonDirectory);


        files.forEach(
                file -> {
                    try {
                        T[] objectArray = mapper.readValue(file, arrayClassToCast);
                        castedObjectList.addAll(Arrays.asList(objectArray));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        return castedObjectList;
    }
}
