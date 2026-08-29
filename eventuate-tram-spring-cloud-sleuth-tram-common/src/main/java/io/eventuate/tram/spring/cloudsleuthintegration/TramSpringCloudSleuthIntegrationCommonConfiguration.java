package io.eventuate.tram.spring.cloudsleuthintegration;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramSpringCloudSleuthIntegrationCommonConfiguration {

  @Bean
  public SpanHelper spanHelper(Propagator propagator, Tracer tracer) {
    return new SpanHelper(propagator, MessageHeaderPropagation.INSTANCE, MessageHeaderPropagation.INSTANCE, tracer);
  }

}
