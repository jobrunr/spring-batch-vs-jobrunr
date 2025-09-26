package org.jobrunr.jobs.etl;

import org.jobrunr.utils.exceptions.Exceptions;

import java.util.stream.Stream;

public interface FiniteStream<T> extends Stream<T> {

    static <T> FiniteStream<T> using(Stream<T> stream, long count) {
        return FiniteStreamInvocationHandler.of(stream, count);
    }

    static <T> FiniteStream<T> usingStreamCount(Exceptions.ThrowingSupplier<Stream<T>> streamSupplier) throws Exception {
        return FiniteStreamInvocationHandler.of(streamSupplier.get(), streamSupplier.get().count());
    }

    default long getTotalCount() {
        return Long.MIN_VALUE;
    }
}
