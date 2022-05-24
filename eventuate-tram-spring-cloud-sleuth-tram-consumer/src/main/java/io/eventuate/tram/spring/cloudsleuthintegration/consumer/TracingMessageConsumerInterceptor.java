package io.eventuate.tram.spring.cloudsleuthintegration.consumer;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import io.eventuate.tram.consumer.common.MessageHandlerDecoratorChain;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.eventuate.tram.spring.cloudsleuthintegration.MessageHeaderAccessor;
import io.eventuate.tram.spring.cloudsleuthintegration.MessageHeaderPropagation;
import io.eventuate.tram.spring.cloudsleuthintegration.SpanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.Span;

import java.util.HashMap;
import java.util.Map;

public class TracingMessageConsumerInterceptor implements MessageHandlerDecorator {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final SpanHelper spanHelper;

  TracingMessageConsumerInterceptor(SpanHelper spanHelper) {
    this.spanHelper = spanHelper;
  }

  private void addMessageTags(Span span, Message message) {
    Map<String, String> copy = new HashMap<>(message.getHeaders());
    MessageHeaderPropagation.removeAnyTraceHeaders(new SpanHelper.MessageHeaderMapAccessor(copy), this.spanHelper.tracing.propagation().keys());
    copy.forEach((key, value) -> span.tag("message." + key, value));
  }


  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public void accept(SubscriberIdAndMessage subscriberIdAndMessage, MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    String subscriberId = subscriberIdAndMessage.getSubscriberId();

    Message message = subscriberIdAndMessage.getMessage();

    logger.debug("preHandle {}", message.getHeaders());

    MessageHeaderAccessor headers = spanHelper.makeMessageHeaderAccessor(message);

    Span span = spanHelper.nextSpan(headers, spann -> {
      spann.name("receive " + message.getRequiredHeader(Message.DESTINATION));
      spann.tag("subscriberId", subscriberId);
      spann.tag("message.operation", "CONSUME");
      addMessageTags(spann, message);
    });

    RuntimeException throwable = null;
    try {
      messageHandlerDecoratorChain.invokeNext(subscriberIdAndMessage);
    } catch (RuntimeException e) {
      throwable = e;
      throw e;
    } finally {
      spanHelper.finishSpan(throwable);
    }
  }

}
