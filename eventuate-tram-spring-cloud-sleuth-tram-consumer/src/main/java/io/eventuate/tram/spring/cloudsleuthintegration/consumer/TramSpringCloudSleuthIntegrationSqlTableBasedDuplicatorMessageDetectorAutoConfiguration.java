package io.eventuate.tram.spring.cloudsleuthintegration.consumer;

import io.eventuate.tram.consumer.jdbc.SqlTableBasedDuplicateMessageDetector;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

@AutoConfiguration(afterName = "org.springframework.boot.micrometer.tracing.brave.autoconfigure.BraveAutoConfiguration")
@ConditionalOnClass({SqlTableBasedDuplicateMessageDetector.class, Tracer.class})
@Import(TramSpringCloudSleuthIntegrationSqlTableBasedDuplicatorMessageDetectorConfiguration.class)
@ConditionalOnProperty(value = "management.tracing.enabled", matchIfMissing = true)
public class TramSpringCloudSleuthIntegrationSqlTableBasedDuplicatorMessageDetectorAutoConfiguration {

}
