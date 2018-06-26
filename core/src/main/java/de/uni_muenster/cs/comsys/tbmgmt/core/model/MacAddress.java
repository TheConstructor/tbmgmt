package de.uni_muenster.cs.comsys.tbmgmt.core.model;

import com.google.common.primitives.UnsignedBytes;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Transient;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 Created by matthias on 07.10.15.
 */
public class MacAddress implements Serializable {
    /**
     An usual MAC-Address only contains 6 segments, but we could support more.
     */
    public static final  int     MAC_ADDRESS_SIZE = 6;
    private static final Pattern SEGMENT_PATTERN  = Pattern.compile("(?:^|[-:])([0-9a-fA-F]{1,2})");
    private final byte[] address;

    public MacAddress(final byte[] address) {
        if (address == null) {
            throw new IllegalArgumentException("Provided null as address");
        }
        this.address = Arrays.copyOf(address, MAC_ADDRESS_SIZE);
    }

    public MacAddress(final String address) throws ParseException {
        if (address == null) {
            throw new IllegalArgumentException("Provided null as address");
        } else if ("".equals(address)) {
            this.address = new byte[0];
        } else {
            final Matcher matcher = SEGMENT_PATTERN.matcher(address);
            if (!matcher.find()) {
                throw new ParseException(String.format("Could not parse \"%s\" as Mac-Address", address), 0);
            }

            final ByteBuffer buffer = ByteBuffer.allocate(MAC_ADDRESS_SIZE);
            int count = 0;
            int pos = 0;
            try {
                do {
                    if (matcher.start() != pos) {
                        throw new ParseException(String.format("Could not parse \"%s\" as Mac-Address", address), pos);
                    }
                    if (!buffer.hasRemaining()) {
                        throw new ParseException(
                                String.format("\"%s\" contains more than %d segments", address, MAC_ADDRESS_SIZE), pos);
                    }

                    count++;
                    buffer.put(UnsignedBytes.parseUnsignedByte(matcher.group(1), 16));
                    pos = matcher.end();
                } while (matcher.find());
            } catch (final NumberFormatException e) {
                final ParseException parseException = new ParseException(
                        String.format("Segment %d of \"%s\" could not be read as number", count, address), pos);
                parseException.initCause(e);
                throw parseException;
            }

            if (pos != address.length()) {
                throw new ParseException(
                        String.format("\"%s\" contains trailing garbage and cannot be read as Mac-Address", address),
                        pos);
            }

            this.address = new byte[count];
            buffer.rewind();
            buffer.get(this.address);
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getAddress())
                .toHashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MacAddress)) {
            return false;
        }

        final MacAddress that = (MacAddress) o;

        return new EqualsBuilder()
                .append(getAddress(), that.getAddress())
                .isEquals();
    }

    @Transient
    public String getColonizedString() {
        return getHexSegmentStream().collect(Collectors.joining(":"));
    }

    @Transient
    public String getDashedString() {
        return getHexSegmentStream().collect(Collectors.joining("-"));
    }

    @Override
    public String toString() {
        return getDashedString();
    }

    @Transient
    private Stream<String> getHexSegmentStream() {
        return TbmgmtUtil.streamOfUnsigned(address).mapToObj(segment -> String.format("%02X", segment));
    }

    public byte[] getAddress() {
        return Arrays.copyOf(address, MAC_ADDRESS_SIZE);
    }
}
