package hh.homework.search;

import com.sun.net.httpserver.HttpServer;
import hh.homework.search.handlers.IndexHttpHandler;
import hh.homework.search.handlers.SearchHttpHandler;

import java.net.InetSocketAddress;

/**
 * Created by fib on 08/02/15.
 */
class Main {
    public static void main(String[] args) throws Exception {
        final Manager manager = new Manager(ResourceUtils.readDistinctLines("StopWords.txt"));
        final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/search", new SearchHttpHandler(manager));
        server.createContext("/index", new IndexHttpHandler(manager));
        server.setExecutor(null);
        server.start();
    }
}
