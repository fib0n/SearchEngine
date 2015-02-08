package hh.homework.search.handlers;

import com.sun.net.httpserver.HttpExchange;
import hh.homework.search.Manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by fib on 08/02/15.
 */
public class SearchHttpHandler extends BaseHttpHandler {
    public SearchHttpHandler(Manager manager) {
        super(manager);
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        try {

            final Map<String, String> params = HttpHandlerHelper.parseQuery(httpExchange.getRequestURI().getQuery());
            final String query = params.get("query");
            if (query != null && !query.isEmpty()) {
                final List<Long> documents = manager.searchDocuments(
                        params.get("query"),
                        params.get("logic"),
                        Integer.parseInt(params.get("count")));
                HttpHandlerHelper.writeResponse(httpExchange, documents);
            }else {
                httpExchange.sendResponseHeaders(500, 0);
            }
        }catch (IOException e) {
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
