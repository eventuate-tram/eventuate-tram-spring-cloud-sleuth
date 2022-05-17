package io.eventuate.tram.spring.cloudsleuthintegration;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import io.eventuate.tram.consumer.common.MessageHandlerDecoratorChain;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.springframework.cloud.sleuth.Span;

import java.util.HashMap;
import java.util.Map;

public class TracingMessagingInterceptor implements MessageInterceptor, MessageHandlerDecorator {

  private final SpanHelper spanHelper;

  TracingMessagingInterceptor(SpanHelper spanHelper) {
    this.spanHelper = spanHelper;
  }

  @Override
  public void preSend(Message message) {
    spanHelper.logger.info("preSend {}", message.getHeaders());
    MessageHeaderAccessor headers = spanHelper.makeMessageHeaderAccessor(message);
    org.springframework.cloud.sleuth.Span span = spanHelper.nextSpan(spann -> {
      spann.name("doSend " + message.getRequiredHeader(Message.DESTINATION));
      spann.tag("message.operation", "PRODUCE");
      addMessageTags(spann, message);
    });
    MessageHeaderPropagation.removeAnyTraceHeaders(headers, this.spanHelper.tracing.propagation().keys());
    spanHelper.propagator.inject(span.context(), headers, spanHelper.setter);
  }

  private void addMessageTags(org.springframework.cloud.sleuth.Span span, Message message) {
    Map<String, String> copy = new HashMap<>(message.getHeaders());
    MessageHeaderPropagation.removeAnyTraceHeaders(new SpanHelper.MessageHeaderMapAccessor(copy), this.spanHelper.tracing.propagation().keys());
    copy.forEach((key, value) -> span.tag("message." + key, value));
  }


  @Override
  public void postSend(Message message, Exception e) {
    System.out.println("postSend()");
    this.spanHelper.threadLocalSpan.get().getSpan().tag("message.ID", message.getId());
    spanHelper.finishSpan(e);
  }


  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public void accept(SubscriberIdAndMessage subscriberIdAndMessage, MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    String subscriberId = subscriberIdAndMessage.getSubscriberId();

    Message message = subscriberIdAndMessage.getMessage();

    spanHelper.logger.info("preHandle {}", message.getHeaders());

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
      System.out.println("postHandle()");
      spanHelper.finishSpan(throwable);
    }
  }

}
