package com.rbkmoney.midgard.adapter.mts.utils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/** Утилитный класс для генерации объектов с использованием velocity */
public final class VelocityUtil {

    /**
     * Создание списка объектов согласно заданному шаблону и контекстам
     *
     * @param templateDir шаблон
     * @param contexts список контекстов
     * @return список сгенерированных объектов
     */
    public static List<String> create(String templateDir, List<VelocityContext> contexts) {
        VelocityEngine engine = getEngine();
        Template template = engine.getTemplate(templateDir);
        List<String> structures = new ArrayList<>();
        for (VelocityContext context : contexts) {
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            structures.add(writer.toString());
        }
        return structures;
    }

    /**
     * Создание списка объектов согласно заданному шаблону и контексту
     *
     * @param templateDir шаблон
     * @param context контекст
     * @return сгенерированный объект
     */
    public static String create(String templateDir, VelocityContext context) {
        VelocityEngine engine = getEngine();
        Template template = engine.getTemplate(templateDir);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    /**
     * Создание velocity движка
     *
     * @return экземпляр velocity engine
     */
    private static VelocityEngine getEngine() {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty("resource.loader", "class");
        String resourceLoader = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
        engine.setProperty("class.resource.loader.class", resourceLoader);
        engine.init();
        return engine;
    }

    private VelocityUtil() {}

}
