package io.eventuate.tram.spring.cloudsleuthintegration.producer;

import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramSpringCloudSleuthIntegrationProducerConfiguration.class)
@ConditionalOnClass(MessageProducerImplementation.class)
public class TramSpringCloudSleuthIntegrationProducerAutoConfiguration {

}
