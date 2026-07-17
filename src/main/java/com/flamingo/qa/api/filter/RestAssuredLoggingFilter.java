package com.flamingo.qa.api.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flamingo.qa.core.jackson.ObjectMapperFactory;
import com.flamingo.qa.core.logging.StepLogger;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class RestAssuredLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestAssuredLoggingFilter.class);
    private static final String LINE = "----------------------------------------------------------------";
    private static final String EMPTY = "<empty>";
    private static final String NONE = "<none>";
    private static final String MASK = "***MASKED***";
    private static final int MAX_BODY_LENGTH = 12_000;

    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext context
    ) {
        ApiRequestLog request = ApiRequestLog.from(requestSpec);
        long startedAt = System.nanoTime();

        Response response = context.next(requestSpec, responseSpec);

        ApiResponseLog apiResponse = ApiResponseLog.from(response, elapsedMs(startedAt));
        String exchange = formatExchange(request, apiResponse);

        LOGGER.info("API exchange completed: method={} uri={} status={} timeMs={}{}{}",
                request.method, request.uri, apiResponse.statusCode, apiResponse.elapsedMs,
                System.lineSeparator(), exchange);
        StepLogger.attachment(LOGGER, request.method + " " + request.uri, exchange, "text/plain", ".http");
        return response;
    }

    private static long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String formatExchange(ApiRequestLog request, ApiResponseLog response) {
        return section("API REQUEST")
                .append(request.method).append(' ').append(request.uri).append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(formatBlock("Headers", formatHeaders(request.headers)))
                .append(formatBlock("Body", renderBody(request.body)))
                .append(section("API RESPONSE"))
                .append("Status: ").append(response.statusCode).append(System.lineSeparator())
                .append("Time: ").append(response.elapsedMs).append(" ms").append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(formatBlock("Headers", formatHeaders(response.headers)))
                .append(formatBlock("Body", renderJsonOrText(response.body)))
                .append(LINE).append(System.lineSeparator())
                .toString();
    }

    private StringBuilder section(String title) {
        return new StringBuilder()
                .append(LINE).append(System.lineSeparator())
                .append("[ ").append(title).append(" ]").append(System.lineSeparator())
                .append(LINE).append(System.lineSeparator());
    }

    private String formatBlock(String title, String content) {
        return new StringBuilder()
                .append(title).append(System.lineSeparator())
                .append(indent(normalizeBlockContent(content))).append(System.lineSeparator())
                .append(System.lineSeparator())
                .toString();
    }

    private String formatHeaders(Headers headers) {
        if (headers == null || headers.asList().isEmpty()) {
            return NONE;
        }

        StringBuilder formatted = new StringBuilder();
        for (Header header : headers) {
            formatted.append(header.getName())
                    .append(": ")
                    .append(maskHeaderValue(header))
                    .append(System.lineSeparator());
        }
        return trimTrailingLineBreak(formatted.toString());
    }

    private String renderBody(Object body) {
        if (body == null) {
            return EMPTY;
        }
        if (body instanceof String) {
            return renderJsonOrText((String) body);
        }
        try {
            return renderJsonOrText(ObjectMapperFactory.mapper().writeValueAsString(body));
        } catch (JsonProcessingException ignored) {
            return limitBody(String.valueOf(body));
        }
    }

    private String renderJsonOrText(String body) {
        if (body == null || body.isBlank()) {
            return EMPTY;
        }
        try {
            JsonNode json = ObjectMapperFactory.mapper().readTree(body);
            redactSensitiveFields(json);
            return limitBody(ObjectMapperFactory.mapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(json));
        } catch (Exception ignored) {
            return limitBody(body);
        }
    }

    private void redactSensitiveFields(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                if (isSensitive(fieldName)) {
                    objectNode.put(fieldName, MASK);
                } else {
                    redactSensitiveFields(objectNode.get(fieldName));
                }
            });
            return;
        }
        if (node.isArray()) {
            node.forEach(this::redactSensitiveFields);
        }
    }

    private String maskHeaderValue(Header header) {
        if (header == null) {
            return "";
        }
        if (isSensitive(header.getName())) {
            return MASK;
        }
        return header.getValue();
    }

    private boolean isSensitive(String name) {
        if (name == null) {
            return false;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("authorization")
                || normalized.contains("token")
                || normalized.contains("cookie")
                || normalized.contains("password")
                || normalized.contains("secret");
    }

    private String indent(String value) {
        String text = value == null || value.isBlank() ? EMPTY : value;
        return "  " + text.replace(System.lineSeparator(), System.lineSeparator() + "  ");
    }

    private String normalizeBlockContent(String content) {
        if (content == null || content.isBlank()) {
            return EMPTY;
        }
        return trimTrailingLineBreak(content);
    }

    private String limitBody(String body) {
        if (body == null || body.isBlank()) {
            return EMPTY;
        }
        String normalized = trimTrailingLineBreak(body);
        if (normalized.length() <= MAX_BODY_LENGTH) {
            return normalized;
        }
        int omittedCharacters = normalized.length() - MAX_BODY_LENGTH;
        return normalized.substring(0, MAX_BODY_LENGTH)
                + System.lineSeparator()
                + "... [truncated "
                + omittedCharacters
                + " characters]";
    }

    private String trimTrailingLineBreak(String value) {
        if (value == null) {
            return "";
        }
        int end = value.length();
        while (end > 0) {
            char last = value.charAt(end - 1);
            if (last != '\n' && last != '\r') {
                break;
            }
            end--;
        }
        return value.substring(0, end);
    }

    private static final class ApiRequestLog {
        private final String method;
        private final String uri;
        private final Headers headers;
        private final Object body;

        private ApiRequestLog(String method, String uri, Headers headers, Object body) {
            this.method = method;
            this.uri = uri;
            this.headers = headers;
            this.body = body;
        }

        private static ApiRequestLog from(FilterableRequestSpecification requestSpec) {
            return new ApiRequestLog(
                    requestSpec.getMethod(),
                    requestSpec.getURI(),
                    requestSpec.getHeaders(),
                    requestSpec.getBody()
            );
        }
    }

    private static final class ApiResponseLog {
        private final int statusCode;
        private final long elapsedMs;
        private final Headers headers;
        private final String body;

        private ApiResponseLog(int statusCode, long elapsedMs, Headers headers, String body) {
            this.statusCode = statusCode;
            this.elapsedMs = elapsedMs;
            this.headers = headers;
            this.body = body;
        }

        private static ApiResponseLog from(Response response, long elapsedMs) {
            return new ApiResponseLog(
                    response.statusCode(),
                    elapsedMs,
                    response.getHeaders(),
                    response.asString()
            );
        }
    }
}
