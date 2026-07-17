package com.flamingo.qa.ui.core;

import com.flamingo.qa.config.FrameworkConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class PlaywrightSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaywrightSession.class);

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> BROWSER_CONSOLE_EVENTS = new ThreadLocal<>();

    private PlaywrightSession() {
    }

    public static void start() {
        if (PAGE.get() != null) {
            LOGGER.debug("Playwright session already exists for thread={}", Thread.currentThread().getName());
            return;
        }

        String browserName = FrameworkConfig.getRequired("ui.browser");
        boolean headless = FrameworkConfig.getBoolean("ui.headless");
        int viewportWidth = FrameworkConfig.getInt("ui.viewport.width");
        int viewportHeight = FrameworkConfig.getInt("ui.viewport.height");
        LOGGER.info("Starting Playwright session: browser={} headless={} viewport={}x{} thread={}",
                browserName, headless, viewportWidth, viewportHeight, Thread.currentThread().getName());

        Playwright playwright = null;
        Browser browser = null;
        BrowserContext context = null;
        Page page = null;
        try {
            playwright = Playwright.create();
            browser = browserType(playwright)
                    .launch(new BrowserType.LaunchOptions()
                            .setHeadless(headless)
                            .setSlowMo(FrameworkConfig.getLong("ui.slow.mo.ms"))
                            .setArgs(Arrays.asList("--disable-crash-reporter", "--disable-crashpad")));

            context = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(viewportWidth, viewportHeight));
            context.setDefaultTimeout(FrameworkConfig.getLong("ui.timeout.ms"));

            page = context.newPage();
            page.setDefaultTimeout(FrameworkConfig.getLong("ui.timeout.ms"));
            List<String> consoleEvents = Collections.synchronizedList(new ArrayList<>());
            Page createdPage = page;
            page.onConsoleMessage(message -> {
                var entry = message.type() + " " + createdPage.url() + " - " + message.text();
                consoleEvents.add(entry);
                if ("error".equalsIgnoreCase(message.type())) {
                    LOGGER.warn("Browser console error: {}", entry);
                } else {
                    LOGGER.debug("Browser console: {}", entry);
                }
            });

            PLAYWRIGHT.set(playwright);
            BROWSER.set(browser);
            CONTEXT.set(context);
            PAGE.set(page);
            BROWSER_CONSOLE_EVENTS.set(consoleEvents);
        } catch (RuntimeException exception) {
            closeQuietly(page);
            closeQuietly(context);
            closeQuietly(browser);
            closeQuietly(playwright);
            clearThreadLocals();
            throw exception;
        }
        LOGGER.info("Playwright session started");
    }

    public static Page page() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException("Playwright session has not been started for this thread");
        }
        return page;
    }

    public static Page currentPageOrNull() {
        return PAGE.get();
    }

    public static String browserConsoleEventsAsText() {
        List<String> events = BROWSER_CONSOLE_EVENTS.get();
        if (events == null || events.isEmpty()) {
            return "No browser console events were captured.";
        }
        synchronized (events) {
            return events.stream().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    public static void stop() {
        LOGGER.info("Stopping Playwright session");
        closeQuietly(PAGE.get());
        closeQuietly(CONTEXT.get());
        closeQuietly(BROWSER.get());
        closeQuietly(PLAYWRIGHT.get());

        clearThreadLocals();
        LOGGER.info("Playwright session stopped");
    }

    private static void clearThreadLocals() {
        PAGE.remove();
        CONTEXT.remove();
        BROWSER.remove();
        PLAYWRIGHT.remove();
        BROWSER_CONSOLE_EVENTS.remove();
    }

    private static BrowserType browserType(Playwright playwright) {
        String browser = FrameworkConfig.getRequired("ui.browser").toLowerCase();
        switch (browser) {
            case "firefox":
                return playwright.firefox();
            case "webkit":
                return playwright.webkit();
            case "chromium":
            default:
                return playwright.chromium();
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception exception) {
            // Cleanup must not hide the original test failure.
            LOGGER.debug("Unable to close Playwright resource", exception);
        }
    }
}
