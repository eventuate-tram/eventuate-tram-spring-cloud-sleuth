package io.eventuate.tram.spring.cloudsleuthintegration;

import brave.Tracing;
import io.eventuate.tram.consumer.jdbc.SqlTableBasedDuplicateMessageDetector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramSpringCloudSleuthIntegrationConfiguration {

  @Bean
  @ConditionalOnClass(SqlTableBasedDuplicateMessageDetector.class)
  public SqlTableBasedDuplicateMessageDetectorTracingAspect sqlTableBasedDuplicateMessageDetectorTracingAspect(SpanHelper spanHelper) {
    return new SqlTableBasedDuplicateMessageDetectorTracingAspect(spanHelper);
  }

  @Bean
  public SpanHelper spanHelper(Tracing tracing, Propagator propagator, Tracer tracer) {
    return new SpanHelper(tracing, propagator, MessageHeaderPropagation.INSTANCE, MessageHeaderPropagation.INSTANCE, tracer);
  }

  @Bean
  public TracingMessagingInterceptor tracingMessagingInterceptor(SpanHelper spanHelper) {
    return new TracingMessagingInterceptor(spanHelper);
  }
}
