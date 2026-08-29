package io.eventuate.tram.spring.cloudsleuthintegration.reactive.producer;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateTramSpringCloudSleuthReactiveProducerConfiguration {

    @Bean
    public ReactiveMessageProducerImplementationAspect reactiveMessageProducerImplementationAspect(Tracer tracer, Propagator propagator) {
        return new ReactiveMessageProducerImplementationAspect(tracer, tracer.currentTraceContext(), propagator);
    }


}
