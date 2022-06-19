package io.eventuate.tram.spring.cloudsleuthintegration.reactive.common;

import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import reactor.core.publisher.Mono;

public abstract class AbstractChildSpanMonoOperator<T> extends AbstractTracingMonoOperator<T> {
    public AbstractChildSpanMonoOperator(Mono<? extends T> source, Propagator propagator, Tracer tracer, CurrentTraceContext currentTraceContext) {
        super(source, propagator, tracer, currentTraceContext);
    }

    @Override
    protected Span createSpan() {
        return createChildSpan();
    }

    protected Span createChildSpan() {
        Span span = tracer.nextSpan();
        initializeSpan(span);
        span.start();
        return span;
    }

    protected abstract void initializeSpan(Span span);

}
