package com.tma.job_fusion_backend.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class Jackson3SupportConfig {

    private static final tools.jackson.databind.ObjectMapper JACKSON3_MAPPER = new tools.jackson.databind.ObjectMapper();

    @Bean
    public com.fasterxml.jackson.databind.Module jackson3Module() {
        SimpleModule module = new SimpleModule("Jackson3SupportModule");

        // Serializer for tools.jackson.databind.JsonNode using Jackson 2 JsonGenerator
        module.addSerializer(tools.jackson.databind.JsonNode.class, new JsonSerializer<tools.jackson.databind.JsonNode>() {
            @Override
            public void serialize(tools.jackson.databind.JsonNode value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    String json = JACKSON3_MAPPER.writeValueAsString(value);
                    gen.writeRawValue(json);
                }
            }
        });

        // Deserializer for tools.jackson.databind.JsonNode using Jackson 2 JsonParser
        module.addDeserializer(tools.jackson.databind.JsonNode.class, new JsonDeserializer<tools.jackson.databind.JsonNode>() {
            @Override
            public tools.jackson.databind.JsonNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                JsonNode jackson2Node = p.readValueAsTree();
                if (jackson2Node == null) {
                    return null;
                }
                String json = jackson2Node.toString();
                return JACKSON3_MAPPER.readTree(json);
            }
        });

        return module;
    }
}
