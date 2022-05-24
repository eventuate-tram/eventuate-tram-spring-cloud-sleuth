package io.eventuate.tram.spring.cloudsleuthintegration.consumer;

import io.eventuate.tram.spring.cloudsleuthintegration.SpanHelper;
import io.eventuate.tram.spring.cloudsleuthintegration.TramSpringCloudSleuthIntegrationCommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramSpringCloudSleuthIntegrationCommonConfiguration.class)
public class TramSpringCloudSleuthIntegrationConsumerConfiguration {

  @Bean
  public TracingMessageConsumerInterceptor tracingMessageConsumerInterceptor(SpanHelper spanHelper) {
    return new TracingMessageConsumerInterceptor(spanHelper);
  }
}
