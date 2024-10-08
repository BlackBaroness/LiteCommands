package dev.rollczi.litecommands.annotations;

import dev.rollczi.litecommands.command.builder.CommandBuilder;

import java.lang.annotation.Annotation;

class ClassInvoker<SENDER> implements AnnotationInvoker<SENDER> {

    private final Class<?> type;
    private CommandBuilder<SENDER> commandBuilder;

    public ClassInvoker(Class<?> type, CommandBuilder<SENDER> commandBuilder) {
        this.type = type;
        this.commandBuilder = commandBuilder;
    }

    @Override
    public <A extends Annotation> AnnotationInvoker<SENDER> on(Class<A> annotationType, AnnotationProcessor.AnyListener<A> listener) {
        A annotation = type.getAnnotation(annotationType);

        if (annotation == null) {
            return this;
        }

        listener.call(annotation, commandBuilder);
        return this;
    }

    @Override
    public <A extends Annotation> AnnotationInvoker<SENDER> onClass(Class<A> annotationType, AnnotationProcessor.ClassListener<SENDER, A> listener) {
        A annotation = type.getAnnotation(annotationType);

        if (annotation == null) {
            return this;
        }

        commandBuilder = listener.call(type, annotation, commandBuilder);
        return this;
    }

    @Override
    public CommandBuilder<SENDER> getResult() {
        return commandBuilder;
    }

}
