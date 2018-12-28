package com.rbkmoney.midgard.adapter.mts.utils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

// TODO: подумать насколько правильно держать это на статике
public final class VelocityUtil {

    private static VelocityEngine engine;

    public static List<String> create(String templateDir, List<VelocityContext> contexts) {
        VelocityEngine engine = getEngine();
        Template template = engine.getTemplate(templateDir);
        return contexts.stream()
                .map(context -> getStructure(template, context))
                .collect(Collectors.toList());
    }

    public static String create(String templateDir, VelocityContext context) {
        VelocityEngine engine = getEngine();
        Template template = engine.getTemplate(templateDir);
        return getStructure(template, context);
    }

    private static String getStructure(Template template, VelocityContext context) {
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    private static VelocityEngine getEngine() {
        if (engine == null) {
            engine = new VelocityEngine();
            engine.setProperty("resource.loader", "class");
            String resourceLoader = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
            engine.setProperty("class.resource.loader.class", resourceLoader);
            engine.init();
        }
        return engine;
    }

    private VelocityUtil() {}

}
