package event.annotation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public @interface MessageArgument {
    /**
     * Argument's name
     */
    String value();

    /**
     * Argument's type
     */
    ArgType type() default ArgType.UNDEFINED;

    /**
     * Whether it is required or not <br>
     * default: true
     */
    boolean required() default true;

    @RequiredArgsConstructor
    enum ArgType {
        UNDEFINED(""),
        TEXT("text"),
        MIXED("mixed"),
        INTEGER("integer"),
        DECIMAL("decimal");

        @Getter
        private final String name;
    }
}

