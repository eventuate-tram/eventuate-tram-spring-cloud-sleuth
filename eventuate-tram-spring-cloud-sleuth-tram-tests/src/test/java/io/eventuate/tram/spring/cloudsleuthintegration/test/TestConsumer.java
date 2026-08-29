package io.eventuate.tram.spring.cloudsleuthintegration.test;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
public class TestConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private MessageConsumer messageConsumer;

  @Autowired
  private RestTemplate restTemplate;

  @PostConstruct
  public void initialize () {
    messageConsumer.subscribe(TestConsumer.class.getName(), Collections.singleton("testChannel"), this::messageHandler);
  }

  private void messageHandler(Message message) {
    logger.debug("received message {}" , message);
    TestMessage testMessage = JSonMapper.fromJson(message.getPayload(), TestMessage.class);

    ResponseEntity<String> result = restTemplate.postForEntity(TestController.barUrl(testMessage.getPort()),
            "hello", String.class);

    assertEquals(HttpStatus.OK, result.getStatusCode());
  }
}
