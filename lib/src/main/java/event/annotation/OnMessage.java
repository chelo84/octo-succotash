package event.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnMessage {
    /**
     * Command's name
     */
    String value();

    /**
     * Command's description
     */
    String description() default "";

    /**
     * Command's arguments
     */
    MessageArgument[] args() default {};
}
