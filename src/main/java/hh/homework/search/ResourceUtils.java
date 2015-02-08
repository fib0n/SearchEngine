package hh.homework.search;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fib on 08/02/15.
 */
class ResourceUtils {
    private ResourceUtils() {
    }

    private static String read(final String resourceName) throws URISyntaxException, IOException {
        final URI resourceURI = Thread.currentThread().getContextClassLoader().getResource(resourceName).toURI();
        final File resourceFile = new File(resourceURI);
        return Files.toString(resourceFile, Charset.defaultCharset());
    }

    public static Set<String> readDistinctLines(final String resourceName) throws URISyntaxException, IOException {
        return new HashSet<>(Arrays.asList(read(resourceName).split("\\r?\\n")));
    }
}
