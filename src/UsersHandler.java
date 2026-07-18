import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UsersHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET") && path.equals("/users")) {
            handleGetAllUsers(exchange);
        } else if (method.equals("GET") && path.matches("/users/\\d+/workouts")) {
            int userId = extractId(path, "/users/", "/workouts");
            handleGetUserWorkouts(exchange, userId);
        } else if (method.equals("POST") && path.equals("/users")) {
            handleCreateUser(exchange);
        } else if (method.equals("PUT") && path.matches("/users/\\d+")) {
            int userId = extractId(path, "/users/", "");
            handleUpdateWeight(exchange, userId);
        } else {
            FitnessApiServer.sendResponse(exchange, 404, "{\"error\":\"not found\"}");
        }
    }

    private int extractId(String path, String prefix, String suffix) {
        String idPart = path.substring(prefix.length());
        if (!suffix.isEmpty()) {
            idPart = idPart.substring(0, idPart.length() - suffix.length());
        }
        return Integer.parseInt(idPart);
    }

    private void handleGetAllUsers(HttpExchange exchange) throws IOException {
        List<User> users = FitnessDatabase.getAllUsers();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            json.append(users.get(i).toJson());
            if (i < users.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        FitnessApiServer.sendResponse(exchange, 200, json.toString());
    }

    private void handleGetUserWorkouts(HttpExchange exchange, int userId) throws IOException {
        List<WorkoutPlan> plans = FitnessDatabase.getWorkoutsByUserId(userId);
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < plans.size(); i++) {
            json.append(plans.get(i).toJson());
            if (i < plans.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        FitnessApiServer.sendResponse(exchange, 200, json.toString());
    }

    private void handleCreateUser(HttpExchange exchange) throws IOException {
        String body = FitnessApiServer.readBody(exchange);
        Map<String, String> data = JsonUtil.parse(body);

        int age = Integer.parseInt(data.get("age"));
        double weight = Double.parseDouble(data.get("weight"));
        double height = Double.parseDouble(data.get("height"));
        String gender = data.get("gender");

        User user = new User(age, weight, height, gender);
        FitnessDatabase.saveUser(user);

        FitnessApiServer.sendResponse(exchange, 201, user.toJson());
    }

    private void handleUpdateWeight(HttpExchange exchange, int userId) throws IOException {
        String body = FitnessApiServer.readBody(exchange);
        Map<String, String> data = JsonUtil.parse(body);
        double newWeight = Double.parseDouble(data.get("weight"));

        FitnessDatabase.updateUserWeight(userId, newWeight);

        FitnessApiServer.sendResponse(exchange, 200, "{\"message\":\"вес обновлён\"}");
    }
}