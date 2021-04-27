package uk.co.eelpieconsulting.feedlistener.annotations;

import java.lang.reflect.Method;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class TimingAspect {

    private final static Logger log = LogManager.getLogger(TimingAspect.class);

    /**
     * This around advice adds timing to any method annotated with the Timed
     * annotation. It binds the annotation to the parameter timedAnnotation so
     * that the values are available at runtime. Also note that the retention
     * policy of the annotation needs to be RUNTIME.
     *
     * @param pjp             - the join point for this advice
     * @param timedAnnotation - the Timed annotation as declared on the method
     * @return
     * @throws Throwable
     */
    @Around("@annotation( timedAnnotation ) ")
    public Object processSystemRequest(final ProceedingJoinPoint pjp, Timed timedAnnotation) throws Throwable {
        try {
            long start = System.nanoTime();
            Object retVal = pjp.proceed();
            long end = System.nanoTime();
            Duration differenceNanos = Duration.ofNanos(end - start);

            final MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            final Method targetMethod = methodSignature.getMethod();

            if (differenceNanos.toMillis() > 100) {
                log.warn(targetMethod.getDeclaringClass().getName() + "."
                        + targetMethod.getName() + " took " + differenceNanos
                        + " ms : timing notes: " + timedAnnotation.timingNotes()
                        + " request info : ");
            } else {
                log.debug(targetMethod.getDeclaringClass().getName() + "."
                        + targetMethod.getName() + " took " + differenceNanos
                        + " ms : timing notes: " + timedAnnotation.timingNotes()
                        + " request info : ");
            }

            return retVal;

        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

}
