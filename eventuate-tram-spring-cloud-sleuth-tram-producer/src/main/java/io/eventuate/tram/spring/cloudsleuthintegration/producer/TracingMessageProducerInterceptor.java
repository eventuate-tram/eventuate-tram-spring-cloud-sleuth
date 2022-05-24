package io.eventuate.tram.spring.cloudsleuthintegration.producer;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.spring.cloudsleuthintegration.MessageHeaderAccessor;
import io.eventuate.tram.spring.cloudsleuthintegration.MessageHeaderPropagation;
import io.eventuate.tram.spring.cloudsleuthintegration.SpanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TracingMessageProducerInterceptor implements MessageInterceptor {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final SpanHelper spanHelper;

  public TracingMessageProducerInterceptor(SpanHelper spanHelper) {
    this.spanHelper = spanHelper;
  }

  @Override
  public void preSend(Message message) {
    logger.debug("preSend {}", message.getHeaders());
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
    this.spanHelper.threadLocalSpan.get().getSpan().tag("message.ID", message.getId());
    spanHelper.finishSpan(e);
  }

}
