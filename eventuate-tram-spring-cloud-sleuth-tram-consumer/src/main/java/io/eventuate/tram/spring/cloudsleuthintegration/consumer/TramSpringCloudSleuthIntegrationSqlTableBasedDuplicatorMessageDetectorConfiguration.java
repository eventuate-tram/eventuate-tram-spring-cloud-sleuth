package io.eventuate.tram.spring.cloudsleuthintegration.consumer;

import io.eventuate.tram.consumer.jdbc.SqlTableBasedDuplicateMessageDetector;
import io.eventuate.tram.spring.cloudsleuthintegration.SpanHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramSpringCloudSleuthIntegrationSqlTableBasedDuplicatorMessageDetectorConfiguration {

  @Bean
  @ConditionalOnClass(SqlTableBasedDuplicateMessageDetector.class)
  public SqlTableBasedDuplicateMessageDetectorTracingAspect sqlTableBasedDuplicateMessageDetectorTracingAspect(SpanHelper spanHelper) {
    return new SqlTableBasedDuplicateMessageDetectorTracingAspect(spanHelper);
  }

}
