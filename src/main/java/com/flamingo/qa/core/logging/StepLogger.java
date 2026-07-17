package com.flamingo.qa.core.logging;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public final class StepLogger {

    private StepLogger() {
    }

    public static void step(Logger logger, String messageTemplate, Object... arguments) {
        logger.info(messageTemplate, arguments);
        Allure.step(render(messageTemplate, arguments));
    }

    public static void step(Logger logger, String messageTemplate, Runnable action, Object... arguments) {
        logger.info(messageTemplate, arguments);
        Allure.step(render(messageTemplate, arguments), action::run);
    }

    public static void debugStep(Logger logger, String messageTemplate, Runnable action, Object... arguments) {
        logger.debug(messageTemplate, arguments);
        Allure.step(render(messageTemplate, arguments), action::run);
    }

    public static void attachment(
            Logger logger,
            String attachmentName,
            String content,
            String contentType,
            String fileExtension
    ) {
        logger.info("Attach report artifact: name={} type={}", attachmentName, contentType);
        Allure.addAttachment(
                attachmentName,
                contentType,
                new ByteArrayInputStream(nullSafe(content).getBytes(StandardCharsets.UTF_8)),
                fileExtension
        );
    }

    public static String render(String messageTemplate, Object... arguments) {
        return MessageFormatter.arrayFormat(messageTemplate, arguments).getMessage();
    }

    private static String nullSafe(String content) {
        return content == null ? "" : content;
    }
}
