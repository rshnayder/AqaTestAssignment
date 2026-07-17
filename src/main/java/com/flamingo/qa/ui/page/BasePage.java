package com.flamingo.qa.ui.page;

import com.flamingo.qa.config.FrameworkConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected void navigate(String path) {
        String url = FrameworkConfig.getRequired("ui.base.url") + path;
        page.navigate(url);
        removeKnownOverlays();
    }

    protected void fill(Locator locator, String value) {
        locator.fill(value);
    }

    protected void click(Locator locator) {
        locator.click();
    }

    protected void waitVisible(Locator locator) {
        assertThat(locator).isVisible();
    }

    protected void removeKnownOverlays() {
        page.evaluate("() => { "
                + "document.querySelectorAll('#fixedban, footer, iframe[id^=\"google_ads\"], .adsbygoogle')"
                + ".forEach(element => element.remove()); "
                + "}");
    }
}
