package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.StringReader;

/**
 * Created by matthias on 18.02.16.
 */
public class ImpatientBufferedReaderTest {

    @Test
    public void testReadStringReader() throws Exception {
        try (ImpatientBufferedReader impatientBufferedReader = new ImpatientBufferedReader(
                new StringReader("one\ntwo\r\nthree"))) {
            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals("one", impatientBufferedReader.readLine());

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals("two", impatientBufferedReader.readLine());

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals(null, impatientBufferedReader.readLine());

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals("three", impatientBufferedReader.readLine());

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(true, impatientBufferedReader.hasReachedEOF());
        }
        try (ImpatientBufferedReader impatientBufferedReader = new ImpatientBufferedReader(new StringReader("one\n"))) {
            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals("one", impatientBufferedReader.readLine());

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals(null, impatientBufferedReader.readLine());

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(true, impatientBufferedReader.hasReachedEOF());
        }
    }

    @Test
    public void testReadPiped() throws IOException {
        try (final PipedWriter pipedWriter = new PipedWriter();
             final ImpatientBufferedReader impatientBufferedReader = new ImpatientBufferedReader(
                     new PipedReader(pipedWriter))) {
            Assert.assertEquals(false, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            pipedWriter.write("one\rtwo");

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals("one", impatientBufferedReader.readLine());

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals(null, impatientBufferedReader.readLine());

            Assert.assertEquals(false, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            pipedWriter.write("\nthree");

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            pipedWriter.close();

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals("two", impatientBufferedReader.readLine());

            Assert.assertEquals(true, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals(null, impatientBufferedReader.readLine());

            Assert.assertEquals(false, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals(null, impatientBufferedReader.readLine());

            Assert.assertEquals(false, impatientBufferedReader.ready());
            Assert.assertEquals(false, impatientBufferedReader.hasReachedEOF());

            Assert.assertEquals("three", impatientBufferedReader.readLine(true));

            Assert.assertEquals(false, impatientBufferedReader.ready());
            Assert.assertEquals(true, impatientBufferedReader.hasReachedEOF());
        }
    }
}