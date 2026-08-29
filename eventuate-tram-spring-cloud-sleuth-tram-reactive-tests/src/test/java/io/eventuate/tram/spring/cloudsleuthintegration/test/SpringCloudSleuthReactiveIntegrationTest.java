package io.eventuate.tram.spring.cloudsleuthintegration.test;

import io.eventuate.tram.spring.events.publisher.ReactiveTramEventsPublisherConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import io.eventuate.tram.spring.reactive.consumer.common.ReactiveTramConsumerCommonConfiguration;
import io.eventuate.tram.spring.reactive.consumer.kafka.EventuateTramReactiveKafkaMessageConsumerConfiguration;
import io.eventuate.tram.spring.reactive.events.subscriber.ReactiveTramEventSubscriberConfiguration;
import io.eventuate.util.test.async.Eventually;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.netty.http.client.HttpClient;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

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
      public WebClient webClient(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);

        return webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
      }

      @Bean
      public RestTemplate restTemplate() {
        return new RestTemplate();
      }
  }

  @Value("${test.zipkin.baseUrl}")
  private String zipkinBaseUrl;

  @LocalServerPort
  private int port;

  @Autowired
  private RestTemplate restTemplate;

  @Container
  static private final ComposeContainer zipkin = new ComposeContainer(new File("../docker-compose.yml"))
  .withEnv("EVENTUATE_COMMON_VERSION", System.getProperty("eventuateCommonImageVersion"))
  .withEnv("EVENTUATE_MESSAGING_KAFKA_IMAGE_VERSION", System.getProperty("eventuateMessagingKafkaImageVersion"))
  .withEnv("EVENTUATE_CDC_VERSION", System.getProperty("eventuateCdcImageVersion"))
  .withEnv("EVENTUATE_CDC_KAFKA_ENABLE_BATCH_PROCESSING", System.getProperty("eventuateCdcKafkaEnableBatchProcessing"))
  .withExposedService("mysql-1", 3306, new DockerHealthcheckWaitStrategy())
  .withExposedService("kafka-1", 9092)
  .withExposedService("zookeeper-1", 2181)
  ;


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


    logger.info("jsonString={}", jsonString);
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
