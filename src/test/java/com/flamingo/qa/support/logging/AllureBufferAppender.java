package com.flamingo.qa.support.logging;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Plugin(name = "AllureBuffer", category = "Core", elementType = "appender", printObject = true)
public final class AllureBufferAppender extends AbstractAppender {

    private AllureBufferAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static AllureBufferAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout
    ) {
        var appenderName = name == null || name.trim().isEmpty() ? "ALLURE_BUFFER" : name;
        var resolvedLayout = layout == null
                ? PatternLayout.newBuilder().withPattern("%d %-5p [%X{testId:-suite}] %c - %m%n%throwable").build()
                : layout;
        return new AllureBufferAppender(appenderName, filter, resolvedLayout);
    }

    @Override
    public void append(LogEvent event) {
        var immutableEvent = event.toImmutable();
        var rawTestId = immutableEvent.getContextData().getValue("testId");
        var testId = rawTestId == null ? TestLogBuffer.SUITE_LOG_ID : String.valueOf(rawTestId);
        var rendered = new String(getLayout().toByteArray(immutableEvent), StandardCharsets.UTF_8);
        TestLogBuffer.append(testId, rendered);
    }
}
