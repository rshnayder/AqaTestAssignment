package com.flamingo.qa.ui;

import com.flamingo.qa.support.RetryTestExtension;
import com.flamingo.qa.support.ScreenshotOnFailureExtension;
import com.flamingo.qa.support.TestLoggingExtension;
import com.flamingo.qa.ui.core.PlaywrightSession;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("ui")
@ExtendWith({TestLoggingExtension.class, RetryTestExtension.class, ScreenshotOnFailureExtension.class})
public abstract class UiTestBase {

    @BeforeEach
    void startBrowser() {
        PlaywrightSession.start();
    }

    @AfterEach
    void stopBrowser() {
        PlaywrightSession.stop();
    }

    protected Page page() {
        return PlaywrightSession.page();
    }
}
