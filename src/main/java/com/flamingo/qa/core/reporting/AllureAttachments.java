package com.flamingo.qa.core.reporting;

import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;

public final class AllureAttachments {

    private AllureAttachments() {
    }

    public static void attachText(String name, String text) {
        Allure.addAttachment(name, "text/plain", text == null ? "" : text);
    }

    public static void attachPng(String name, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), ".png");
    }

}
