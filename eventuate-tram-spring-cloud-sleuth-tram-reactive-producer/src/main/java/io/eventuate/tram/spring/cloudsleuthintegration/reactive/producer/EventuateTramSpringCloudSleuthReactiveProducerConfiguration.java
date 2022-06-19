package io.eventuate.tram.spring.cloudsleuthintegration.reactive.producer;

import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateTramSpringCloudSleuthReactiveProducerConfiguration {

    @Bean
    public ReactiveMessageProducerImplementationAspect reactiveMessageProducerImplementationAspect(Tracer tracer, CurrentTraceContext currentTraceContext, Propagator propagator) {
        return new ReactiveMessageProducerImplementationAspect(tracer, currentTraceContext, propagator);
    }


}
