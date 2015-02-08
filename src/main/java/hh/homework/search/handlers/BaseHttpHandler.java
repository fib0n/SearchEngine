package hh.homework.search.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import hh.homework.search.Manager;

import java.io.IOException;

/**
 * Created by fib on 08/02/15.
 */
abstract class BaseHttpHandler implements HttpHandler {
    final Manager manager;

    BaseHttpHandler(final Manager manager) {
        this.manager = manager;
    }

    @Override
    public abstract void handle(HttpExchange httpExchange) throws IOException;
}
