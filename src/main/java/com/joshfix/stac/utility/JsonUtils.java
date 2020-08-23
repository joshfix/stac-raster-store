package com.joshfix.stac.utility;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;


public class JsonUtils {
    private static ObjectMapper objectMapper;
    private final static Logger LOGGER = Logger.getLogger(JsonUtils.class.getName());

    public JsonUtils() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] toJson(Object obj) throws StacException {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (Exception ex) {
            LOGGER.severe("Error converting object to Json string: " + ex);
            throw new StacException("Error converting object to Json string");
        }
    }

    public Map fromJson(String itemString) {
        try {
            return objectMapper.readValue(itemString, Map.class);
        } catch (IOException e) {
            LOGGER.severe("Error deserializing hit for item: " + itemString);
        }
        return null;
    }

    public Map itemCollectionFromJson(byte[] itemCollectionBytes) {
        try {
            return objectMapper.readValue(itemCollectionBytes, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map fromJsonBytes(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

