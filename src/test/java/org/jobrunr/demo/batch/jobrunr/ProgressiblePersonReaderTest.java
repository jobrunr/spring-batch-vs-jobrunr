package org.jobrunr.demo.batch.jobrunr;

import org.jobrunr.demo.batch.jobrunr.ProgressiblePersonReader.LineNumberReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressiblePersonReaderTest {

    @Test
    void testHasMoreLinesA() throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader("./src/main/resources/person-data.csv");
        assertThat(lineNumberReader.hasMoreLines()).isTrue();
    }

    @Test
    void testHasMoreLinesB() throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader("./src/main/resources/person-data.csv");
        lineNumberReader.readLine();
        lineNumberReader.readLine();
        lineNumberReader.readLine();
        assertThat(lineNumberReader.getCurrentLine()).isEqualTo(3);
        assertThat(lineNumberReader.hasMoreLines()).isTrue();

        lineNumberReader.readLine();
        lineNumberReader.readLine();
        assertThat(lineNumberReader.getCurrentLine()).isEqualTo(5);
        assertThat(lineNumberReader.hasMoreLines()).isFalse();
    }
}