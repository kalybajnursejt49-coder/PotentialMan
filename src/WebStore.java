import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebStore {
    private final Path file = Path.of("data", "web-data.json");
    private final List<Object> users = new ArrayList<>();
    private final List<Object> workouts = new ArrayList<>();
    private final List<Object> templates = new ArrayList<>();
    private final List<Object> customPrograms = new ArrayList<>();
    private int nextUserId = 1;
    private int nextWorkoutId = 1;
    private int nextCustomProgramId = 1;

    public WebStore() throws IOException {
        if (Files.exists(file)) {
            try {
                Map<String, Object> data = object(WebJson.parse(Files.readString(file, StandardCharsets.UTF_8)));
                users.addAll(array(data.get("users")));
                workouts.addAll(array(data.get("workouts")));
                if (data.get("templates") != null) templates.addAll(array(data.get("templates")));
                if (data.get("customPrograms") != null) customPrograms.addAll(array(data.get("customPrograms")));
                nextUserId = number(data.get("nextUserId"));
                nextWorkoutId = number(data.get("nextWorkoutId"));
                if (data.get("nextCustomProgramId") != null) nextCustomProgramId = number(data.get("nextCustomProgramId"));
            } catch (RuntimeException e) {
                throw new IOException("Не удалось прочитать сохранённые данные", e);
            }
        }
    }

    public synchronized List<Object> getUsers() { return new ArrayList<>(users); }

    public synchronized Map<String, Object> getUser(int id) {
        for (Object item : users) {
            Map<String, Object> user = object(item);
            if (number(user.get("id")) == id) return new LinkedHashMap<>(user);
        }
        return null;
    }

    public synchronized Map<String, Object> saveUser(Map<String, Object> data, int id) throws IOException {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", id == 0 ? nextUserId++ : id);
        user.put("age", data.get("age"));
        user.put("weight", data.get("weight"));
        user.put("height", data.get("height"));
        user.put("gender", data.get("gender"));
        user.put("activityLevel", data.get("activityLevel"));
        user.put("stepsPerDay", data.get("stepsPerDay"));
        user.put("workoutsPerWeek", data.get("workoutsPerWeek"));
        user.put("trainingIntensity", data.get("trainingIntensity"));
        if (id == 0) users.add(user);
        else {
            boolean found = false;
            for (int i = 0; i < users.size(); i++) {
                if (number(object(users.get(i)).get("id")) == id) {
                    users.set(i, user);
                    found = true;
                    break;
                }
            }
            if (!found) throw new IllegalArgumentException("Пользователь не найден");
        }
        write();
        return new LinkedHashMap<>(user);
    }

    public synchronized List<Object> getWorkouts(int userId) {
        List<Object> result = new ArrayList<>();
        for (Object item : workouts) {
            Map<String, Object> workout = object(item);
            if (number(workout.get("userId")) == userId) result.add(new LinkedHashMap<>(workout));
        }
        return result;
    }

    public synchronized Map<String, Object> saveWorkout(Map<String, Object> data) throws IOException {
        Map<String, Object> workout = new LinkedHashMap<>(data);
        workout.put("id", nextWorkoutId++);
        workouts.add(workout);
        write();
        return workout;
    }

    public synchronized boolean deleteWorkout(int id) throws IOException {
        for (int i = 0; i < workouts.size(); i++) {
            if (number(object(workouts.get(i)).get("id")) == id) {
                workouts.remove(i);
                write();
                return true;
            }
        }
        return false;
    }

    public synchronized List<Object> getTemplates(int userId) {
        List<Object> result = new ArrayList<>();
        for (Object value : templates) {
            Map<String, Object> item = object(value);
            if (number(item.get("userId")) == userId) result.add(new LinkedHashMap<>(item));
        }
        return result;
    }

    public synchronized Map<String, Object> saveTemplate(Map<String, Object> data) throws IOException {
        int userId = number(data.get("userId"));
        String programId = String.valueOf(data.get("programId"));
        int dayIndex = number(data.get("dayIndex"));
        for (int i = 0; i < templates.size(); i++) {
            Map<String, Object> old = object(templates.get(i));
            if (number(old.get("userId")) == userId && String.valueOf(old.get("programId")).equals(programId)
                    && number(old.get("dayIndex")) == dayIndex) {
                templates.set(i, new LinkedHashMap<>(data));
                write();
                return new LinkedHashMap<>(data);
            }
        }
        templates.add(new LinkedHashMap<>(data));
        write();
        return new LinkedHashMap<>(data);
    }

    public synchronized boolean deleteTemplate(int userId, String programId, int dayIndex) throws IOException {
        for (int i = 0; i < templates.size(); i++) {
            Map<String, Object> item = object(templates.get(i));
            if (number(item.get("userId")) == userId && String.valueOf(item.get("programId")).equals(programId)
                    && number(item.get("dayIndex")) == dayIndex) {
                templates.remove(i);
                write();
                return true;
            }
        }
        return false;
    }

    public synchronized List<Object> getCustomPrograms(int userId) {
        List<Object> result = new ArrayList<>();
        for (Object value : customPrograms) {
            Map<String, Object> program = object(value);
            if (number(program.get("userId")) == userId) result.add(new LinkedHashMap<>(program));
        }
        return result;
    }

    public synchronized Map<String, Object> saveCustomProgram(Map<String, Object> data) throws IOException {
        Map<String, Object> program = new LinkedHashMap<>(data);
        program.put("id", "custom-" + nextCustomProgramId++);
        customPrograms.add(program);
        write();
        return new LinkedHashMap<>(program);
    }

    public synchronized boolean deleteCustomProgram(int userId, String programId) throws IOException {
        for (int i = 0; i < customPrograms.size(); i++) {
            Map<String, Object> program = object(customPrograms.get(i));
            if (number(program.get("userId")) == userId && String.valueOf(program.get("id")).equals(programId)) {
                customPrograms.remove(i);
                for (int j = templates.size() - 1; j >= 0; j--) {
                    Map<String, Object> template = object(templates.get(j));
                    if (number(template.get("userId")) == userId && String.valueOf(template.get("programId")).equals(programId)) {
                        templates.remove(j);
                    }
                }
                write();
                return true;
            }
        }
        return false;
    }

    private void write() throws IOException {
        Files.createDirectories(file.getParent());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nextUserId", nextUserId);
        data.put("nextWorkoutId", nextWorkoutId);
        data.put("nextCustomProgramId", nextCustomProgramId);
        data.put("users", users);
        data.put("workouts", workouts);
        data.put("templates", templates);
        data.put("customPrograms", customPrograms);
        Path temp = file.resolveSibling("web-data.tmp");
        Files.writeString(temp, WebJson.stringify(data), StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.writeString(file, WebJson.stringify(data), StandardCharsets.UTF_8);
            Files.deleteIfExists(temp);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object value) {
        if (!(value instanceof Map)) throw new IllegalArgumentException("Ожидался объект JSON");
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> array(Object value) {
        if (!(value instanceof List)) throw new IllegalArgumentException("Ожидался список JSON");
        return (List<Object>) value;
    }

    public static int number(Object value) {
        if (!(value instanceof Number)) throw new IllegalArgumentException("Ожидалось число");
        return ((Number) value).intValue();
    }
}
