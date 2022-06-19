package io.eventuate.tram.spring.cloudsleuthintegration.test;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Component
public class TestConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private ReactiveMessageConsumer messageConsumer;

  @Autowired
  private WebClient restTemplate;

  @PostConstruct
  public void initialize () {
    messageConsumer.subscribe(TestConsumer.class.getName(),
            Collections.singleton("testChannel"), this::messageHandler);
  }

  private Mono<?> messageHandler(Message message) {
    logger.debug("received message {}" , message);
    TestMessage testMessage = JSonMapper.fromJson(message.getPayload(), TestMessage.class);

    return restTemplate.post().uri(String.format("http://localhost:%s/bar", testMessage
                    .getPort()))
            .exchange();
  }
}
