package io.eventuate.tram.spring.cloudsleuthintegration.consumer;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramSpringCloudSleuthIntegrationConsumerConfiguration.class)
@ConditionalOnClass(MessageHandlerDecorator.class)
@AutoConfigureAfter(BraveAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.sleuth.enabled", matchIfMissing = true)
public class TramSpringCloudSleuthIntegrationConsumerAutoConfiguration {

}
