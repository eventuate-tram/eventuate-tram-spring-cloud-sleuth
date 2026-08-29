package io.eventuate.tram.spring.cloudsleuthintegration.reactive.producer;

import io.eventuate.tram.messaging.common.Message;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Aspect
public class ReactiveMessageProducerImplementationAspect {

    private Logger logger = LoggerFactory.getLogger(getClass());


    final Tracer tracer;


    private final CurrentTraceContext currentTraceContext;

    public final Propagator propagator;

    public ReactiveMessageProducerImplementationAspect(Tracer tracer, CurrentTraceContext currentTraceContext, Propagator propagator) {
        this.tracer = tracer;
        this.currentTraceContext = currentTraceContext;
        this.propagator = propagator;
    }

    @Pointcut("execution(* io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducerImplementation.send(..))")
    private void doWithMessage() {
    }

    @Around("doWithMessage()")
    public Object aroundSend(ProceedingJoinPoint pjp) throws Throwable {
        Mono<Message> source = Mono.defer(() -> {
            try {
                return (Mono<Message>) pjp.proceed();
            } catch (Throwable e) {
                return Mono.error(e);
            }
        });
        return new ProducerMonoOperator(source, (Message)pjp.getArgs()[0], this.tracer, this.currentTraceContext, this.propagator);
    }


}
