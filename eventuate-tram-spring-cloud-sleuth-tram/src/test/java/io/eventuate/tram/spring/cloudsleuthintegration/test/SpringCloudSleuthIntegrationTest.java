package io.eventuate.tram.spring.cloudsleuthintegration.test;

import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes=SpringCloudSleuthIntegrationTest.TestConfiguration.class, webEnvironment = SpringBootTest
        .WebEnvironment.RANDOM_PORT)
@Testcontainers
public class SpringCloudSleuthIntegrationTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Configuration
  @SpringBootApplication
  @Import(TramInMemoryConfiguration.class)
  static class TestConfiguration {

      @Bean
      public RestTemplate restTemplate() {
        return new RestTemplate();
      }
  }

  @Value("${spring.zipkin.baseUrl}")
  private String zipkinBaseUrl;

  @LocalServerPort
  private int port;

  @Autowired
  private RestTemplate restTemplate;

  @Container
  static private final GenericContainer<?> zipkin = new GenericContainer<>(DockerImageName.parse("openzipkin/zipkin:2.23"))
          .withExposedPorts(9411);

  @DynamicPropertySource
  static void zipkinProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.zipkin.baseUrl", () -> String.format("http://%s:%s/", zipkin.getHost(), zipkin.getFirstMappedPort()));
  }

  @Test
  public void shouldImplementTracing() {
    String id = Long.toString(System.currentTimeMillis());
    ResponseEntity<String> result = restTemplate.postForEntity(String.format("http://localhost:%s/foo/%s",
            port, id),
            new TestMessage(port), String.class);
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

    Eventually.eventually(() -> assertTracesSendToZipkin(id));
  }

  private void assertTracesSendToZipkin(String id)  {

    String url = String.format
            ("%sapi/v2/traces?annotationQuery=http.path=/foo/%s",
                    zipkinBaseUrl, id);

    logger.debug("Retrieving traces {}", url);

    ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);

    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

    String jsonString = result.getBody();

    List<List<ZipkinSpan>> traces = OpenZipkinTraceDeserializer.deserializeTraces(jsonString);
    assertEquals(1, traces.size());

    List<ZipkinSpan> trace = traces.get(0);
    ZipkinSpan parentSpan = findRequiredSpanByHttpPathTag(trace, "/foo/" + id);
    ZipkinSpan sendSpan = findRequiredSpanByName(trace, "dosend testchannel");
    ZipkinSpan receiveSpan = findRequiredSpanByName(trace, "receive testchannel");
    ZipkinSpan barPostSpan = findRequiredSpanByHttpPathTag(trace, "/bar");

    assertChildOf(parentSpan, sendSpan);
    assertChildOf(sendSpan, receiveSpan);
    assertChildOf(receiveSpan, barPostSpan);

  }

  @NotNull
  private ZipkinSpan findRequiredSpanByName(List<ZipkinSpan> trace, String name) {
    return findRequiredSpan(trace, s -> s.hasName(name));
  }

  @NotNull
  private ZipkinSpan findRequiredSpanByHttpPathTag(List<ZipkinSpan> trace, String path) {
    return findRequiredSpan(trace, s -> s.hasTag("http.path", path));
  }

  private void assertChildOf(ZipkinSpan parent, ZipkinSpan span) {
    if (!parent.isChild(span)) {
      Assertions.fail(String.format("Expected the parent of %s to be %s but is %s", span, parent.getId(), span
              .getParentId()));
    }
  }

  @NotNull
  private ZipkinSpan findRequiredSpan(List<ZipkinSpan> trace, Predicate<ZipkinSpan> predicate) {
    return trace.stream().filter(predicate).findFirst().orElseThrow(() -> new RuntimeException("Span not found"));
  }

}
