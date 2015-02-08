package hh.homework.search.handlers;

import com.sun.net.httpserver.HttpExchange;
import hh.homework.search.Manager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by fib on 08/02/15.
 */
public class IndexHttpHandler extends BaseHttpHandler<Long> {
    private final JSONParser jsonParser;

    public IndexHttpHandler(Manager<Long> manager) {
        super(manager);
        jsonParser = new JSONParser();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            final String requestMethod = httpExchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("POST")) {
                final StringBuilder sb = new StringBuilder();
                String line;
                try (final BufferedReader br = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()))) {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                final JSONObject json = (JSONObject) jsonParser.parse(sb.toString());
                final Long id = Long.valueOf(json.get("id").toString());
                if (id != null) {
                    manager.insertDocument(id, json.get("text").toString());
                    final JSONObject response = new JSONObject();
                    //noinspection unchecked
                    response.put("status", "ok");
                    HttpHandlerHelper.writeResponse(httpExchange, response);
                }
                else {
                    httpExchange.sendResponseHeaders(500, 0);
                }
            } else {
                httpExchange.sendResponseHeaders(404, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            httpExchange.sendResponseHeaders(500, 0);
            throw e;
        } catch (Exception e) {
            httpExchange.sendResponseHeaders(500, 0);
            e.printStackTrace();
        } finally {
            httpExchange.close();
        }
    }
}