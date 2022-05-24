package io.eventuate.tram.spring.cloudsleuthintegration.producer;

import io.eventuate.tram.spring.cloudsleuthintegration.SpanHelper;
import io.eventuate.tram.spring.cloudsleuthintegration.TramSpringCloudSleuthIntegrationCommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramSpringCloudSleuthIntegrationCommonConfiguration.class)
public class TramSpringCloudSleuthIntegrationProducerConfiguration {

  @Bean
  public TracingMessageProducerInterceptor tracingMessageProducerInterceptor(SpanHelper spanHelper) {
    return new TracingMessageProducerInterceptor(spanHelper);
  }
}
