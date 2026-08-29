package io.eventuate.tram.spring.cloudsleuthintegration.reactive.consumer;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecorator;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecoratorChain;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class ReactiveTracingMessageConsumerInterceptor implements ReactiveMessageHandlerDecorator {

  private Logger logger = LoggerFactory.getLogger(getClass());


  final Tracer tracer;


  private final CurrentTraceContext currentTraceContext;

  public final Propagator propagator;

  public ReactiveTracingMessageConsumerInterceptor(Tracer tracer, CurrentTraceContext currentTraceContext, Propagator propagator) {
    this.tracer = tracer;
    this.currentTraceContext = currentTraceContext;
    this.propagator = propagator;
  }

  @Override
  public Publisher<?> accept(SubscriberIdAndMessage subscriberIdAndMessage,
                             ReactiveMessageHandlerDecoratorChain decoratorChain) {

    Mono<Object> source = Mono.defer(() -> Mono.from(decoratorChain.next(subscriberIdAndMessage)));
    return new ConsumerMonoOperator(source, subscriberIdAndMessage, this.tracer, this.currentTraceContext, this.propagator);

  }

  @Override
  public int getOrder() {
    return 0;
  }

}
