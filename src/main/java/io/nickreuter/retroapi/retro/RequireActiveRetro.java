package io.nickreuter.retroapi.retro;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireActiveRetro {
    String retroIdParam() default "";
    String thoughtIdParam() default "";
}
