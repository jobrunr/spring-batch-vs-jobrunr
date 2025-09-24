package org.jobrunr.demo.batch.jobrunr;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.jobrunr.demo.batch.model.Person;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.jobs.context.JobDashboardProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jobrunr.jobs.context.JobDashboardProgressBar.JOBRUNR_PROGRESSBAR_KEY;
import static org.jobrunr.utils.reflection.ReflectionUtils.cast;

public class ProgressiblePersonReader {

    private final LineNumberReader lineNumberReader;
    private final JobDashboardProgressBar progressBar;
    private final ObjectReader csvReader;

    public ProgressiblePersonReader(String fileName, JobContext jobContext) throws IOException {
        this.lineNumberReader = new LineNumberReader(fileName);
        long previousRunProcessedCount = getPreviousRunProcessedCount(jobContext);
        this.lineNumberReader.skipLines(previousRunProcessedCount);
        this.progressBar = jobContext.progressBar(lineNumberReader.getTotalAmount());
        this.progressBar.setProgress(previousRunProcessedCount);
        this.csvReader = new CsvMapper()
                .readerFor(Person.class)
                .with(CsvSchema.builder()
                        .addColumn("firstName")
                        .addColumn("lastName")
                        .addColumn("visible")
                        .build());
    }

    public void increaseProgress(int size) {
        progressBar.setProgress(progressBar.getSucceededAmount() + size);
    }

    public List<Person> read(int amount) throws IOException {
        List<Person> personList = new ArrayList<>();
        while (lineNumberReader.hasMoreLines() && personList.size() <= amount) {
            personList.add(csvReader.readValue(lineNumberReader.readLine()));
        }
        return personList;
    }

    public boolean hasMorePersons() {
        return lineNumberReader.hasMoreLines();
    }

    private static long getPreviousRunProcessedCount(JobContext jobContext) {
        Map<String, Object> jobMetadata = jobContext.getMetadata();
        return jobMetadata.keySet().stream().filter(key -> key.startsWith(JOBRUNR_PROGRESSBAR_KEY))
                .max(String::compareTo)
                .map(key -> new JobDashboardProgressBar(cast(jobMetadata.get(key))))
                .map(JobDashboardProgressBar::getSucceededAmount)
                .orElse(0L);
    }

    static class LineNumberReader extends BufferedReader {
        private final long totalAmount;
        private long currentLine;

        public LineNumberReader(String fileName) throws IOException {
            this(new File(fileName));
        }

        public LineNumberReader(File file) throws IOException {
            this(new FileReader(file));
        }

        public LineNumberReader(Reader fileReader) throws IOException {
            super(fileReader);
            this.totalAmount = readTotalAmount();
        }

        public void skipLines(long amountToSkip) throws IOException {
            for (long l = 0; l < amountToSkip; l++) {
                readLine();
            }
        }

        @Override
        public String readLine() throws IOException {
            String line = super.readLine();
            if (line != null) {
                currentLine++;
            }
            return line;
        }

        public long getCurrentLine() {
            return currentLine;
        }

        public long getTotalAmount() {
            return totalAmount;
        }

        public boolean hasMoreLines() {
            return currentLine < totalAmount;
        }

        private long readTotalAmount() throws IOException {
            super.mark(100_000);
            long amount = 0L;
            while (super.readLine() != null) {
                amount++;
            }
            super.reset();
            return amount;
        }
    }
}
