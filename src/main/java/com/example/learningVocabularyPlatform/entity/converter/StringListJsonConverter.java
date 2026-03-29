package com.example.learningVocabularyPlatform.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return null;
        }
        if (attribute.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < attribute.size(); i++) {
            String value = attribute.get(i) == null ? "" : attribute.get(i);
            String encoded = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
            if (i > 0) {
                json.append(',');
            }
            json.append('"').append(encoded).append('"');
        }
        json.append(']');
        return json.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }

        String normalized = dbData.trim();
        if ("[]".equals(normalized)) {
            return new ArrayList<>();
        }

        if (!normalized.startsWith("[") || !normalized.endsWith("]")) {
            throw new IllegalArgumentException("Invalid options format in database");
        }

        String inner = normalized.substring(1, normalized.length() - 1).trim();
        List<String> result = new ArrayList<>();
        if (inner.isEmpty()) {
            return result;
        }

        String[] tokens = inner.split(",");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                String encoded = trimmed.substring(1, trimmed.length() - 1);
                byte[] decoded = Base64.getDecoder().decode(encoded);
                result.add(new String(decoded, StandardCharsets.UTF_8));
            }
        }
        return result;
    }
}
