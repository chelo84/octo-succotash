package command.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
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
    CommandArg[] args() default {};
}
