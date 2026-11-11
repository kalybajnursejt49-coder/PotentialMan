import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class WorkoutsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("OPTIONS")) {
            FitnessApiServer.handleOptions(exchange);
            return;
        }

        if (method.equals("POST") && path.equals("/workouts")) {
            handleCreateWorkout(exchange);
        } else if (method.equals("DELETE") && path.matches("/workouts/\\d+")) {
            int workoutId = Integer.parseInt(path.substring("/workouts/".length()));
            handleDeleteWorkout(exchange, workoutId);
        } else {
            FitnessApiServer.sendResponse(exchange, 404, "{\"error\":\"not found\"}");
        }
    }

    private void handleCreateWorkout(HttpExchange exchange) throws IOException {
        String body = FitnessApiServer.readBody(exchange);
        Map<String, String> data = JsonUtil.parse(body);

        int userId = Integer.parseInt(data.get("userId"));
        String type = data.get("type");
        int duration = Integer.parseInt(data.get("duration"));
        int calories = Integer.parseInt(data.get("calories"));

        WorkoutPlan plan;
        if (type.equalsIgnoreCase("Cardio")) {
            double distance = Double.parseDouble(data.get("distance"));
            plan = FitnessApiServer.facade.createCardioWorkout(duration, calories, distance);
        } else {
            int sets = Integer.parseInt(data.get("sets"));
            int reps = Integer.parseInt(data.get("reps"));
            plan = FitnessApiServer.facade.createStrengthWorkout(duration, calories, sets, reps);
        }

        plan.setUserId(userId);
        FitnessApiServer.facade.saveLegacyWorkout(plan);

        FitnessApiServer.sendResponse(exchange, 201, plan.toJson());
    }

    private void handleDeleteWorkout(HttpExchange exchange, int workoutId) throws IOException {
        FitnessApiServer.facade.deleteLegacyWorkout(workoutId);
        FitnessApiServer.sendResponse(exchange, 200, "{\"message\":\"тренировка удалена\"}");
    }
}
