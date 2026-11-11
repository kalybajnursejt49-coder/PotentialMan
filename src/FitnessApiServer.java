import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class FitnessApiServer {
    static FitnessFacade facade;

    public static void main(String[] args) throws IOException {
        facade = new FitnessFacade(new WebStore());
        int port = Integer.getInteger("potentialman.port", 8080);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/users", new UsersHandler());
        server.createContext("/workouts", new WorkoutsHandler());
        server.createContext("/workouts/build", new WebApiHandler());
        server.createContext("/workouts/built", new WebApiHandler());
        server.createContext("/programs", new WebApiHandler());
        server.createContext("/exercises", new WebApiHandler());
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Сервер запущен: http://localhost:" + port + "/");
    }

    static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.close();
    }

    static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        sendResponse(exchange, statusCode, WebJson.stringify(body));
    }

    static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        java.util.Map<String, Object> error = new java.util.LinkedHashMap<>();
        error.put("error", message);
        sendJson(exchange, statusCode, error);
    }

    static void handleOptions(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}
