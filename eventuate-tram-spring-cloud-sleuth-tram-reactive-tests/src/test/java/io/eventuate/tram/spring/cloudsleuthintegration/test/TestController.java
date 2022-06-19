package io.eventuate.tram.spring.cloudsleuthintegration.test;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController()
public class TestController {

  @Autowired
  private ReactiveMessageProducer messageProducer;

  @PostMapping(path= "/foo/{id}")
  public Mono<String> sendSomething(@RequestBody TestMessage message, @PathVariable String id) {
    Message message1 = MessageBuilder.withPayload(JSonMapper.toJson(message)).build();
    return messageProducer.send("testChannel", message1).map(Message::getId);
  }

  @PostMapping(path= "/bar")
  public Mono<String> sendSomethingElse(@RequestBody String message) {
    return Mono.just(message);
  }


}
