import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class WebApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            FitnessApiServer.handleOptions(exchange);
            return;
        }
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if (path.equals("/exercises") && method.equals("GET")) {
                FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.exercises());
            } else if (path.equals("/programs") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                int userId = query != null && query.startsWith("userId=") ? Integer.parseInt(query.substring(7)) : 0;
                FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.programs(userId));
            } else if (path.equals("/programs/split") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query == null || !query.startsWith("days=")) throw new IllegalArgumentException("Укажите число дней");
                int days = Integer.parseInt(query.substring(5));
                FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.recommend(days));
            } else if (path.equals("/programs/custom") && method.equals("POST")) {
                Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                FitnessApiServer.sendJson(exchange, 201, FitnessApiServer.facade.saveCustomProgram(body));
            } else if (path.matches("/programs/custom/custom-\\d+") && method.equals("DELETE")) {
                String query = exchange.getRequestURI().getQuery();
                if (query == null || !query.startsWith("userId=")) throw new IllegalArgumentException("Укажите пользователя");
                int userId = Integer.parseInt(query.substring(7));
                String programId = path.substring("/programs/custom/".length());
                if (FitnessApiServer.facade.deleteCustomProgram(userId, programId)) {
                    FitnessApiServer.sendJson(exchange, 200, java.util.Collections.singletonMap("message", "Программа удалена"));
                } else FitnessApiServer.sendError(exchange, 404, "Программа не найдена");
            } else if (path.equals("/workouts/build") && method.equals("POST")) {
                Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.build(body));
            } else if (path.equals("/workouts/built") && method.equals("POST")) {
                Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                FitnessApiServer.sendJson(exchange, 201, FitnessApiServer.facade.saveWorkout(body));
            } else if (path.matches("/workouts/built/\\d+") && method.equals("DELETE")) {
                int id = Integer.parseInt(path.substring("/workouts/built/".length()));
                if (FitnessApiServer.facade.deleteWorkout(id)) FitnessApiServer.sendJson(exchange, 200, java.util.Collections.singletonMap("message", "Тренировка удалена"));
                else FitnessApiServer.sendError(exchange, 404, "Тренировка не найдена");
            } else {
                FitnessApiServer.sendError(exchange, 404, "Адрес не найден");
            }
        } catch (IllegalArgumentException e) {
            FitnessApiServer.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            FitnessApiServer.sendError(exchange, 500, "Не удалось выполнить запрос");
        }
    }
}
