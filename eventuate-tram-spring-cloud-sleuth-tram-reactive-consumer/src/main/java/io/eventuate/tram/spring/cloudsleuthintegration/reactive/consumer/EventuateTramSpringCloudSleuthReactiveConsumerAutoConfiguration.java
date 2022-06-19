package io.eventuate.tram.spring.cloudsleuthintegration.reactive.consumer;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecorator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(ReactiveMessageHandlerDecorator.class)
@Import(EventuateTramSpringCloudSleuthReactiveConsumerConfiguration.class)
public class EventuateTramSpringCloudSleuthReactiveConsumerAutoConfiguration {
}
