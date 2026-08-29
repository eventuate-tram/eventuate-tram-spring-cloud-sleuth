package io.eventuate.tram.spring.cloudsleuthintegration.reactive.common;

import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
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
