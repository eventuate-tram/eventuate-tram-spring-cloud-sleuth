package io.eventuate.tram.spring.cloudsleuthintegration;

import brave.Tracing;
import io.eventuate.tram.messaging.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanAndScope;
import org.springframework.cloud.sleuth.ThreadLocalSpan;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;

import java.util.Map;
import java.util.function.Consumer;

public class SpanHelper {
  Logger logger = LoggerFactory.getLogger(getClass());
  final Tracing tracing;
  Propagator propagator;
  Propagator.Setter<MessageHeaderAccessor> setter;
  Propagator.Getter<MessageHeaderAccessor> getter;
  final Tracer tracer;
  final ThreadLocalSpan threadLocalSpan;

  public SpanHelper(Tracing tracing, Propagator propagator, Propagator.Setter<MessageHeaderAccessor> setter, Propagator.Getter<MessageHeaderAccessor> getter, Tracer tracer) {
    this.tracing = tracing;
    this.propagator = propagator;
    this.setter = setter;
    this.getter = getter;
    this.tracer = tracer;
    this.threadLocalSpan = new ThreadLocalSpan(tracer);

  }

  org.springframework.cloud.sleuth.Span nextSpan(Consumer<Span> spanCustomizer) {
    SpanAndScope spanAndScope = threadLocalSpan.get();
    org.springframework.cloud.sleuth.Span currentSpan = spanAndScope != null ? spanAndScope.getSpan() : tracer.currentSpan();
    org.springframework.cloud.sleuth.Span span = tracer.nextSpan();
    threadLocalSpan.set(span);
    spanCustomizer.accept(span);
    span.start();
    logger.info("preSend newSpan {}", span);
    logger.info("preSend currentSpan {}", tracer.currentSpan());
    return span;
  }

  void finishSpan(Throwable error) {
    logger.info("finishSpan tracer.currentSpan {}", tracer.currentSpan());
    SpanAndScope spanAndScope = threadLocalSpan.get();
    Span span = spanAndScope.getSpan();
    threadLocalSpan.remove();
    logger.info("actual span {}", span);
    if (span == null)
      return;
    if (error != null) { // an error occurred, adding error to span
      span.error(error);
    }
    spanAndScope.close();
    logger.info("finishSpan currentSpan {}", tracer.currentSpan());
  }

  MessageHeaderAccessor makeMessageHeaderAccessor(Message message) {
    return new MessageHeaderAccessor() {
        @Override
        public void put(String key, String value) {
          message.setHeader(key, value);
        }

        @Override
        public String get(String key) {
          return message.getHeader(key).orElse(null);
        }

        @Override
        public void remove(String key) {
          message.removeHeader(key);
        }
      };
  }

  Span nextSpan(MessageHeaderAccessor headers, Consumer<Span> spanCustomizer) {
    Span.Builder extracted = propagator.extract(headers, getter);

    Span span = extracted.start();

    spanCustomizer.accept(span);

    threadLocalSpan.set(span);

    logger.info("preHandle newSpan {}", span);
    logger.info("preHandle currentSpan {}", tracer.currentSpan());

    return span;
  }

  static class MessageHeaderMapAccessor implements MessageHeaderAccessor {
    private final Map<String, String> headers;

    public MessageHeaderMapAccessor(Map<String, String> headers) {
      this.headers = headers;
    }

    @Override
    public void put(String key, String value) {
      headers.put(key, value);
    }

    @Override
    public String get(String key) {
      return headers.get(key);
    }

    @Override
    public void remove(String key) {
      headers.remove(key);
    }
  }
}