package io.eventuate.tram.spring.cloudsleuthintegration.test;

import io.eventuate.tram.spring.events.publisher.ReactiveTramEventsPublisherConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import io.eventuate.tram.spring.reactive.consumer.common.ReactiveTramConsumerCommonConfiguration;
import io.eventuate.tram.spring.reactive.consumer.kafka.EventuateTramReactiveKafkaMessageConsumerConfiguration;
import io.eventuate.tram.spring.reactive.events.subscriber.ReactiveTramEventSubscriberConfiguration;
import io.eventuate.util.test.async.Eventually;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.netty.http.client.HttpClient;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes= SpringCloudSleuthReactiveIntegrationTest.TestConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class SpringCloudSleuthReactiveIntegrationTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Configuration
  @EnableAutoConfiguration
  @ComponentScan
  @Import({ReactiveTramMessageProducerJdbcConfiguration.class,
          ReactiveTramEventsPublisherConfiguration.class,
          ReactiveTramEventSubscriberConfiguration.class,
          ReactiveTramConsumerCommonConfiguration.class,
          EventuateTramReactiveKafkaMessageConsumerConfiguration.class
  })

  static class TestConfiguration {

      @Bean
      public WebClient webClient() {
        HttpClient httpClient = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);

        WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        return webClient;
      }

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
  static private final DockerComposeContainer<?> zipkin = new DockerComposeContainer<>(new File("../docker-compose.yml"))
  .withEnv("EVENTUATE_COMMON_VERSION", System.getProperty("eventuateCommonImageVersion"))
  .withEnv("EVENTUATE_MESSAGING_KAFKA_IMAGE_VERSION", System.getProperty("eventuateMessagingKafkaImageVersion"))
  .withEnv("EVENTUATE_CDC_VERSION", System.getProperty("eventuateCdcImageVersion"))
  .withEnv("EVENTUATE_CDC_KAFKA_ENABLE_BATCH_PROCESSING", System.getProperty("eventuateCdcKafkaEnableBatchProcessing"))
  .withExposedService("mysql_1", 3306, new DockerHealthcheckWaitStrategy())
  .withExposedService("kafka", 9092)
  .withExposedService("zookeeper_1", 2181)
  ;


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


    logger.info("jsonString={}", jsonString);
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
