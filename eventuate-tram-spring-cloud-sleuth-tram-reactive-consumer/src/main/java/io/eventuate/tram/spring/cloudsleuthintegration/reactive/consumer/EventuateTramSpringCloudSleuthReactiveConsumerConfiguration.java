package io.eventuate.tram.spring.cloudsleuthintegration.reactive.consumer;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateTramSpringCloudSleuthReactiveConsumerConfiguration {


    @Bean
    public ReactiveTracingMessageConsumerInterceptor reactiveTracingMessageConsumerInterceptor(Tracer tracer, Propagator propagator) {
        return new ReactiveTracingMessageConsumerInterceptor(tracer, tracer.currentTraceContext(), propagator);
    }

}
