package com.github.ubaifadhli.util;

import com.github.ubaifadhli.constant.FilePath;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TemplateEngine {
    public static String generateReport(Map<String, Object> context) throws IOException {
        PebbleEngine engine = new PebbleEngine.Builder().build();
        PebbleTemplate compiledTemplate = engine.getTemplate(FilePath.DASHBOARD_TEMPLATE_FILE_NAME);

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, context);

        return writer.toString();
    }

    public static void generateWebpage(Object data, String templateFilename, String generatedWebpagePath) throws IOException{
        Map<String, Object> context = new HashMap<>();
        context.put("data", data);

        PebbleEngine engine = new PebbleEngine.Builder().build();
        PebbleTemplate compiledTemplate = engine.getTemplate(templateFilename);

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, context);

        String output = writer.toString();

        Path path = Paths.get(generatedWebpagePath);

        path.getParent().toFile().mkdirs();

        Files.write(path, output.getBytes(StandardCharsets.UTF_8));

        // TODO Add log linking to the dashboard.html path
        //  for easier webpage opening.
    }
}
