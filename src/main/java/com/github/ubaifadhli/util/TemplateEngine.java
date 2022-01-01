package com.github.ubaifadhli.util;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class TemplateEngine {
    public static String generateReport(Map<String, Object> context) throws IOException {
        PebbleEngine engine = new PebbleEngine.Builder().build();
        PebbleTemplate compiledTemplate = engine.getTemplate(FilePath.TEMPLATE_FILE_NAME);

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, context);

        return writer.toString();
    }
}
