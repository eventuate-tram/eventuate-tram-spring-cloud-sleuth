package io.eventuate.tram.spring.cloudsleuthintegration.reactive.consumer;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecorator;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecoratorChain;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

public class ReactiveTracingMessageConsumerInterceptor implements ReactiveMessageHandlerDecorator {

  private Logger logger = LoggerFactory.getLogger(getClass());


  final Tracer tracer;


  private CurrentTraceContext currentTraceContext;

  public final Propagator propagator;

  @Autowired
  private ApplicationContext applicationContext;


  public ReactiveTracingMessageConsumerInterceptor(Tracer tracer, CurrentTraceContext currentTraceContext, Propagator propagator) {
    this.tracer = tracer;
    this.currentTraceContext = currentTraceContext;
    this.propagator = propagator;
  }

  @Override
  public Publisher<?> accept(SubscriberIdAndMessage subscriberIdAndMessage,
                             ReactiveMessageHandlerDecoratorChain decoratorChain) {

    Mono<Object> source = Mono.defer(() -> Mono.from(decoratorChain.next(subscriberIdAndMessage)));
    return new ConsumerMonoOperator(source, subscriberIdAndMessage, this.tracer, this.currentTraceContext(), this.propagator);

  }

  @Override
  public int getOrder() {
    return 0;
  }

  CurrentTraceContext currentTraceContext() {
    if (this.currentTraceContext == null) {
      this.currentTraceContext = this.applicationContext.getBean(CurrentTraceContext.class);
    }
    return this.currentTraceContext;
  }

}
