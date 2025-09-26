package org.jobrunr.jobs.etl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;

class FiniteStreamInvocationHandler<T> implements InvocationHandler {

    private final Stream<T> delegate;
    private final java.util.function.Supplier<Long> countSupplier;

    FiniteStreamInvocationHandler(Stream<T> delegate, java.util.function.Supplier<Long> countSupplier) {
        this.delegate = delegate;
        this.countSupplier = countSupplier;
    }

    static <T> FiniteStream<T> of(Stream<T> stream, long count) {
        return of(stream, () -> count);
    }

    @SuppressWarnings("unchecked")
    static <T> FiniteStream<T> of(Stream<T> stream, java.util.function.Supplier<Long> countSupplier) {
        return (FiniteStream<T>) Proxy.newProxyInstance(
                FiniteStream.class.getClassLoader(),
                new Class<?>[]{FiniteStream.class},
                new FiniteStreamInvocationHandler<>(stream, countSupplier)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle the FiniteStream-specific method
        if ("getTotalCount".equals(method.getName()) && method.getParameterCount() == 0) {
            return countSupplier.get();
        }

        // For all other methods, delegate to the underlying Stream
        return method.invoke(delegate, args);
    }
}
