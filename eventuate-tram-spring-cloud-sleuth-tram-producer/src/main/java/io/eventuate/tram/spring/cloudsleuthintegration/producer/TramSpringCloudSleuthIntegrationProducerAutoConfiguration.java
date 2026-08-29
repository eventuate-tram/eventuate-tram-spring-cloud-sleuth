package io.eventuate.tram.spring.cloudsleuthintegration.producer;

import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

@AutoConfiguration(afterName = "org.springframework.boot.micrometer.tracing.brave.autoconfigure.BraveAutoConfiguration")
@Import(TramSpringCloudSleuthIntegrationProducerConfiguration.class)
@ConditionalOnClass({MessageProducerImplementation.class, Tracer.class})
@ConditionalOnProperty(value = "management.tracing.enabled", matchIfMissing = true)
public class TramSpringCloudSleuthIntegrationProducerAutoConfiguration {

}
