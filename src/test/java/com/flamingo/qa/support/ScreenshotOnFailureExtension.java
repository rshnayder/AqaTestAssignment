package com.flamingo.qa.support;

import com.flamingo.qa.core.reporting.AllureAttachments;
import com.flamingo.qa.ui.core.PlaywrightSession;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenshotOnFailureExtension implements TestExecutionExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotOnFailureExtension.class);

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        Page page = PlaywrightSession.currentPageOrNull();
        if (page != null) {
            LOGGER.error("UI test failed at url={}", page.url(), throwable);
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            AllureAttachments.attachPng(context.getDisplayName() + " failure screenshot", screenshot);
            AllureAttachments.attachText("Current URL", page.url());
            AllureAttachments.attachText("Browser console events", PlaywrightSession.browserConsoleEventsAsText());
        }
        throw throwable;
    }
}
