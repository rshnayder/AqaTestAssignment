package com.flamingo.qa.support;

import com.flamingo.qa.support.logging.TestLogBuffer;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class TestLoggingExtension implements BeforeAllCallback, BeforeEachCallback, TestWatcher, AfterAllCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestLoggingExtension.class);
    private static final String CLASS_START_TIME = "classStartTime";
    private static final String START_TIME = "startTime";
    private static final String TEST_ID = "testId";
    private static final String LINE =
            "================================================================================";

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getStore(namespace(context)).put(CLASS_START_TIME, System.nanoTime());
        LOGGER.info("{}{}{}[ TEST CLASS START ] {}{}{}",
                System.lineSeparator(), LINE, System.lineSeparator(), context.getRequiredTestClass().getName(),
                System.lineSeparator(), LINE);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        var testName = context.getRequiredTestClass().getSimpleName()
                + "." + context.getRequiredTestMethod().getName();
        var testId = testName + "-" + Integer.toHexString(context.getUniqueId().hashCode());

        ThreadContext.put("testId", sanitize(testId));
        ThreadContext.put("testName", testName);
        ThreadContext.put("testDisplayName", context.getDisplayName());
        ThreadContext.put("testClass", context.getRequiredTestClass().getName());
        ThreadContext.put("testMethod", context.getRequiredTestMethod().getName());

        context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
                .put(START_TIME, System.nanoTime());
        context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
                .put(TEST_ID, ThreadContext.get("testId"));
        TestLogBuffer.clear(ThreadContext.get("testId"));

        LOGGER.info("{}{}{}[ TEST START ] {}{}{}{}",
                System.lineSeparator(), LINE, System.lineSeparator(), context.getDisplayName(),
                System.lineSeparator(), LINE, System.lineSeparator());
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        LOGGER.info("{}{}{}[ TEST PASS ] {} ({} ms){}{}{}",
                System.lineSeparator(), LINE, System.lineSeparator(), context.getDisplayName(), elapsedMs(context),
                System.lineSeparator(), LINE, System.lineSeparator());
        attachBufferedLogs(context);
        ThreadContext.clearMap();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        LOGGER.error("{}{}{}[ TEST FAIL ] {} ({} ms){}Reason: {}{}{}{}",
                System.lineSeparator(), LINE, System.lineSeparator(), context.getDisplayName(), elapsedMs(context),
                System.lineSeparator(), cause.getMessage(), System.lineSeparator(), LINE, System.lineSeparator(),
                cause);
        attachBufferedLogs(context);
        ThreadContext.clearMap();
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        LOGGER.warn("{}{}{}[ TEST SKIPPED ] {} ({} ms){}Reason: {}{}{}{}",
                System.lineSeparator(), LINE, System.lineSeparator(), context.getDisplayName(), elapsedMs(context),
                System.lineSeparator(), cause.getMessage(), System.lineSeparator(), LINE, System.lineSeparator());
        attachBufferedLogs(context);
        ThreadContext.clearMap();
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        LOGGER.warn("{}{}{}[ TEST SKIPPED ] {}{}Reason: {}{}{}{}",
                System.lineSeparator(), LINE, System.lineSeparator(), context.getDisplayName(),
                System.lineSeparator(), reason.orElse("<not provided>"), System.lineSeparator(), LINE,
                System.lineSeparator());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        LOGGER.info("{}{}{}[ TEST CLASS END ] {} ({} ms){}{}",
                System.lineSeparator(), LINE, System.lineSeparator(), context.getRequiredTestClass().getName(),
                elapsedClassMs(context), System.lineSeparator(), LINE);
    }

    private long elapsedMs(ExtensionContext context) {
        Long startTime = context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
                .get(START_TIME, Long.class);
        if (startTime == null) {
            return 0;
        }
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private long elapsedClassMs(ExtensionContext context) {
        Long startTime = context.getStore(namespace(context)).get(CLASS_START_TIME, Long.class);
        if (startTime == null) {
            return 0;
        }
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private void attachBufferedLogs(ExtensionContext context) {
        String testId = context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
                .get(TEST_ID, String.class);
        String logs = TestLogBuffer.drain(testId);
        if (logs.isBlank()) {
            logs = "No framework log events were captured for " + context.getDisplayName();
        }
        Allure.addAttachment("Framework log", "text/plain",
                new java.io.ByteArrayInputStream(logs.getBytes(StandardCharsets.UTF_8)), ".log");
    }

    private String sanitize(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private ExtensionContext.Namespace namespace(ExtensionContext context) {
        return ExtensionContext.Namespace.create(getClass(), context.getRequiredTestClass());
    }
}
