package io.eventuate.tram.spring.cloudsleuthintegration.reactive.producer;

import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducerImplementation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(ReactiveMessageProducerImplementation.class)
@Import(EventuateTramSpringCloudSleuthReactiveProducerConfiguration.class)
public class EventuateTramSpringCloudSleuthReactiveProducerAutoConfiguration {
}
