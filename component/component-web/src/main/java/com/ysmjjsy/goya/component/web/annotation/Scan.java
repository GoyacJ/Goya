package com.ysmjjsy.goya.component.web.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/12 19:05
 */
@Target({METHOD, TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Scan {

    /**
     * The name of this tag.
     *
     * @return the name of this tag
     */
    boolean ignore() default false;

    /**
     * The name of this tag.
     *
     * @return the name of this tag
     */
    boolean elementIgnore() default false;
}
