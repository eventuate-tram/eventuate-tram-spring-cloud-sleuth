package io.eventuate.tram.spring.cloudsleuthintegration;

import brave.Tracing;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramSpringCloudSleuthIntegrationCommonConfiguration {

  @Bean
  public SpanHelper spanHelper(Tracing tracing, Propagator propagator, Tracer tracer) {
    return new SpanHelper(tracing, propagator, MessageHeaderPropagation.INSTANCE, MessageHeaderPropagation.INSTANCE, tracer);
  }

}
