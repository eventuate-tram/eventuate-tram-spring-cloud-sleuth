package io.eventuate.tram.spring.cloudsleuthintegration.consumer;

import io.eventuate.tram.spring.cloudsleuthintegration.SpanHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramSpringCloudSleuthIntegrationSqlTableBasedDuplicatorMessageDetectorConfiguration {

  @Bean
  public SqlTableBasedDuplicateMessageDetectorTracingAspect sqlTableBasedDuplicateMessageDetectorTracingAspect(SpanHelper spanHelper) {
    return new SqlTableBasedDuplicateMessageDetectorTracingAspect(spanHelper);
  }

}
