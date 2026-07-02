package com.tma.job_fusion_backend.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tma.job_fusion_backend.pojo.dtos.FeatureDto;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    public static JsonNode convertFeaturesToJson(List<FeatureDto> features) {
        if (features == null) {
            return null;
        }
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode rootNode = factory.objectNode();
        ArrayNode featuresArray = factory.arrayNode();
        for (FeatureDto featureDto : features) {
            ObjectNode featureNode = factory.objectNode();
            featureNode.put("key", featureDto.getKey());
            featureNode.put("status", featureDto.getStatus());
            featuresArray.add(featureNode);
        }
        rootNode.set("features", featuresArray);
        return rootNode;
    }

    public static String getStringValue(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode()) {
            return "";
        }
        return node.path(fieldName).asText("");
    }

    public static List<FeatureDto> convertJsonToFeatures(JsonNode featureJson) {
        if (featureJson == null || !featureJson.has("features")) {
            return new ArrayList<>();
        }
        JsonNode featuresNode = featureJson.get("features");
        if (featuresNode == null || !featuresNode.isArray()) {
            return new ArrayList<>();
        }
        List<FeatureDto> featuresList = new ArrayList<>();
        for (JsonNode node : featuresNode) {
            String key = getStringValue(node, "key");
            String status = getStringValue(node, "status");
            featuresList.add(new FeatureDto(key, status));
        }
        return featuresList;
    }
}
