package org.jobrunr.jobs.etl;

import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.jobs.context.JobDashboardProgressBar;
import org.jobrunr.server.runner.ThreadLocalJobContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public abstract class JobRunrEtlTask<C, X, Y> implements AutoCloseable {

    private C context;

    public void run(C context) throws Exception {
        this.context = context;

        onStart();
        try (Stream<X> dataStream = extract()) {
            ETLProgress progress = new ETLProgress(dataStream);

            // Process each partition manually as Java 8 Stream API cannot handle checked exceptions
            Iterator<List<X>> partitions = partition(dataStream.skip(progress.startAt()), 100).iterator();
            while (partitions.hasNext()) {
                List<X> batch = partitions.next();
                List<Y> transformedBatch = new ArrayList<>();
                // Transform each item in the batch
                for (X item : batch) {
                    Y transformed = transform(item);
                    transformedBatch.add(transformed);
                }

                // Write the transformed batch
                load(transformedBatch);
                progress.increase(transformedBatch.size());
            }
        }
        close();
        onEnd();
    }

    protected abstract Stream<X> extract() throws Exception;

    protected abstract Y transform(X input) throws Exception;

    protected abstract void load(List<Y> output) throws Exception;

    protected C getContext() {
        return context;
    }

    protected void onStart() throws Exception {

    }

    protected void onEnd() throws Exception {

    }

    public void close() throws Exception {
    }

    private static <T> Stream<List<T>> partition(Stream<T> stream, int batchSize) {
        Iterator<T> iterator = stream.iterator();
        return Stream.generate(() -> {
                    List<T> batch = new ArrayList<>(batchSize);
                    for (int i = 0; i < batchSize && iterator.hasNext(); i++) {
                        batch.add(iterator.next());
                    }
                    return batch.isEmpty() ? null : batch;
                })
                .takeWhile(Objects::nonNull);
    }

    private static class ETLProgress {

        private static final String JOBRUNR_ETL_PROGRESS_METADATA_KEY = "etlProgress";
        private final JobContext jobContext;
        private final JobDashboardProgressBar progressBar;
        private final AtomicLong progressCounter;

        public ETLProgress(Stream<?> stream) {
            this.jobContext = ThreadLocalJobContext.getJobContext();
            this.progressBar = createProgressBarIfFiniteStream(stream);
            this.progressCounter = new AtomicLong(startAt());
        }


        public void increase(long increment) {
            jobContext.saveMetadata(JOBRUNR_ETL_PROGRESS_METADATA_KEY, progressCounter.addAndGet(increment));
            if (this.progressBar != null) {
                this.progressBar.setProgress(progressCounter.get());
            }
        }

        private long startAt() {
            Long etlProgress = jobContext.getMetadata(JOBRUNR_ETL_PROGRESS_METADATA_KEY);
            if (etlProgress == null) return 0L;
            return etlProgress;
        }

        private JobDashboardProgressBar createProgressBarIfFiniteStream(Stream<?> stream) {
            if (stream instanceof FiniteStream<?>) {
                return jobContext.progressBar(((FiniteStream<?>) stream).getTotalCount());
            }
            return null;
        }
    }
}
