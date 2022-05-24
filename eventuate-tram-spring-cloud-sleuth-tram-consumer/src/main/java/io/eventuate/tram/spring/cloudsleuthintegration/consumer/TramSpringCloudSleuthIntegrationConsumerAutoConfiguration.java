package io.eventuate.tram.spring.cloudsleuthintegration.consumer;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramSpringCloudSleuthIntegrationConsumerConfiguration.class)
@ConditionalOnClass(MessageHandlerDecorator.class)
public class TramSpringCloudSleuthIntegrationConsumerAutoConfiguration {

}
