package io.eventuate.tram.spring.cloudsleuthintegration.reactive.producer;

import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducerImplementation;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

@AutoConfiguration(afterName = "org.springframework.boot.micrometer.tracing.brave.autoconfigure.BraveAutoConfiguration")
@ConditionalOnClass({ReactiveMessageProducerImplementation.class, Tracer.class})
@Import(EventuateTramSpringCloudSleuthReactiveProducerConfiguration.class)
@ConditionalOnProperty(value = "management.tracing.enabled", matchIfMissing = true)
public class EventuateTramSpringCloudSleuthReactiveProducerAutoConfiguration {
}
