package de.uni_muenster.cs.comsys.tbmgmt.web.support;

import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.webjars.WebJarAssetLocator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by matthias on 18.02.2015.
 */
@Service
public class WebJarUrlUtil {

    public static final String WEBJARS_URL_PREFIX = "/webjars";
    private static final Logger logger = LoggerFactory.getLogger(WebJarUrlUtil.class);
    private static final ConcurrentMap<String, ConcurrentMap<String, String>> INTEGRITY_MAP;
    private static final ConcurrentMap<String, ConcurrentMap<String, String>> FULL_PATH_MAP;

    static {
        // Without a maximum size set this only acts as a way to reduce OutOfMemoryErrors
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder().softValues();
        INTEGRITY_MAP = cacheBuilder.<String, ConcurrentMap<String, String>>build().asMap();
        FULL_PATH_MAP = cacheBuilder.<String, ConcurrentMap<String, String>>build().asMap();
    }

    /**
     * Builder for the individual per-web-jar-caches. Small initial size and concurrency as usually there are few
     * files used from each web-jar.
     */
    private static final CacheBuilder<Object, Object> SUB_CACHE_BUILDER =
            CacheBuilder.newBuilder().softValues().concurrencyLevel(1).initialCapacity(4);
    private static final Function<String, ConcurrentMap<String, String>> SUB_MAP_CONSTRUCTOR =
            parentKey -> SUB_CACHE_BUILDER.<String, String>build().asMap();

    private final WebJarAssetLocator webJarAssetLocator;
    private final ResourcePatternResolver resourcePatternResolver;


    @Autowired
    public WebJarUrlUtil(final WebJarAssetLocator webJarAssetLocator,
                         final ResourcePatternResolver resourcePatternResolver) {
        this.webJarAssetLocator = webJarAssetLocator;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public String getUrl(final String webJar, final String resource) {
        final String fullPath = getFullPathExact(webJar, resource);
        return WEBJARS_URL_PREFIX + fullPath.substring(WebJarAssetLocator.WEBJARS_PATH_PREFIX.length());
    }

    public String getIntegrity(final String webJar, final String resource) {
        final ConcurrentMap<String, String> integrityMap = INTEGRITY_MAP.computeIfAbsent(webJar, SUB_MAP_CONSTRUCTOR);
        final String currentValue = integrityMap.get(resource);
        if (currentValue != null) {
            return currentValue;
        }

        final String fullPath = getFullPathExact(webJar, resource);
        final Resource[] resources;
        try {
            resources =
                    resourcePatternResolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + fullPath);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Could not find a webjar-resource for " + webJar + ", " + resource, e);
        }
        if (resources == null || resources.length < 1) {
            throw new IllegalArgumentException("Could not find a webjar-resource for " + webJar + ", " + resource);
        }
        if (resources.length > 1) {
            logger.warn("Found multiple resources " + Arrays.toString(resources) + " for the webjar-resource " + webJar
                    + ", " + resource + " this means we will possibly provide multiple sets of hashes for this "
                    + "resource where probably only one will ever match");
        }

        final String integrityString;
        try {
            integrityString = getIntegrityString(resources);
        } catch (final Exception e) {
            throw new IllegalStateException(
                    "Exception calculating integrity-String for \"" + webJar + "\"'s resource " + "\"" + resource
                            + "\"");
        }

        integrityMap.putIfAbsent(resource, integrityString);
        return integrityString;
    }

    public static String getIntegrityString(final Resource[] resources) {
        if (resources.length == 1) {
            return getIntegrityString(resources[0]);
        }
        return Arrays
                .stream(resources)
                .map(WebJarUrlUtil::getIntegrityString)
                .distinct()
                .collect(Collectors.joining("\n"));
    }

    public static String getIntegrityString(final Resource resource) {
        final String sha256Hash;
        final String sha384Hash;
        final String sha512Hash;
        try {
            final HashFunction sha256 = Hashing.sha256();
            final HashFunction sha384 = Hashing.sha384();
            final HashFunction sha512 = Hashing.sha512();
            // We will calculate all hashes currently required to be present in browsers as posted in
            // https://w3c.github.io/webappsec-subresource-integrity/#cryptographic-hash-functions to get the
            // greatest possible coverage
            try (final HashingInputStream sha256stream = new HashingInputStream(sha256, resource.getInputStream());
                 final HashingInputStream sha384stream = new HashingInputStream(sha384, sha256stream);
                 final HashingInputStream sha512stream = new HashingInputStream(sha512, sha384stream)) {

                // Read and discard the whole file so the contents get passed through the hash-functions
                final byte[] buffer = new byte[8192];
                int read;
                do {
                    read = sha512stream.read(buffer, 0, buffer.length);
                } while (read > 0);

                final Base64.Encoder encoder = Base64.getEncoder();
                sha256Hash = encoder.encodeToString(sha256stream.hash().asBytes());
                sha384Hash = encoder.encodeToString(sha384stream.hash().asBytes());
                sha512Hash = encoder.encodeToString(sha512stream.hash().asBytes());
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Error while hashing " + resource, e);
        }
        return "sha256-" + sha256Hash + " sha384-" + sha384Hash + " sha512-" + sha512Hash;
    }

    public String getFullPathExact(final String webJar, final String resource) {
        final ConcurrentMap<String, String> fullPathMap = FULL_PATH_MAP.computeIfAbsent(webJar, SUB_MAP_CONSTRUCTOR);
        final String currentValue = fullPathMap.get(resource);
        if (currentValue != null) {
            return currentValue;
        }

        final String fullPathExact = webJarAssetLocator.getFullPathExact(webJar, resource);
        if (fullPathExact == null) {
            throw new IllegalStateException(
                    String.format("WebJar \"%s\" version \"%s\" does not contain \"%s\"", webJar,
                            webJarAssetLocator.getWebJars().get(webJar), resource));
        }

        fullPathMap.putIfAbsent(resource, fullPathExact);
        return fullPathExact;
    }
}
