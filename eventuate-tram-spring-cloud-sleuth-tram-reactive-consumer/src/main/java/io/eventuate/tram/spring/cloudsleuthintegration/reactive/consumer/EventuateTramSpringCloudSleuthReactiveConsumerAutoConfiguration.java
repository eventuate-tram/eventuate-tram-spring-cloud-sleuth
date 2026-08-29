package io.eventuate.tram.spring.cloudsleuthintegration.reactive.consumer;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecorator;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

@AutoConfiguration(afterName = "org.springframework.boot.micrometer.tracing.brave.autoconfigure.BraveAutoConfiguration")
@ConditionalOnClass({ReactiveMessageHandlerDecorator.class, Tracer.class})
@Import(EventuateTramSpringCloudSleuthReactiveConsumerConfiguration.class)
@ConditionalOnProperty(value = "management.tracing.enabled", matchIfMissing = true)
public class EventuateTramSpringCloudSleuthReactiveConsumerAutoConfiguration {
}
