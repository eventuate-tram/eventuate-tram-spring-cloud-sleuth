package io.eventuate.tram.spring.cloudsleuthintegration.test;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

public class OpenZipkinTraceDeserializer {

  private static final ObjectMapper objectMapper = JsonMapper.builder()
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .build();

  static List<List<ZipkinSpan>> deserializeTraces(String jsonString) {
    return objectMapper.readValue(jsonString, new TypeReference<List<List<ZipkinSpan>>>() { });
  }

  static List<ZipkinSpan> deserializeTrace(String jsonString) {
    return objectMapper.readValue(jsonString, new TypeReference<List<ZipkinSpan>>() { });
  }
}
