package command.annotation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public @interface CommandArg {
    String value();

    ArgType type() default ArgType.UNDEFINED;

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

