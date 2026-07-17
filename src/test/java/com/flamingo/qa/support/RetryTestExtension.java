package com.flamingo.qa.support;

import com.flamingo.qa.config.FrameworkConfig;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class RetryTestExtension implements InvocationInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryTestExtension.class);

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) throws Throwable {
        int maxRetries = Math.max(0, FrameworkConfig.getInt("retry.max.attempts"));
        Throwable lastFailure = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                invocation.proceed();
                return;
            } catch (Throwable failure) {
                lastFailure = failure;
                if (attempt >= maxRetries) {
                    throw failure;
                }
                LOGGER.warn("Retrying failed test '{}' after attempt {}/{}",
                        extensionContext.getDisplayName(), attempt + 1, maxRetries + 1, failure);
            }
        }

        throw lastFailure;
    }
}
