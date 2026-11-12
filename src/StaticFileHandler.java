import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements HttpHandler {
    private final Path webFolder = Path.of("web").toAbsolutePath().normalize();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equals("OPTIONS")) {
            FitnessApiServer.handleOptions(exchange);
            return;
        }
        if (!method.equals("GET")) {
            FitnessApiServer.sendResponse(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        String requestedPath = exchange.getRequestURI().getPath();
        if (requestedPath.equals("/")) {
            requestedPath = "/index.html";
        }
        Path file = webFolder.resolve(requestedPath.substring(1)).normalize();
        if (!file.startsWith(webFolder) || !Files.isRegularFile(file)) {
            FitnessApiServer.sendResponse(exchange, 404, "{\"error\":\"not found\"}");
            return;
        }

        String fileName = file.getFileName().toString().toLowerCase();
        String contentType;
        if (fileName.endsWith(".html")) {
            contentType = "text/html; charset=utf-8";
        } else if (fileName.endsWith(".css")) {
            contentType = "text/css; charset=utf-8";
        } else if (fileName.endsWith(".js")) {
            contentType = "text/javascript; charset=utf-8";
        } else if (fileName.endsWith(".svg")) {
            contentType = "image/svg+xml";
        } else if (fileName.endsWith(".webmanifest")) {
            contentType = "application/manifest+json; charset=utf-8";
        } else if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else {
            FitnessApiServer.sendResponse(exchange, 404, "{\"error\":\"not found\"}");
            return;
        }

        byte[] bytes = Files.readAllBytes(file);
        FitnessApiServer.addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.close();
    }
}
