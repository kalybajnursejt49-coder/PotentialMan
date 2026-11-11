import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FitnessFacade {
    private static final String[] MUSCLES = {"chest", "back", "shoulders", "biceps", "triceps", "abs", "legs"};
    private static final String[] MUSCLE_NAMES = {"Грудь", "Спина", "Плечи", "Бицепс", "Трицепс", "Пресс", "Ноги"};
    private static final String[][] EXERCISES = {
            {"bench", "Жим лёжа", "Грудь", "chest", "triceps"},
            {"incline_bench", "Жим на наклонной скамье", "Грудь", "chest", "shoulders"},
            {"dumbbell_bench", "Жим гантелей лёжа", "Грудь", "chest", "triceps"},
            {"incline_dumbbell", "Жим гантелей под наклоном", "Грудь", "chest", "shoulders"},
            {"pushups", "Отжимания", "Грудь", "chest", "shoulders"},
            {"weighted_pushups", "Отжимания с весом", "Грудь", "chest", "triceps"},
            {"fly", "Разведение гантелей", "Грудь", "chest", "shoulders"},
            {"butterfly", "Бабочка в тренажёре", "Грудь", "chest", "shoulders"},
            {"cable_crossover", "Сведение рук в кроссовере", "Грудь", "chest", "shoulders"},
            {"chest_dips", "Отжимания на брусьях", "Грудь", "chest", "triceps"},
            {"pullups", "Подтягивания", "Спина", "back", "biceps"},
            {"weighted_pullups", "Подтягивания с весом", "Спина", "back", "biceps"},
            {"chinups", "Подтягивания обратным хватом", "Спина", "back", "biceps"},
            {"row", "Тяга штанги", "Спина", "back", "biceps"},
            {"dumbbell_row", "Тяга гантели одной рукой", "Спина", "back", "biceps"},
            {"seated_cable_row", "Горизонтальная тяга блока", "Спина", "back", "biceps"},
            {"rowing_machine", "Гребля на тренажёре", "Спина", "back", "legs"},
            {"t_bar_row", "Тяга Т-грифа", "Спина", "back", "biceps"},
            {"latpull", "Тяга верхнего блока", "Спина", "back", "biceps"},
            {"straight_arm_pull", "Пуловер на блоке", "Спина", "back", "triceps"},
            {"press", "Жим над головой", "Плечи", "shoulders", "triceps"},
            {"dumbbell_press", "Жим гантелей сидя", "Плечи", "shoulders", "triceps"},
            {"arnold_press", "Жим Арнольда", "Плечи", "shoulders", "triceps"},
            {"raises", "Подъём гантелей в стороны", "Плечи", "shoulders", "chest"},
            {"front_raise", "Подъём гантелей перед собой", "Плечи", "shoulders", "chest"},
            {"rear_delt_fly", "Разведение на заднюю дельту", "Плечи", "shoulders", "back"},
            {"face_pull", "Тяга каната к лицу", "Плечи", "shoulders", "back"},
            {"curl", "Сгибание рук", "Бицепс", "biceps", "back"},
            {"scott_curl", "Сгибание рук на скамье Скотта", "Бицепс", "biceps", "back"},
            {"hammer_curl", "Молотковые сгибания", "Бицепс", "biceps", "back"},
            {"incline_curl", "Сгибание гантелей на наклонной скамье", "Бицепс", "biceps", "back"},
            {"triceps", "Разгибание рук", "Трицепс", "triceps", "shoulders"},
            {"rope_pushdown", "Разгибание с канатом", "Трицепс", "triceps", "shoulders"},
            {"skull_crusher", "Французский жим", "Трицепс", "triceps", "chest"},
            {"close_grip_bench", "Жим узким хватом", "Трицепс", "triceps", "chest"},
            {"squat", "Приседания", "Ноги", "legs", "abs"},
            {"front_squat", "Фронтальные приседания", "Ноги", "legs", "abs"},
            {"leg_press", "Жим ногами", "Ноги", "legs", "abs"},
            {"lunge", "Выпады", "Ноги", "legs", "abs"},
            {"deadlift", "Становая тяга", "Ноги", "legs", "back"},
            {"romanian_deadlift", "Румынская тяга", "Ноги", "legs", "back"},
            {"leg_extension", "Разгибание ног", "Ноги", "legs", "abs"},
            {"leg_curl", "Сгибание ног", "Ноги", "legs", "abs"},
            {"calf_raise", "Подъём на носки", "Ноги", "legs", "abs"},
            {"plank", "Планка", "Пресс", "abs", "shoulders"},
            {"crunch", "Скручивания", "Пресс", "abs", "chest"},
            {"hanging_leg_raise", "Подъём ног в висе", "Пресс", "abs", "legs"},
            {"cable_crunch", "Скручивания на блоке", "Пресс", "abs", "chest"}
    };

    private final WebStore store;
    private final Health health = new Health();

    public FitnessFacade() { this.store = null; }
    public FitnessFacade(WebStore store) { this.store = store; }

    public User createUser(int age, double weight, double height, String gender) {
        return new User(age, weight, height, gender);
    }

    public CardioWorkout createCardioWorkout(int duration, int calories, double distance) {
        return new CardioWorkout(duration, calories, distance);
    }

    public StrengthWorkout createStrengthWorkout(int duration, int calories, int sets, int reps) {
        return new StrengthWorkout(duration, calories, sets, reps);
    }

    public void addWorkout(User user, WorkoutPlan workout) { user.addWorkout(workout); }
    public List<WorkoutPlan> userWorkouts(User user) { return user.getWorkouts(); }
    public int totalCalories(User user) { return user.getTotalCalories(); }
    public String bmiCategory(User user) { return health.getBMICategory(user); }
    public double bmr(User user) { return health.calculateBMR(user); }
    public double dailyNorm(User user, String goal) { return health.calculateDailyNorm(user, goal); }
    public void saveLegacyUser(User user) { FitnessDatabase.saveUser(user); }
    public void saveLegacyWorkout(WorkoutPlan plan) { FitnessDatabase.saveWorkout(plan); }
    public List<User> legacyUsers() { return FitnessDatabase.getAllUsers(); }
    public List<WorkoutPlan> legacyWorkouts(int userId) { return FitnessDatabase.getWorkoutsByUserId(userId); }
    public void updateLegacyWeight(int userId, double weight) { FitnessDatabase.updateUserWeight(userId, weight); }
    public void deleteLegacyWorkout(int workoutId) { FitnessDatabase.deleteWorkout(workoutId); }

    public List<Object> users() { return store.getUsers(); }

    public Map<String, Object> profile(int id) {
        Map<String, Object> user = store.getUser(id);
        if (user == null) throw new IllegalArgumentException("Пользователь не найден");
        double weight = decimal(user.get("weight"));
        double height = decimal(user.get("height"));
        int age = WebStore.number(user.get("age"));
        double bmi = weight / Math.pow(height / 100.0, 2);
        double bmr = 10 * weight + 6.25 * height - 5 * age;
        if ("M".equals(user.get("gender"))) bmr += 5;
        else bmr -= 161;
        String chosenActivity = user.get("activityLevel") == null ? "auto" : String.valueOf(user.get("activityLevel"));
        int steps = user.get("stepsPerDay") == null ? 0 : WebStore.number(user.get("stepsPerDay"));
        int workouts = user.get("workoutsPerWeek") == null ? 0 : WebStore.number(user.get("workoutsPerWeek"));
        String intensity = user.get("trainingIntensity") == null ? "light" : String.valueOf(user.get("trainingIntensity"));
        String effectiveActivity = activity(chosenActivity, steps, workouts, intensity);
        user.put("activityLevel", chosenActivity);
        user.put("stepsPerDay", steps);
        user.put("workoutsPerWeek", workouts);
        user.put("trainingIntensity", intensity);
        user.put("effectiveActivity", effectiveActivity);
        user.put("activityFactor", activityFactor(effectiveActivity));
        user.put("bmi", round(bmi));
        user.put("bmr", Math.round(bmr));
        if (bmi < 18.5) user.put("bmiCategory", "Недостаточный вес");
        else if (bmi < 25) user.put("bmiCategory", "Норма");
        else if (bmi < 30) user.put("bmiCategory", "Избыточный вес");
        else user.put("bmiCategory", "Ожирение");
        return user;
    }

    public Map<String, Object> saveUser(Map<String, Object> data, int id) throws IOException {
        int age = WebStore.number(data.get("age"));
        double weight = decimal(data.get("weight"));
        double height = decimal(data.get("height"));
        String gender = String.valueOf(data.get("gender"));
        if (age < 12 || age > 100 || weight < 25 || weight > 400 || height < 100 || height > 250 || !(gender.equals("M") || gender.equals("F"))) {
            throw new IllegalArgumentException("Проверьте возраст, вес, рост и пол");
        }
        String activityLevel = data.get("activityLevel") == null ? "auto" : String.valueOf(data.get("activityLevel"));
        String intensity = data.get("trainingIntensity") == null ? "light" : String.valueOf(data.get("trainingIntensity"));
        int steps = data.get("stepsPerDay") == null ? 0 : WebStore.number(data.get("stepsPerDay"));
        int workouts = data.get("workoutsPerWeek") == null ? 0 : WebStore.number(data.get("workoutsPerWeek"));
        if (!(activityLevel.equals("auto") || activityLevel.equals("rest") || activityLevel.equals("light") || activityLevel.equals("moderate") || activityLevel.equals("high"))
                || !(intensity.equals("light") || intensity.equals("moderate") || intensity.equals("hard"))
                || steps < 0 || steps > 50000 || workouts < 0 || workouts > 14) {
            throw new IllegalArgumentException("Проверьте активность, шаги и тренировки в неделю");
        }
        data.put("activityLevel", activityLevel);
        data.put("stepsPerDay", steps);
        data.put("workoutsPerWeek", workouts);
        data.put("trainingIntensity", intensity);
        Map<String, Object> user = store.saveUser(data, id);
        return profile(WebStore.number(user.get("id")));
    }

    public List<Object> exercises() {
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < EXERCISES.length; i++) {
            result.add(map("id", EXERCISES[i][0], "name", EXERCISES[i][1], "group", EXERCISES[i][2]));
        }
        return result;
    }

    public List<Object> programs() {
        List<Object> result = new ArrayList<>();
        result.add(program(SplitFactory.forId("full").create()));
        result.add(program(SplitFactory.forId("upper").create()));
        result.add(program(SplitFactory.forId("classic").create()));
        result.add(program(SplitFactory.forId("ppl").create()));
        return result;
    }

    public List<Object> programs(int userId) {
        List<Object> result = programs();
        if (userId > 0) {
            if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
            result.addAll(store.getCustomPrograms(userId));
        }
        return result;
    }

    public Map<String, Object> saveCustomProgram(Map<String, Object> data) throws IOException {
        int userId = WebStore.number(data.get("userId"));
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Сначала создайте профиль");
        String name = String.valueOf(data.get("name")).trim();
        List<Object> selectedDays = WebStore.array(data.get("days"));
        if (name.isEmpty() || name.length() > 40 || selectedDays.isEmpty() || selectedDays.size() > 7) {
            throw new IllegalArgumentException("Введите название и выберите от 1 до 7 дней");
        }
        String[] weekdayNames = {"", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        boolean[] used = new boolean[8];
        List<Object> schedule = new ArrayList<>();
        for (Object value : selectedDays) {
            Map<String, Object> day = WebStore.object(value);
            int weekday = WebStore.number(day.get("weekday"));
            String label = String.valueOf(day.get("label")).trim();
            if (weekday < 1 || weekday > 7 || used[weekday] || label.isEmpty() || label.length() > 35) {
                throw new IllegalArgumentException("Проверьте дни и названия тренировок");
            }
            used[weekday] = true;
            schedule.add(weekdayNames[weekday] + " · " + label);
        }
        return store.saveCustomProgram(map("userId", userId, "name", name, "days", schedule.size(), "schedule", schedule));
    }

    public boolean deleteCustomProgram(int userId, String programId) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        return store.deleteCustomProgram(userId, programId);
    }

    public List<Object> templates(int userId) {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        return store.getTemplates(userId);
    }

    public Map<String, Object> saveTemplate(int userId, Map<String, Object> data) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Сначала создайте профиль");
        String programId = String.valueOf(data.get("programId"));
        int dayIndex = WebStore.number(data.get("dayIndex"));
        Map<String, Object> program = null;
        for (Object value : programs(userId)) {
            Map<String, Object> candidate = WebStore.object(value);
            if (String.valueOf(candidate.get("id")).equals(programId)) program = candidate;
        }
        if (program == null || dayIndex < 0 || dayIndex >= WebStore.array(program.get("schedule")).size()) {
            throw new IllegalArgumentException("Выберите день программы");
        }
        Map<String, Object> built = build(data);
        List<Object> exercises = WebStore.array(built.get("exercises"));
        if (exercises.isEmpty()) throw new IllegalArgumentException("Добавьте хотя бы одно упражнение");
        String name = data.get("name") == null ? "Моя тренировка" : String.valueOf(data.get("name")).trim();
        if (name.isEmpty() || name.length() > 50) throw new IllegalArgumentException("Проверьте название тренировки");
        return store.saveTemplate(map("userId", userId, "programId", programId, "dayIndex", dayIndex,
                "name", name, "exercises", exercises));
    }

    public boolean deleteTemplate(int userId, String programId, int dayIndex) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        return store.deleteTemplate(userId, programId, dayIndex);
    }

    public Map<String, Object> recommend(int days) {
        SplitStrategy strategy = SplitFactory.recommend(days);
        return map("recommendedId", strategy.getId(), "days", days);
    }

    public Map<String, Object> build(Map<String, Object> data) {
        List<Object> items = WebStore.array(data.get("exercises"));
        if (items.size() > 20) throw new IllegalArgumentException("Слишком много упражнений");
        WorkoutBuilder builder = new WorkoutBuilder(MUSCLES);
        LoadPanel panel = new LoadPanel();
        builder.addObserver(panel);
        for (Object value : items) {
            Map<String, Object> item = WebStore.object(value);
            String id = String.valueOf(item.get("id"));
            String[] exercise = findExercise(id);
            int sets = WebStore.number(item.get("sets"));
            int reps = WebStore.number(item.get("reps"));
            boolean drop = Boolean.TRUE.equals(item.get("dropSet"));
            boolean superset = Boolean.TRUE.equals(item.get("superSet"));
            boolean heavy = Boolean.TRUE.equals(item.get("heavyWeight"));
            builder.addExercise(exercise, sets, reps, drop, superset, heavy);
        }
        return map("load", panel.getLevels(), "calories", builder.getCalories(), "exercises", builder.getExercises());
    }

    public Map<String, Object> saveWorkout(Map<String, Object> data) throws IOException {
        int userId = WebStore.number(data.get("userId"));
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Сначала создайте профиль");
        Map<String, Object> built = build(data);
        if (WebStore.array(built.get("exercises")).isEmpty()) throw new IllegalArgumentException("Добавьте хотя бы одно упражнение");
        String day = String.valueOf(data.get("day"));
        if (day.length() > 50) throw new IllegalArgumentException("Слишком длинное название дня");
        LocalDate date;
        try {
            date = data.get("trainingDate") == null ? LocalDate.now() : LocalDate.parse(String.valueOf(data.get("trainingDate")));
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректная дата тренировки");
        }
        if (date.isAfter(LocalDate.now()) || date.isBefore(LocalDate.now().minusYears(3))) {
            throw new IllegalArgumentException("Выберите дату тренировки за последние три года");
        }
        Map<String, Object> result = map("userId", userId, "day", day, "date", date.atTime(LocalDateTime.now().toLocalTime()).toString(),
                "calories", built.get("calories"), "load", built.get("load"), "exercises", built.get("exercises"));
        return store.saveWorkout(result);
    }

    public List<Object> history(int userId) {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        List<Object> result = store.getWorkouts(userId);
        List<Object> reverse = new ArrayList<>();
        for (int i = result.size() - 1; i >= 0; i--) reverse.add(result.get(i));
        return reverse;
    }

    public boolean deleteWorkout(int id) throws IOException { return store.deleteWorkout(id); }

    public Map<String, Object> recovery(int userId) {
        Map<String, Object> profile = profile(userId);
        List<Object> workouts = history(userId);
        List<Object> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < MUSCLES.length; i++) {
            String muscle = MUSCLES[i];
            LocalDateTime latestReady = null;
            int latestLoad = 0;
            for (Object value : workouts) {
                Map<String, Object> workout = WebStore.object(value);
                int level = WebStore.number(WebStore.object(workout.get("load")).get(muscle));
                if (level == 0) continue;
                int hours = level >= 9 ? 72 : level >= 7 ? 48 : level >= 4 ? 30 : 12;
                if (WebStore.number(profile.get("age")) >= 40) hours += 8;
                LocalDateTime ready = LocalDateTime.parse(String.valueOf(workout.get("date"))).plusHours(hours);
                if (latestReady == null || ready.isAfter(latestReady)) {
                    latestReady = ready;
                    latestLoad = level;
                }
            }
            long remaining = latestReady == null ? 0 : Math.max(0, ChronoUnit.HOURS.between(now, latestReady) + 1);
            int total = latestLoad >= 9 ? 72 : latestLoad >= 7 ? 48 : latestLoad >= 4 ? 30 : 12;
            int progress = remaining == 0 ? 100 : Math.max(0, (int) (100 - remaining * 100.0 / total));
            result.add(map("id", muscle, "name", MUSCLE_NAMES[i], "ready", remaining == 0, "remainingHours", remaining, "progress", progress));
        }
        return map("muscles", result);
    }

    public Map<String, Object> nutrition(int userId, String goal) {
        Map<String, Object> user = profile(userId);
        if (!goal.equals("gain") && !goal.equals("lose") && !goal.equals("maintain")) throw new IllegalArgumentException("Неизвестная цель");
        double activityFactor = ((Number) user.get("activityFactor")).doubleValue();
        int target = Math.max(1200, (int) Math.round(WebStore.number(user.get("bmr")) * activityFactor + (goal.equals("gain") ? 500 : goal.equals("lose") ? -500 : 0)));
        double proteinFactor = proteinFactor(user);
        int protein = (int) Math.round(((Number) user.get("weight")).doubleValue() * proteinFactor);
        int fat = (int) Math.round(target * 0.25 / 9.0);
        int carbs = (int) Math.round((target - protein * 4 - fat * 9) / 4.0);
        int burned = 0;
        for (Object value : history(userId)) {
            Map<String, Object> workout = WebStore.object(value);
            if (String.valueOf(workout.get("date")).startsWith(LocalDate.now().toString())) burned += WebStore.number(workout.get("calories"));
        }
        return map("goal", goal, "calories", target, "protein", protein, "proteinPerKg", proteinFactor,
                "activityFactor", activityFactor, "activityLevel", user.get("effectiveActivity"), "fat", fat, "carbs", carbs,
                "burnedToday", burned, "remaining", target);
    }

    private String activity(String selected, int steps, int workouts, String intensity) {
        if (!selected.equals("auto")) return selected;
        if (steps >= 12000 || workouts >= 6 || (steps >= 9000 && workouts >= 4 && intensity.equals("hard"))) return "high";
        if (steps >= 7000 || workouts >= 3) return "moderate";
        if (steps >= 3500 || workouts >= 1) return "light";
        return "rest";
    }

    private double activityFactor(String level) {
        if (level.equals("high")) return 1.8;
        if (level.equals("moderate")) return 1.6;
        if (level.equals("light")) return 1.4;
        return 1.2;
    }

    private double proteinFactor(Map<String, Object> user) {
        String level = String.valueOf(user.get("effectiveActivity"));
        double factor = 1.0;
        if (level.equals("light")) factor = 1.3;
        if (level.equals("moderate")) factor = 1.6;
        if (level.equals("high")) factor = 1.8;
        if (String.valueOf(user.get("trainingIntensity")).equals("hard") && WebStore.number(user.get("workoutsPerWeek")) >= 3) factor = 1.9;
        if (String.valueOf(user.get("trainingIntensity")).equals("hard") && WebStore.number(user.get("workoutsPerWeek")) >= 6) factor = 2.0;
        return factor;
    }

    private Map<String, Object> program(SplitStrategy strategy) {
        List<Object> week = new ArrayList<>();
        for (String day : strategy.getSchedule()) week.add(day);
        return map("id", strategy.getId(), "name", strategy.getName(), "days", week.size(), "schedule", week);
    }

    private String[] findExercise(String id) {
        for (String[] exercise : EXERCISES) if (exercise[0].equals(id)) return exercise;
        throw new IllegalArgumentException("Упражнение не найдено");
    }

    private double decimal(Object value) {
        if (!(value instanceof Number)) throw new IllegalArgumentException("Ожидалось число");
        return ((Number) value).doubleValue();
    }

    private double round(double value) { return Math.round(value * 10.0) / 10.0; }

    private Map<String, Object> map(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }
}
