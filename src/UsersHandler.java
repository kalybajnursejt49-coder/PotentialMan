import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UsersHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            FitnessApiServer.handleOptions(exchange);
            return;
        }
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/users") && method.equals("GET")) {
                FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.users());
            } else if (path.equals("/users") && method.equals("POST")) {
                Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                FitnessApiServer.sendJson(exchange, 201, FitnessApiServer.facade.saveUser(body, 0));
            } else if (path.matches("/users/\\d+")) {
                int id = Integer.parseInt(path.substring(7));
                if (method.equals("GET")) FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.profile(id));
                else if (method.equals("PUT")) {
                    Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                    FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.saveUser(body, id));
                } else FitnessApiServer.sendError(exchange, 405, "Метод не поддерживается");
            } else if (path.matches("/users/\\d+/journal")) {
                int id = Integer.parseInt(path.split("/")[2]);
                if (method.equals("GET")) FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.journal(id));
                else if (method.equals("PUT")) {
                    Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                    FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.saveJournal(id, body));
                } else FitnessApiServer.sendError(exchange, 405, "Метод не поддерживается");
            } else if (path.matches("/users/\\d+/journal/\\d{4}-\\d{2}-\\d{2}/sleep")) {
                String[] parts = path.split("/");
                int id = Integer.parseInt(parts[2]);
                if (method.equals("DELETE") && FitnessApiServer.facade.deleteSleep(id, parts[4])) {
                    FitnessApiServer.sendJson(exchange, 200, java.util.Collections.singletonMap("message", "Сон удалён"));
                } else FitnessApiServer.sendError(exchange, 404, "Запись сна не найдена");
            } else if (path.matches("/users/\\d+/meals")) {
                int id = Integer.parseInt(path.split("/")[2]);
                if (method.equals("POST")) {
                    Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                    FitnessApiServer.sendJson(exchange, 201, FitnessApiServer.facade.addMeal(id, body));
                } else FitnessApiServer.sendError(exchange, 405, "Метод не поддерживается");
            } else if (path.matches("/users/\\d+/meals/\\d{4}-\\d{2}-\\d{2}/\\d+")) {
                String[] parts = path.split("/");
                int id = Integer.parseInt(parts[2]);
                int mealId = Integer.parseInt(parts[5]);
                if (method.equals("DELETE") && FitnessApiServer.facade.deleteMeal(id, parts[4], mealId)) {
                    FitnessApiServer.sendJson(exchange, 200, java.util.Collections.singletonMap("message", "Запись удалена"));
                } else FitnessApiServer.sendError(exchange, 404, "Запись не найдена");
            } else if (path.matches("/users/\\d+/strength")) {
                int id = Integer.parseInt(path.split("/")[2]);
                if (method.equals("GET")) FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.strength(id));
                else if (method.equals("POST")) {
                    Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                    FitnessApiServer.sendJson(exchange, 201, FitnessApiServer.facade.saveStrengthRecord(id, body));
                } else FitnessApiServer.sendError(exchange, 405, "Метод не поддерживается");
            } else if (path.matches("/users/\\d+/strength/goals")) {
                int id = Integer.parseInt(path.split("/")[2]);
                if (method.equals("PUT")) {
                    Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                    FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.saveStrengthGoal(id, body));
                } else FitnessApiServer.sendError(exchange, 405, "Метод не поддерживается");
            } else if (path.matches("/users/\\d+/strength/\\d+")) {
                String[] parts = path.split("/");
                int id = Integer.parseInt(parts[2]);
                int recordId = Integer.parseInt(parts[4]);
                if (method.equals("PUT")) {
                    Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                    Map<String, Object> updated = FitnessApiServer.facade.updateStrengthRecord(id, recordId, body);
                    if (updated == null) FitnessApiServer.sendError(exchange, 404, "Запись не найдена");
                    else FitnessApiServer.sendJson(exchange, 200, updated);
                } else if (method.equals("DELETE") && FitnessApiServer.facade.deleteStrengthRecord(id, recordId)) {
                    FitnessApiServer.sendJson(exchange, 200, java.util.Collections.singletonMap("message", "Вес удалён"));
                } else FitnessApiServer.sendError(exchange, 404, "Запись не найдена");
            } else if (path.matches("/users/\\d+/week") && method.equals("GET")) {
                int id = Integer.parseInt(path.split("/")[2]);
                FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.weekSummary(id));
            } else if (path.matches("/users/\\d+/body-weight")) {
                int id = Integer.parseInt(path.split("/")[2]);
                if (method.equals("GET")) FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.bodyWeights(id));
                else if (method.equals("PUT")) {
                    Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                    FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.saveBodyWeight(id, body));
                } else FitnessApiServer.sendError(exchange, 405, "Метод не поддерживается");
            } else if (path.matches("/users/\\d+/body-weight/\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = path.split("/");
                int id = Integer.parseInt(parts[2]);
                if (method.equals("DELETE") && FitnessApiServer.facade.deleteBodyWeight(id, parts[4])) {
                    FitnessApiServer.sendJson(exchange, 200, java.util.Collections.singletonMap("message", "Запись удалена"));
                } else FitnessApiServer.sendError(exchange, 404, "Запись не найдена");
            } else if (path.matches("/users/\\d+/export") && method.equals("GET")) {
                int id = Integer.parseInt(path.split("/")[2]);
                FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.exportData(id));
            } else if (path.matches("/users/\\d+/templates")) {
                int id = Integer.parseInt(path.split("/")[2]);
                if (method.equals("GET")) FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.templates(id));
                else if (method.equals("PUT")) {
                    Map<String, Object> body = WebStore.object(WebJson.parse(FitnessApiServer.readBody(exchange)));
                    FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.saveTemplate(id, body));
                } else FitnessApiServer.sendError(exchange, 405, "Метод не поддерживается");
            } else if (path.matches("/users/\\d+/templates/[^/]+/\\d+") && method.equals("DELETE")) {
                String[] parts = path.split("/");
                int id = Integer.parseInt(parts[2]);
                int dayIndex = Integer.parseInt(parts[5]);
                if (FitnessApiServer.facade.deleteTemplate(id, parts[4], dayIndex)) {
                    FitnessApiServer.sendJson(exchange, 200, java.util.Collections.singletonMap("message", "Шаблон удалён"));
                } else FitnessApiServer.sendError(exchange, 404, "Шаблон не найден");
            } else if (path.matches("/users/\\d+/(history|workouts|recovery|nutrition)")) {
                String[] parts = path.split("/");
                int id = Integer.parseInt(parts[2]);
                if (!method.equals("GET")) {
                    FitnessApiServer.sendError(exchange, 405, "Метод не поддерживается");
                } else if (parts[3].equals("history") || parts[3].equals("workouts")) {
                    FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.history(id));
                } else if (parts[3].equals("recovery")) {
                    FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.recovery(id));
                } else {
                    String query = exchange.getRequestURI().getRawQuery();
                    String goal = "maintain";
                    if (query != null && query.startsWith("goal=")) goal = URLDecoder.decode(query.substring(5), StandardCharsets.UTF_8);
                    FitnessApiServer.sendJson(exchange, 200, FitnessApiServer.facade.nutrition(id, goal));
                }
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
