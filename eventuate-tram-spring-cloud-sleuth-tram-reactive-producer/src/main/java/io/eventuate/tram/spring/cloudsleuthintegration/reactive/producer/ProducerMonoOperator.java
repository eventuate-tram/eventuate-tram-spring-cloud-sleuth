package io.eventuate.tram.spring.cloudsleuthintegration.reactive.producer;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.spring.cloudsleuthintegration.reactive.common.AbstractChildSpanMonoOperator;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;



public class ProducerMonoOperator extends AbstractChildSpanMonoOperator<Message> {

    private final Message message;


    ProducerMonoOperator(Mono<? extends Message> source,
                         Message message, Tracer tracer, CurrentTraceContext currentTraceContext, Propagator propagator) {
        super(source, propagator, tracer, currentTraceContext);
        this.message = message;
    }


    @Override
    protected void onNewSpan(Span span) {
        propagator.inject(currentTraceContext.context(),
                message, (carrier, key, value) -> carrier.getHeaders().put(key, value));
    }

    @Override
    protected void initializeSpan(Span span) {
        Map<String, String> copy = new HashMap<>(message.getHeaders());
        span.name("doSend " + message.getRequiredHeader(Message.DESTINATION));
        copy.forEach((key, value) -> span.tag("message." + key, value));
    }


}
