package io.eventuate.tram.spring.cloudsleuthintegration;

import org.springframework.cloud.sleuth.propagation.Propagator;

import java.util.List;

public class MessageHeaderPropagation implements Propagator.Setter<MessageHeaderAccessor>, Propagator.Getter<MessageHeaderAccessor> {

  public static MessageHeaderPropagation INSTANCE = new MessageHeaderPropagation();

  public static void removeAnyTraceHeaders(MessageHeaderAccessor headers, List<String> keys) {
    keys.forEach(headers::remove);
  }

  @Override
  public String get(MessageHeaderAccessor carrier, String key) {
    return carrier.get(key);
  }

  @Override
  public void set(MessageHeaderAccessor carrier, String key, String value) {
    carrier.put(key, value);
  }
}
