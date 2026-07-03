package com.tma.job_fusion_backend.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tma.job_fusion_backend.pojo.dtos.FeatureDto;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    private static final String FEATURES = "features";
    private static final String KEY = "key";
    private static final String STATUS = "status";

    public static JsonNode convertFeaturesToJson(List<FeatureDto> features) {
        if (ObjectUtils.isEmpty(features)) {
            return null;
        }
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode rootNode = factory.objectNode();
        ArrayNode featuresArray = factory.arrayNode();
        for (FeatureDto featureDto : features) {
            ObjectNode featureNode = factory.objectNode();
            featureNode.put(KEY, featureDto.getKey());
            featureNode.put(STATUS, featureDto.getStatus());
            featuresArray.add(featureNode);
        }
        rootNode.set(FEATURES, featuresArray);
        return rootNode;
    }

    public static String getStringValue(JsonNode node, String fieldName) {
        if (ObjectUtils.isEmpty(node) || node.isMissingNode()) {
            return "";
        }
        return node.path(fieldName).asText("");
    }

    public static List<FeatureDto> convertJsonToFeatures(JsonNode featureJson) {
        if (ObjectUtils.isEmpty(featureJson) || !featureJson.has(FEATURES)) {
            return new ArrayList<>();
        }
        JsonNode featuresNode = featureJson.get(FEATURES);
        if (ObjectUtils.isEmpty(featuresNode) || !featuresNode.isArray()) {
            return new ArrayList<>();
        }
        List<FeatureDto> featuresList = new ArrayList<>();
        for (JsonNode node : featuresNode) {
            String key = getStringValue(node, KEY);
            String status = getStringValue(node, STATUS);
            featuresList.add(new FeatureDto(key, status));
        }
        return featuresList;
    }
}
