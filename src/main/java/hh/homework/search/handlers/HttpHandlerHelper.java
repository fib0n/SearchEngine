package hh.homework.search.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fib on 08/02/15.
 */
class HttpHandlerHelper {

    private HttpHandlerHelper() {
    }

    public static Map<String, String> parseQuery(final String query) {
        final Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (final String param : query.split("&")) {
                final String pair[] = param.split("=");
                if (pair.length > 1) {
                    result.put(pair[0], pair[1]);
                } else {
                    result.put(pair[0], "");
                }
            }
        }
        return result;
    }

    public static <E> void writeResponse(final HttpExchange httpExchange, final E obj) throws IOException {
        final Headers responseHeaders = httpExchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try (final OutputStream responseBody = httpExchange.getResponseBody()) {
            final StringWriter out = new StringWriter();
            JSONValue.writeJSONString(obj, out);
            final String jsonText = out.toString();
            final byte[] responseBytes = jsonText.getBytes();
            httpExchange.sendResponseHeaders(200, responseBytes.length);
            responseBody.write(responseBytes);
        }
    }
}
