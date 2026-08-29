package io.eventuate.tram.spring.cloudsleuthintegration.test;

import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
      public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
      }
  }

  @Value("${test.zipkin.baseUrl}")
  private String zipkinBaseUrl;

  @LocalServerPort
  private int port;

  @Autowired
  private RestTemplate restTemplate;

  @Container
  static private final GenericContainer<?> zipkin = new GenericContainer<>(DockerImageName.parse("openzipkin/zipkin:3"))
          .withExposedPorts(9411);

  @DynamicPropertySource
  static void zipkinProperties(DynamicPropertyRegistry registry) {
    registry.add("test.zipkin.baseUrl", () -> String.format("http://%s:%s/", zipkin.getHost(), zipkin.getFirstMappedPort()));
    registry.add("management.zipkin.tracing.endpoint",
            () -> String.format("http://%s:%s/api/v2/spans", zipkin.getHost(), zipkin.getFirstMappedPort()));
  }

  @Test
  public void shouldImplementTracing() {
    String id = Long.toString(System.currentTimeMillis());
    ResponseEntity<String> result = restTemplate.postForEntity(String.format("http://localhost:%s/foo/%s",
            port, id),
            new TestMessage(port), String.class);
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

    String traceId = result.getBody();

    Eventually.eventually(() -> assertTracesSendToZipkin(traceId));
  }

  private void assertTracesSendToZipkin(String traceId)  {

    String url = String.format("%sapi/v2/trace/%s", zipkinBaseUrl, traceId);

    logger.debug("Retrieving trace {}", url);

    ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);

    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

    String jsonString = result.getBody();

    List<ZipkinSpan> trace = OpenZipkinTraceDeserializer.deserializeTrace(jsonString);

    ZipkinSpan parentSpan = findRequiredServerSpanByUriTag(trace, TestController.FOO_URI_TEMPLATE);
    ZipkinSpan sendSpan = findRequiredSpanByName(trace, "dosend testchannel");
    ZipkinSpan receiveSpan = findRequiredSpanByName(trace, "receive testchannel");
    ZipkinSpan barPostSpan = findRequiredClientSpanByUriTag(trace, TestController.barUrl(port));

    assertChildOf(parentSpan, sendSpan);
    assertChildOf(sendSpan, receiveSpan);
    assertChildOf(receiveSpan, barPostSpan);

  }

  private ZipkinSpan findRequiredSpanByName(List<ZipkinSpan> trace, String name) {
    return findRequiredSpan(trace, s -> s.hasName(name));
  }

  private ZipkinSpan findRequiredServerSpanByUriTag(List<ZipkinSpan> trace, String uri) {
    return findRequiredSpan(trace, s -> s.isServer() && s.hasTag("uri", uri));
  }

  private ZipkinSpan findRequiredClientSpanByUriTag(List<ZipkinSpan> trace, String uri) {
    return findRequiredSpan(trace, s -> s.isClient() && s.hasTag("uri", uri));
  }

  private void assertChildOf(ZipkinSpan parent, ZipkinSpan span) {
    if (!parent.isChild(span)) {
      Assertions.fail(String.format("Expected the parent of %s to be %s but is %s", span, parent.getId(), span
              .getParentId()));
    }
  }

  private ZipkinSpan findRequiredSpan(List<ZipkinSpan> trace, Predicate<ZipkinSpan> predicate) {
    return trace.stream().filter(predicate).findFirst().orElseThrow(() -> new RuntimeException("Span not found"));
  }

}
