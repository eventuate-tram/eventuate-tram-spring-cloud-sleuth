package io.eventuate.tram.spring.cloudsleuthintegration.reactive.common;

import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;
import reactor.util.context.Context;

public abstract class AbstractTracingMonoOperator<T> extends MonoOperator<T, T> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    protected final Propagator propagator;
    protected final Tracer tracer;
    protected final Span span;
    protected final CurrentTraceContext currentTraceContext;

    public AbstractTracingMonoOperator(Mono<? extends T> source, Propagator propagator, Tracer tracer, CurrentTraceContext currentTraceContext) {
        super(source);
        this.propagator = propagator;
        this.tracer = tracer;
        this.span = this.tracer.currentSpan();
        this.currentTraceContext = currentTraceContext;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> subscriber) {
        Context context = subscriber.currentContext();
        Span span = createSpan();
        try (CurrentTraceContext.Scope scope = this.currentTraceContext.maybeScope(span.context())) {
            onNewSpan(span);
            this.source.subscribe(new ReactiveTracingSubscriber<>(subscriber, context, span));
        }
    }

    protected abstract Span createSpan();


    protected abstract void onNewSpan(Span span);

}
