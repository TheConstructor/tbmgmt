package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 Created by matthias on 20.05.15.
 */
public class TbmgmtUtil {

    public static final byte[] RFC_5987_ATTR_CHARS = {
            '!', '#', '$', '&', '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '|', '~'
    };
    public static final char[] HEX_DIGITS          = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static final Pattern HEADER_ESCAPES = Pattern.compile("[\\\\\"\0-\37\177]");

    private static final Pattern LIKE_ESCAPE_PATTERN = Pattern.compile("([%_&])");
    private static final String LIKE_ESCAPE_REPLACE = "&$1";
    public static final char LIKE_ESCAPE_CHAR = '&';

    public static String asContentDisposition(final String filename) {
        // This could be extended to escape chars between 0 and 31 plus 127
        final String rfc2616 =
                HEADER_ESCAPES.matcher(Translit.toCharset(filename, StandardCharsets.ISO_8859_1)).replaceAll("\\\\$0");
        final String rfc5987 = rfc5987Encode(StandardCharsets.UTF_8, "", filename);
        return "attachment; filename=\"" + rfc2616 + "\"; filename*=" + rfc5987;
    }

    /**
     * Based upon http://stackoverflow.com/a/11307864/1266906
     *
     * @param charset     charset to derive byte-representation of {@code s}
     * @param language    optional language-code
     * @param s           Input-String
     * @return RFC 5987 compliant ext-value
     * @see <a href="http://tools.ietf.org/html/rfc5987">RFC 5987</a>
     */
    public static String rfc5987Encode(final Charset charset, final String language, final String s) {
        final String charsetName = charset.name();
        final byte[] s_bytes = s.getBytes(charset);
        final StringBuilder sb = new StringBuilder(s_bytes.length << 1 + charsetName.length());
        sb.append(charsetName);
        sb.append('\'');
        if (language != null) {
            sb.append(language);
        }
        sb.append('\'');
        for (final byte b : s_bytes) {
            if (Arrays.binarySearch(RFC_5987_ATTR_CHARS, b) >= 0) {
                sb.append((char) b);
            } else {
                sb.append('%');
                sb.append(HEX_DIGITS[0x0f & (b >>> 4)]);
                sb.append(HEX_DIGITS[b & 0x0f]);
            }
        }
        return sb.toString();
    }

    public static Path moveIntoBasePath(final Path basePath, final Path path) {
        final Path newPath = createTempFile(basePath);
        try {
            Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new IllegalStateException("Could not move file " + path + " into directory" + basePath, e);
        }
        return newPath;
    }

    public static Path createTempFile(final Path basePath) {
        Path newPath;
        do {
            final String uuid = UUID.randomUUID().toString().replace("-", basePath.getFileSystem().getSeparator());
            newPath = basePath.resolve(uuid);
        } while (!createFileAndParents(newPath));
        return newPath;
    }

    private static boolean createFileAndParents(final Path path) {
        try {
            ensureDirectoryExists(path.getParent());
            return createFile(path, PosixFilePermissions.asFileAttribute(
                    new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))));
        } catch (final IOException e) {
            throw new IllegalStateException("Could not create file " + path, e);
        }
    }

    private static boolean createFile(final Path path, final FileAttribute<?>... fileAttributes) throws IOException {
        try {
            Files.createFile(path, fileAttributes);
            return true;
        } catch (final FileAlreadyExistsException e) {
            return false;
        }
    }

    public static void ensureDirectoryExists(final Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path, PosixFilePermissions.asFileAttribute(new HashSet<>(
                    Arrays.asList(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE))));
        }
    }

    /**
     * This is a front-end to {@link Path#toRealPath(LinkOption...)} which can handle non-existent paths.
     */
    public static Path getRealPath(final File file) {
        if (file == null) {
            return null;
        }
        return getRealPath(file.toPath());
    }

    public static Path getRealPath(final Path path) {
        if (path == null) {
            return null;
        }
        try {
            return path.toRealPath();
        } catch (NoSuchFileException | FileNotFoundException e) {
            final Path absolutePath = path.toAbsolutePath().normalize();
            Path parent = absolutePath.getParent();
            while (parent != null && !Files.exists(parent)) {
                parent = parent.getParent();
            }
            if (parent == null) {
                throw new IllegalStateException("Could not resolve root of " + path, e);
            }
            return getRealPath(parent).resolve(parent.relativize(absolutePath));
        } catch (final IOException e) {
            throw new IllegalStateException("Could not resolve path of " + path, e);
        }
    }

    /**
     * As there is no ByteStream we use an {@link IntStream} instead.
     */
    public static IntStream streamOfUnsigned(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return IntStream.empty();
        }
        return IntStream.range(0, bytes.length).map(idx -> Byte.toUnsignedInt(bytes[idx]));
    }

    /**
     * Replaces the contents of a file with the provided String.
     *
     * @return {@code true}, if the file was changed
     */
    public static boolean ensureFileContains(final Path targetPath, final String newContents) {
        final boolean oldFile;
        try {
            oldFile = !createFile(targetPath);
        } catch (final IOException e) {
            throw new IllegalStateException("Could not check for existence of " + targetPath + " or create it", e);
        }
        final byte[] newContentsBytes = newContents.getBytes(StandardCharsets.UTF_8);
        if (oldFile) {
            try {
                if (Files.size(targetPath) == newContentsBytes.length) {
                    final byte[] oldContents = Files.readAllBytes(targetPath);
                    if (oldContents != null && Arrays.equals(oldContents, newContentsBytes)) {
                        return false;
                    }
                }
            } catch (final IOException e) {
                throw new IllegalStateException("Could not read " + targetPath, e);
            }
        }
        try {
            Files.write(targetPath, newContentsBytes, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (final IOException e) {
            throw new IllegalStateException("Could not write " + targetPath, e);
        }
    }

    public static int addressAsInt(final Inet4Address ipv4Address) {
        return streamOfUnsigned(ipv4Address.getAddress()).reduce(0, (a, b) -> (a << 8) + b);
    }

    public static void linkOrCopy(final Path linkPath, final Path linkTargetPath) {
        final Path parent = linkPath.getParent();
        try {
            try {
                if (!Files.isSymbolicLink(linkPath) || !parent
                        .resolve(Files.readSymbolicLink(linkPath))
                        .normalize()
                        .equals(linkTargetPath)) {
                    Files.deleteIfExists(linkPath);
                    Files.createSymbolicLink(linkPath, parent.relativize(linkTargetPath));
                }
            } catch (final UnsupportedOperationException e) {
                // This will happen when symbolic links are not supported
                Files.copy(linkTargetPath, linkPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Could not create alias " + linkPath, e);
        }
    }

    public static String escapeLikeString(final String input) {
        return LIKE_ESCAPE_PATTERN.matcher(input).replaceAll(LIKE_ESCAPE_REPLACE);
    }
}
