package io.eventuate.tram.spring.cloudsleuthintegration.reactive.consumer;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.eventuate.tram.spring.cloudsleuthintegration.reactive.common.AbstractTracingMonoOperator;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class ConsumerMonoOperator extends AbstractTracingMonoOperator<Object> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Message message;
    public ConsumerMonoOperator(Mono<Object> source,
                                SubscriberIdAndMessage subscriberIdAndMessage,
                                Tracer tracer, CurrentTraceContext currentTraceContext, Propagator propagator) {
        super(source, propagator, tracer, currentTraceContext);
        this.message = subscriberIdAndMessage.getMessage();
    }


    @Override
    protected Span createSpan() {
        Span span = propagator.extract(message, (carrier, key) -> carrier.getHeaders().get(key)).start();
        Map<String, String> copy = new HashMap<>(message.getHeaders());
        span.name("receive " + message.getRequiredHeader(Message.DESTINATION));
        copy.forEach((key, value) -> span.tag("message." + key, value));
        // TODO remove b3 headers

        // is this needed

        span.start();
        return span;
    }

    @Override
    protected void onNewSpan(Span span) {
    }




}
