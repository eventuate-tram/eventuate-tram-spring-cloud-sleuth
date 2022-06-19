package io.eventuate.tram.spring.cloudsleuthintegration.reactive.consumer;

import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateTramSpringCloudSleuthReactiveConsumerConfiguration {


    @Bean
    public ReactiveTracingMessageConsumerInterceptor reactiveTracingMessageConsumerInterceptor(Tracer tracer, CurrentTraceContext currentTraceContext, Propagator propagator) {
        return new ReactiveTracingMessageConsumerInterceptor(tracer, currentTraceContext, propagator);
    }

}
