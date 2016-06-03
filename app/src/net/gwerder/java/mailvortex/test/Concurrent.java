package net.gwerder.java.mailvortex.test;

/**
 * Created by martin.gwerder on 03.06.2016.
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface for ConcurrentSuite.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @url http://www.chenblog.xyz/questions/997112/running-junit-test-in-parallel-on-suite-level
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Concurrent {
    int threads() default 5;
}