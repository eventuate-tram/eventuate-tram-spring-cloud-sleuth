package io.eventuate.tram.spring.cloudsleuthintegration.reactive.producer;

import io.eventuate.tram.messaging.common.Message;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import reactor.core.publisher.Mono;

@Aspect
public class ReactiveMessageProducerImplementationAspect implements ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(getClass());


    final Tracer tracer;


    private CurrentTraceContext currentTraceContext;
    private ApplicationContext applicationContext;

    public final Propagator propagator;

    public ReactiveMessageProducerImplementationAspect(Tracer tracer, CurrentTraceContext currentTraceContext, Propagator propagator) {
        this.tracer = tracer;
        this.currentTraceContext = currentTraceContext;
        this.propagator = propagator;
    }

    @Pointcut("execution(* io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducerImplementation.send(..))")
    private void doWithMessage() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    CurrentTraceContext currentTraceContext() {
        if (this.currentTraceContext == null) {
            this.currentTraceContext = this.applicationContext.getBean(CurrentTraceContext.class);
        }
        return this.currentTraceContext;
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
        return new ProducerMonoOperator(source, (Message)pjp.getArgs()[0], this.tracer, this.currentTraceContext(), this.propagator);
    }


}
