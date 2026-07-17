package com.flamingo.qa.support.data;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flamingo.qa.core.jackson.ObjectMapperFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public class JsonFileArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<JsonFileSource> {

    private final ObjectMapper mapper = ObjectMapperFactory.mapper();
    private JsonFileSource source;

    @Override
    public void accept(JsonFileSource source) {
        this.source = source;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        try (InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(source.value())) {
            if (stream == null) {
                throw new IllegalStateException("JSON test data was not found: " + source.value());
            }
            JavaType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, source.type());
            List<?> values = mapper.readValue(stream, listType);
            return values.stream().map(Arguments::of);
        }
    }
}
