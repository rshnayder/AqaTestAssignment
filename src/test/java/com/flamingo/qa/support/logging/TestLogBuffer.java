package com.flamingo.qa.support.logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TestLogBuffer {

    public static final String SUITE_LOG_ID = "suite";

    private static final ConcurrentMap<String, StringBuffer> LOGS = new ConcurrentHashMap<>();

    private TestLogBuffer() {
    }

    public static void append(String testId, String message) {
        LOGS.computeIfAbsent(safeTestId(testId), ignored -> new StringBuffer())
                .append(message);
    }

    public static String drain(String testId) {
        StringBuffer buffer = LOGS.remove(safeTestId(testId));
        if (buffer == null) {
            return "";
        }
        return buffer.toString();
    }

    public static void clear(String testId) {
        LOGS.remove(safeTestId(testId));
    }

    private static String safeTestId(String testId) {
        if (testId == null || testId.trim().isEmpty()) {
            return SUITE_LOG_ID;
        }
        return testId;
    }
}
