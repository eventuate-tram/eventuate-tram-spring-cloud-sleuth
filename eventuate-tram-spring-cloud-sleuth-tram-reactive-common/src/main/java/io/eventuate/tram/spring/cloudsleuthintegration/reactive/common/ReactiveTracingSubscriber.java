package io.eventuate.tram.spring.cloudsleuthintegration.reactive.common;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceContext;
import reactor.core.CoreSubscriber;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

public class ReactiveTracingSubscriber<T> implements CoreSubscriber<T>  {


    private Logger logger = LoggerFactory.getLogger(getClass());

    final CoreSubscriber<? super T> actual;

    final Context context;

    final Span span;


    ReactiveTracingSubscriber(CoreSubscriber<? super T> actual, Context context, Span span) {
        this.actual = actual;
        this.span = span;
        this.context = context.put(TraceContext.class, span.context());
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.actual.onSubscribe(subscription);
    }

    @Override
    public void onNext(T aVoid) {
        // IGNORE
    }

    @Override
    public void onError(Throwable t) {
        terminateSpan(t);
        this.actual.onError(t);
    }

    @Override
    public void onComplete() {
        terminateSpan(null);
        this.actual.onComplete();
    }

    @Override
    public Context currentContext() {
        return this.context;
    }

    private void terminateSpan(@Nullable Throwable t) {
        if (t == null)
            this.span.end();
        else
            this.span.error(t);
     }


}

