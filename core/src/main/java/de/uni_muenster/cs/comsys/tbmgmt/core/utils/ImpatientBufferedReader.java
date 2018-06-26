package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

/**
 * Inspired by {@link BufferedReader} this class provides a method to read a line when the line is available without
 * waiting. This class is not thread-safe.
 */
public class ImpatientBufferedReader implements Closeable, Serializable {

    public static final int BUFFER_SIZE = 4096;

    private final Reader reader;
    private final StringBuilder stringBuilder;
    private int noEOLBefore = 0;
    private boolean lastWasCarriageReturn = false;
    private boolean reachedEOF = false;

    public ImpatientBufferedReader(final Reader reader) {
        this.reader = reader;
        this.stringBuilder = new StringBuilder(BUFFER_SIZE);
    }

    /**
     * {@code true} if there are unevaluated characters in the buffer or the underlying stream is ready.
     */
    public boolean ready() throws IOException {
        return noEOLBefore < stringBuilder.length() || reader.ready() || (reachedEOF && stringBuilder.length() > 0);
    }

    /**
     * Try to read a line.
     *
     * @return a line (excluding line-break), everything since the last line up to EOF or {@code null} if the reader
     * did not reach EOF and the buffer does not currently contain a line-break.
     * @throws IOException
     * @see BufferedReader#readLine()
     */
    public String readLine() throws IOException {
        return readLine(false);
    }

    /**
     * Try to read a line.
     *
     * @param blocking if {@code true} also call {@link Reader#read(char[])} when {@link Reader#ready()} returns false
     * @return a line (excluding line-break), everything since the last line up to EOF or {@code null} if the reader
     * did not reach EOF and the buffer does not currently contain a line-break.
     * @throws IOException
     * @see BufferedReader#readLine()
     */
    public String readLine(final boolean blocking) throws IOException {
        do {
            // Fill buffer if we have seen everything
            if (noEOLBefore >= stringBuilder.length()) {
                final int read = fillBuffer(blocking);
                if (read < 0) {
                    // We reached EOF. Return something.
                    if (stringBuilder.length() == 0) {
                        stringBuilder.trimToSize();
                        return null;
                    } else {
                        final String string = stringBuilder.toString();
                        stringBuilder.setLength(0);
                        stringBuilder.trimToSize();
                        return string;
                    }
                }
            }
            if (noEOLBefore < stringBuilder.length()) {
                if (lastWasCarriageReturn) {
                    if (stringBuilder.charAt(noEOLBefore) == '\n') {
                        stringBuilder.deleteCharAt(noEOLBefore);
                    }
                    lastWasCarriageReturn = false;
                }
                for (; noEOLBefore < stringBuilder.length(); noEOLBefore++) {
                    switch (stringBuilder.charAt(noEOLBefore)) {
                        case '\r':
                            lastWasCarriageReturn = true;
                        case '\n':
                            final String line = stringBuilder.substring(0, noEOLBefore);
                            stringBuilder.delete(0, noEOLBefore + 1);
                            noEOLBefore = 0;
                            shrinkStringBuilder();
                            return line;
                    }
                }
            }
        } while (blocking);
        return null;
    }

    /**
     * This function tries to remove unused characters from memory and return
     * {@link #stringBuilder} to the capacity of {@link #BUFFER_SIZE}
     */
    private void shrinkStringBuilder() {
        if (stringBuilder.capacity() > BUFFER_SIZE) {
            final int currentLength = stringBuilder.length();
            if (currentLength > BUFFER_SIZE) {
                stringBuilder.trimToSize();
            } else {
                stringBuilder.setLength(BUFFER_SIZE);
                stringBuilder.trimToSize();
                stringBuilder.setLength(currentLength);
            }
        }
    }

    private int fillBuffer(final boolean blocking) throws IOException {
        if (blocking || reader.ready()) {
            final char[] buffer = new char[BUFFER_SIZE];
            final int read = reader.read(buffer);
            if (read > 0) {
                stringBuilder.append(buffer, 0, read);
            } else if (read < 0) {
                reachedEOF = true;
            }
            return read;
        }
        return 0;
    }

    public boolean hasReachedEOF() {
        return reachedEOF;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
