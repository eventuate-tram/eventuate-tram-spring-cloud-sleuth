package io.eventuate.tram.spring.cloudsleuthintegration.consumer;

import io.eventuate.tram.spring.cloudsleuthintegration.SpanHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class SqlTableBasedDuplicateMessageDetectorTracingAspect {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private SpanHelper spanHelper;

  public SqlTableBasedDuplicateMessageDetectorTracingAspect(SpanHelper spanHelper) {
    this.spanHelper = spanHelper;
  }

  @Pointcut("execution(* io.eventuate.tram.consumer.jdbc.SqlTableBasedDuplicateMessageDetector.doWithMessage(..))")
  private void doWithMessage() {
  }


  @Around("doWithMessage()")
  public Object aroundDoWithMessage(ProceedingJoinPoint pjp) throws Throwable {
    spanHelper.nextSpan(span -> {
      span.name("SqlTableBasedDuplicateMessageDetector");
      span.tag("message.operation", "DEDUPLICATE");
    });
    try {
      return pjp.proceed();
    } finally {
      spanHelper.finishSpan(null);
    }
  }
}

