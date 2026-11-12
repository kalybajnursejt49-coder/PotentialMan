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
            {"cable_crunch", "Скручивания на блоке", "Пресс", "abs", "chest"},
            {"close_grip_latpull", "Тяга верхнего блока узким хватом", "Спина", "back", "biceps"},
            {"seated_lateral_raise", "Разведение гантелей сидя", "Плечи", "shoulders", "chest"},
            {"glute_bridge", "Ягодичный мостик", "Ноги", "legs", "abs"},
            {"hip_thrust", "Ягодичный мостик со штангой", "Ноги", "legs", "abs"},
            {"single_leg_glute_bridge", "Ягодичный мостик одной ногой", "Ноги", "legs", "abs"},
            {"hyperextension", "Гиперэкстензия", "Спина", "back", "legs"},
            {"seated_calf_raise", "Подъём на носки сидя", "Ноги", "legs", "abs"},
            {"standing_calf_raise", "Подъём на носки стоя", "Ноги", "legs", "abs"},
            {"russian_twist", "Русские скручивания", "Пресс", "abs", "shoulders"},
            {"reverse_crunch", "Обратные скручивания", "Пресс", "abs", "legs"},
            {"step_up", "Зашагивания на платформу", "Ноги", "legs", "abs"},
            {"goblet_squat", "Приседания с гантелью", "Ноги", "legs", "abs"},
            {"bulgarian_squat", "Болгарские выпады", "Ноги", "legs", "abs"},
            {"machine_chest_press", "Жим в грудном тренажёре", "Грудь", "chest", "triceps"},
            {"assisted_pullups", "Подтягивания в гравитроне", "Спина", "back", "biceps"},
            {"reverse_pec_deck", "Обратная бабочка", "Плечи", "shoulders", "back"},
            {"cable_lateral_raise", "Отведение руки на блоке", "Плечи", "shoulders", "chest"},
            {"seated_triceps_extension", "Разгибание руки с гантелью сидя", "Трицепс", "triceps", "shoulders"},
            {"run_easy", "Бег в спокойном темпе", "Кардио", "legs", "abs", "time"},
            {"run_intervals", "Интервальный бег", "Кардио", "legs", "abs", "time"},
            {"walk_fast", "Быстрая ходьба", "Кардио", "legs", "abs", "time"},
            {"cycling", "Велосипед", "Кардио", "legs", "abs", "time"},
            {"elliptical", "Эллиптический тренажёр", "Кардио", "legs", "abs", "time"},
            {"jump_rope", "Скакалка", "Кардио", "legs", "shoulders", "time"},
            {"row_cardio", "Гребной тренажёр по времени", "Кардио", "back", "legs", "time"},
            {"stretching", "Растяжка", "Мобильность", "legs", "back", "time"},
            {"mobility", "Суставная разминка", "Мобильность", "shoulders", "legs", "time"},
            {"foam_rolling", "МФР с роллом", "Мобильность", "legs", "back", "time"}
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
            String id = EXERCISES[i][0];
            int rest = EXERCISES[i].length > 5 ? 60 :
                    List.of("weighted_pullups", "weighted_pushups", "squat", "front_squat", "deadlift", "romanian_deadlift", "leg_press").contains(id) ? 180 :
                    List.of("bench", "incline_bench", "dumbbell_bench", "incline_dumbbell", "pullups", "chinups", "row", "dumbbell_row", "t_bar_row", "press", "dumbbell_press", "close_grip_bench").contains(id) ? 150 : 75;
            result.add(map("id", EXERCISES[i][0], "name", EXERCISES[i][1], "group", EXERCISES[i][2],
                    "unit", EXERCISES[i].length > 5 ? "minutes" : "reps", "restSeconds", rest));
        }
        return result;
    }

    public List<Object> bodyWeights(int userId) {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        List<Object> records = store.getBodyWeights(userId);
        records.sort((a, b) -> String.valueOf(WebStore.object(a).get("date"))
                .compareTo(String.valueOf(WebStore.object(b).get("date"))));
        return records;
    }

    public Map<String, Object> saveBodyWeight(int userId, Map<String, Object> data) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        LocalDate date = journalDate(data.get("date"));
        double weight = decimal(data.get("weightKg"));
        if (!Double.isFinite(weight) || weight < 25 || weight > 400) throw new IllegalArgumentException("Вес тела: от 25 до 400 кг");
        return store.saveBodyWeight(userId, date.toString(), round(weight));
    }

    public boolean deleteBodyWeight(int userId, String date) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        return store.deleteBodyWeight(userId, journalDate(date).toString());
    }

    public Map<String, Object> exportData(int userId) {
        return map("profile", profile(userId), "workouts", history(userId), "journal", journal(userId),
                "strength", strength(userId), "bodyWeights", bodyWeights(userId),
                "templates", templates(userId), "programs", programs(userId));
    }

    public List<Object> programs() {
        List<Object> result = new ArrayList<>();
        result.add(program(SplitFactory.forId("full").create()));
        result.add(program(SplitFactory.forId("upper").create()));
        result.add(program(SplitFactory.forId("classic").create()));
        result.add(program(SplitFactory.forId("ppl").create()));
        result.add(program(SplitFactory.forId("weightloss").create()));
        result.add(program(SplitFactory.forId("running").create()));
        result.add(program(SplitFactory.forId("mixed").create()));
        result.add(program(SplitFactory.forId("home").create()));
        result.add(program(SplitFactory.forId("recovery").create()));
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
        for (Object value : exercises) WebStore.object(value).put("setSeconds", new ArrayList<>());
        String name = data.get("name") == null ? "Моя тренировка" : String.valueOf(data.get("name")).trim();
        if (name.isEmpty() || name.length() > 50) throw new IllegalArgumentException("Проверьте название тренировки");
        return store.saveTemplate(map("userId", userId, "programId", programId, "dayIndex", dayIndex,
                "name", name, "exercises", exercises));
    }

    public boolean deleteTemplate(int userId, String programId, int dayIndex) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        return store.deleteTemplate(userId, programId, dayIndex);
    }

    public Map<String, Object> recommend(int days) { return recommend(days, "strength"); }

    public Map<String, Object> recommend(int days, String goal) {
        SplitStrategy strategy = SplitFactory.recommend(days, goal);
        return map("recommendedId", strategy.getId(), "days", days, "goal", goal);
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
            double weight = item.get("weightKg") == null ? 0 : decimal(item.get("weightKg"));
            if (!Double.isFinite(weight) || weight < 0 || weight > 500 || (exercise.length > 5 && weight != 0)) {
                throw new IllegalArgumentException("Проверьте рабочий вес упражнения");
            }
            List<Object> setSeconds = item.get("setSeconds") == null ? new ArrayList<>() : WebStore.array(item.get("setSeconds"));
            if (setSeconds.size() > sets) throw new IllegalArgumentException("Слишком много записанных подходов");
            for (Object secondsValue : setSeconds) {
                int seconds = WebStore.number(secondsValue);
                if (seconds < 1 || seconds > 86400) throw new IllegalArgumentException("Время подхода должно быть от 1 секунды до 24 часов");
            }
            builder.addExercise(exercise, sets, reps, drop, superset, heavy, round(weight), setSeconds);
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
        String note = data.get("note") == null ? "" : String.valueOf(data.get("note")).trim();
        if (note.length() > 250) throw new IllegalArgumentException("Заметка: не больше 250 символов");
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
                "calories", built.get("calories"), "load", built.get("load"), "exercises", built.get("exercises"), "note", note);
        return store.saveWorkout(result);
    }

    public List<Object> history(int userId) {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        List<Object> result = store.getWorkouts(userId);
        List<Object> reverse = new ArrayList<>();
        for (int i = result.size() - 1; i >= 0; i--) reverse.add(result.get(i));
        return reverse;
    }

    public Map<String, Object> weekSummary(int userId) {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        java.util.Set<String> trainingDays = new java.util.HashSet<>();
        int workoutCount = 0;
        int cardioMinutes = 0;
        for (Object value : history(userId)) {
            Map<String, Object> workout = WebStore.object(value);
            String date = String.valueOf(workout.get("date")).substring(0, 10);
            if (date.compareTo(monday.toString()) >= 0 && date.compareTo(today.toString()) <= 0) {
                workoutCount++;
                trainingDays.add(date);
                for (Object exerciseValue : WebStore.array(workout.get("exercises"))) {
                    Map<String, Object> exercise = WebStore.object(exerciseValue);
                    if ("minutes".equals(exercise.get("unit"))) cardioMinutes += WebStore.number(exercise.get("reps"));
                }
            }
        }
        double sleepHours = 0;
        int sleepNights = 0;
        for (Object value : store.getJournal(userId)) {
            Map<String, Object> entry = WebStore.object(value);
            String date = String.valueOf(entry.get("date"));
            if (date.compareTo(monday.toString()) >= 0 && date.compareTo(today.toString()) <= 0
                    && entry.get("sleepHours") instanceof Number) {
                sleepHours += ((Number) entry.get("sleepHours")).doubleValue();
                sleepNights++;
            }
        }
        int elapsedDays = today.getDayOfWeek().getValue();
        return map("startDate", monday.toString(), "endDate", today.toString(), "workouts", workoutCount,
                "cardioMinutes", cardioMinutes,
                "restDays", elapsedDays - trainingDays.size(), "sleepHours", round(sleepHours), "sleepNights", sleepNights);
    }

    public boolean deleteWorkout(int id) throws IOException { return store.deleteWorkout(id); }

    public List<Object> journal(int userId) {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        List<Object> entries = store.getJournal(userId);
        entries.sort((a, b) -> String.valueOf(WebStore.object(b).get("date"))
                .compareTo(String.valueOf(WebStore.object(a).get("date"))));
        return entries;
    }

    public Map<String, Object> saveJournal(int userId, Map<String, Object> data) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Сначала создайте профиль");
        LocalDate date = journalDate(data.get("date"));
        Map<String, Object> entry = journalEntry(userId, date);
        if (data.containsKey("sleepHours")) {
            double hours = decimal(data.get("sleepHours"));
            if (!Double.isFinite(hours) || hours < 0 || hours > 24) throw new IllegalArgumentException("Сон: от 0 до 24 часов");
            entry.put("sleepHours", round(hours));
        }
        for (String key : new String[]{"protein", "fat", "carbs"}) {
            if (data.containsKey(key)) {
                int grams = WebStore.number(data.get(key));
                if (grams < 0 || grams > 1000) throw new IllegalArgumentException("БЖУ: от 0 до 1000 г");
                entry.put(key, grams);
                entry.put("foodLogged", true);
            }
        }
        if (data.containsKey("protein") || data.containsKey("fat") || data.containsKey("carbs")) {
            entry.put("meals", List.of(map("id", 1, "type", "other", "foodName", "Итог за день",
                    "portionGrams", 0, "protein", entry.get("protein"), "fat", entry.get("fat"), "carbs", entry.get("carbs"))));
        }
        return store.saveJournal(entry);
    }

    public boolean deleteSleep(int userId, String dateText) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        LocalDate date = journalDate(dateText);
        Map<String, Object> entry = journalEntry(userId, date);
        if (entry.get("sleepHours") == null) return false;
        if (!Boolean.TRUE.equals(entry.get("foodLogged"))) return store.deleteJournal(userId, date.toString());
        entry.put("sleepHours", null);
        store.saveJournal(entry);
        return true;
    }

    private LocalDate journalDate(Object value) {
        LocalDate date;
        try { date = LocalDate.parse(String.valueOf(value)); }
        catch (Exception e) { throw new IllegalArgumentException("Проверьте дату записи"); }
        if (date.isAfter(LocalDate.now()) || date.isBefore(LocalDate.now().minusYears(3))) {
            throw new IllegalArgumentException("Дата должна быть за последние три года");
        }
        return date;
    }

    private Map<String, Object> journalEntry(int userId, LocalDate date) {
        Map<String, Object> entry = map("userId", userId, "date", date.toString(),
                "sleepHours", null, "protein", 0, "fat", 0, "carbs", 0, "foodLogged", false,
                "meals", new ArrayList<>());
        for (Object value : store.getJournal(userId)) {
            Map<String, Object> old = WebStore.object(value);
            if (String.valueOf(old.get("date")).equals(date.toString())) entry.putAll(old);
        }
        return entry;
    }

    private List<Object> meals(Map<String, Object> entry) {
        List<Object> result = new ArrayList<>();
        if (entry.get("meals") != null) result.addAll(WebStore.array(entry.get("meals")));
        if (result.isEmpty() && Boolean.TRUE.equals(entry.get("foodLogged"))) {
            int protein = WebStore.number(entry.get("protein"));
            int fat = WebStore.number(entry.get("fat"));
            int carbs = WebStore.number(entry.get("carbs"));
            result.add(map("id", 1, "type", "other", "foodName", "Ранее записанный итог",
                    "portionGrams", 0, "protein", protein, "fat", fat, "carbs", carbs));
        }
        return result;
    }

    private void totalMeals(Map<String, Object> entry, List<Object> meals) {
        int protein = 0, fat = 0, carbs = 0;
        for (Object value : meals) {
            Map<String, Object> meal = WebStore.object(value);
            protein += WebStore.number(meal.get("protein"));
            fat += WebStore.number(meal.get("fat"));
            carbs += WebStore.number(meal.get("carbs"));
        }
        entry.put("meals", meals);
        entry.put("protein", protein);
        entry.put("fat", fat);
        entry.put("carbs", carbs);
        entry.put("foodLogged", !meals.isEmpty());
    }

    public Map<String, Object> addMeal(int userId, Map<String, Object> data) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Сначала создайте профиль");
        LocalDate date = journalDate(data.get("date"));
        String type = String.valueOf(data.get("type"));
        if (!type.equals("breakfast") && !type.equals("lunch") && !type.equals("dinner") && !type.equals("snack")) {
            throw new IllegalArgumentException("Выберите приём пищи");
        }
        String foodName = String.valueOf(data.get("foodName")).trim();
        if (foodName.isEmpty() || foodName.equals("null") || foodName.length() > 70) {
            throw new IllegalArgumentException("Напишите, что вы съели (до 70 символов)");
        }
        int portion = WebStore.number(data.get("portionGrams"));
        int protein = WebStore.number(data.get("protein"));
        int fat = WebStore.number(data.get("fat"));
        int carbs = WebStore.number(data.get("carbs"));
        if (portion < 1 || portion > 5000 || protein < 0 || protein > 1000 || fat < 0 || fat > 1000
                || carbs < 0 || carbs > 1000) throw new IllegalArgumentException("Проверьте вес порции и БЖУ");
        Map<String, Object> entry = journalEntry(userId, date);
        List<Object> list = meals(entry);
        if (list.size() >= 30) throw new IllegalArgumentException("На один день можно записать до 30 блюд");
        int nextId = 1;
        for (Object value : list) nextId = Math.max(nextId, WebStore.number(WebStore.object(value).get("id")) + 1);
        list.add(map("id", nextId, "type", type, "foodName", foodName, "portionGrams", portion,
                "protein", protein, "fat", fat, "carbs", carbs));
        totalMeals(entry, list);
        return store.saveJournal(entry);
    }

    public boolean deleteMeal(int userId, String dateText, int mealId) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        Map<String, Object> entry = journalEntry(userId, journalDate(dateText));
        List<Object> list = meals(entry);
        for (int i = 0; i < list.size(); i++) {
            if (WebStore.number(WebStore.object(list.get(i)).get("id")) == mealId) {
                list.remove(i);
                totalMeals(entry, list);
                store.saveJournal(entry);
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> strength(int userId) {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        List<Object> records = store.getStrengthRecords(userId);
        records.sort((a, b) -> {
            Map<String, Object> left = WebStore.object(a), right = WebStore.object(b);
            int byDate = String.valueOf(right.get("date")).compareTo(String.valueOf(left.get("date")));
            return byDate != 0 ? byDate : WebStore.number(right.get("id")) - WebStore.number(left.get("id"));
        });
        List<Object> forecasts = new ArrayList<>();
        for (Object value : store.getStrengthGoals(userId)) {
            Map<String, Object> goal = WebStore.object(value);
            String exerciseId = String.valueOf(goal.get("exerciseId"));
            List<Object> matching = new ArrayList<>();
            for (Object recordValue : records) {
                Map<String, Object> record = WebStore.object(recordValue);
                if (String.valueOf(record.get("exerciseId")).equals(exerciseId)) matching.add(record);
            }
            Double latestKg = matching.isEmpty() ? null : ((Number) WebStore.object(matching.get(0)).get("weightKg")).doubleValue();
            Double bestKg = null;
            for (Object recordValue : matching) {
                double weight = ((Number) WebStore.object(recordValue).get("weightKg")).doubleValue();
                if (bestKg == null || weight > bestKg) bestKg = weight;
            }
            Integer weeks = null;
            double target = ((Number) goal.get("targetKg")).doubleValue();
            if (latestKg != null && latestKg >= target) weeks = 0;
            else if (matching.size() >= 3) {
                Map<String, Object> first = WebStore.object(matching.get(matching.size() - 1));
                Map<String, Object> last = WebStore.object(matching.get(0));
                long days = ChronoUnit.DAYS.between(LocalDate.parse(String.valueOf(first.get("date"))),
                        LocalDate.parse(String.valueOf(last.get("date"))));
                double gain = ((Number) last.get("weightKg")).doubleValue() - ((Number) first.get("weightKg")).doubleValue();
                if (days >= 14 && gain > 0) {
                    int estimate = (int) Math.ceil((target - latestKg) * days / gain / 7.0);
                    if (estimate >= 1 && estimate <= 104) weeks = estimate;
                }
            }
            forecasts.add(map("exerciseId", exerciseId, "exerciseName", goal.get("exerciseName"),
                    "targetKg", target, "latestKg", latestKg, "bestKg", bestKg,
                    "recordCount", matching.size(), "estimatedWeeks", weeks));
        }
        return map("records", records, "goals", forecasts);
    }

    public Map<String, Object> saveStrengthRecord(int userId, Map<String, Object> data) throws IOException {
        return store.saveStrengthRecord(strengthRecordData(userId, data));
    }

    public Map<String, Object> updateStrengthRecord(int userId, int recordId, Map<String, Object> data) throws IOException {
        return store.updateStrengthRecord(userId, recordId, strengthRecordData(userId, data));
    }

    private Map<String, Object> strengthRecordData(int userId, Map<String, Object> data) {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Сначала создайте профиль");
        LocalDate date = journalDate(data.get("date"));
        String exerciseId = String.valueOf(data.get("exerciseId"));
        String[] exercise = findExercise(exerciseId);
        if (exercise.length > 5) throw new IllegalArgumentException("Для кардио записывайте минуты в тренировке");
        double weight = decimal(data.get("weightKg"));
        int sets = WebStore.number(data.get("sets"));
        int reps = WebStore.number(data.get("reps"));
        if (!Double.isFinite(weight) || weight <= 0 || weight > 500 || sets < 1 || sets > 10 || reps < 1 || reps > 50) {
            throw new IllegalArgumentException("Проверьте вес, подходы и повторения");
        }
        return map("userId", userId, "date", date.toString(),
                "exerciseId", exerciseId, "exerciseName", exercise[1], "weightKg", round(weight),
                "sets", sets, "reps", reps);
    }

    public boolean deleteStrengthRecord(int userId, int recordId) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Пользователь не найден");
        return store.deleteStrengthRecord(userId, recordId);
    }

    public Map<String, Object> saveStrengthGoal(int userId, Map<String, Object> data) throws IOException {
        if (store.getUser(userId) == null) throw new IllegalArgumentException("Сначала создайте профиль");
        String exerciseId = String.valueOf(data.get("exerciseId"));
        String[] exercise = findExercise(exerciseId);
        if (exercise.length > 5) throw new IllegalArgumentException("Выберите силовое упражнение");
        double target = decimal(data.get("targetKg"));
        if (!Double.isFinite(target) || target <= 0 || target > 500) throw new IllegalArgumentException("Цель: от 1 до 500 кг");
        return store.saveStrengthGoal(map("userId", userId, "exerciseId", exerciseId,
                "exerciseName", exercise[1], "targetKg", round(target)));
    }

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
        int sum = 0;
        for (Object value : result) sum += WebStore.number(WebStore.object(value).get("progress"));
        int score = Math.round(sum / (float) result.size());
        Double sleep = null;
        int eatenProtein = 0;
        boolean proteinLogged = false;
        for (Object value : store.getJournal(userId)) {
            Map<String, Object> entry = WebStore.object(value);
            String date = String.valueOf(entry.get("date"));
            if ((date.equals(LocalDate.now().toString()) || date.equals(LocalDate.now().minusDays(1).toString()))
                    && entry.get("sleepHours") instanceof Number) {
                if (sleep == null || date.equals(LocalDate.now().toString())) sleep = ((Number) entry.get("sleepHours")).doubleValue();
            }
            if (date.equals(LocalDate.now().toString()) && Boolean.TRUE.equals(entry.get("foodLogged"))) {
                eatenProtein = WebStore.number(entry.get("protein"));
                proteinLogged = true;
            }
        }
        int sleepMinimum = WebStore.number(profile.get("age")) < 18 ? 8 : 7;
        if (sleep != null && sleep < sleepMinimum) score -= 10;
        if (sleep != null && sleep >= sleepMinimum + 1) score += 5;
        int proteinTarget = (int) Math.round(((Number) profile.get("weight")).doubleValue() * proteinFactor(profile));
        if (proteinLogged && eatenProtein < proteinTarget * 0.75) score -= 5;
        if (proteinLogged && eatenProtein >= proteinTarget) score += 3;
        score = Math.max(0, Math.min(100, score));
        return map("muscles", result, "readinessPercent", score, "hasWorkout", !workouts.isEmpty(), "sleepHours", sleep,
                "proteinToday", eatenProtein, "proteinTarget", proteinTarget, "proteinLogged", proteinLogged);
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
        return map("id", strategy.getId(), "name", strategy.getName(), "days", week.size(), "schedule", week,
                "starter", starter(strategy.getId()));
    }

    private Map<String, Object> preset(String id, int sets, int reps) {
        return map("id", id, "sets", sets, "reps", reps);
    }

    private List<Object> starter(String id) {
        List<Object> days = new ArrayList<>();
        if (id.equals("upper")) {
            days.add(List.of(preset("bench", 4, 8), preset("latpull", 4, 10), preset("dumbbell_press", 3, 10),
                    preset("seated_cable_row", 3, 10), preset("butterfly", 3, 12),
                    preset("seated_lateral_raise", 3, 15), preset("scott_curl", 3, 10), preset("rope_pushdown", 3, 12)));
            days.add(List.of(preset("hip_thrust", 4, 12), preset("hyperextension", 3, 12),
                    preset("seated_calf_raise", 4, 15), preset("crunch", 3, 15)));
            days.add(List.of(preset("weighted_pullups", 4, 8), preset("incline_dumbbell", 4, 10),
                    preset("dumbbell_row", 3, 10), preset("dumbbell_press", 3, 10),
                    preset("hammer_curl", 3, 12), preset("seated_triceps_extension", 3, 12)));
            days.add(List.of(preset("single_leg_glute_bridge", 3, 12), preset("hyperextension", 3, 12),
                    preset("seated_calf_raise", 4, 15), preset("crunch", 3, 15)));
        } else if (id.equals("running")) {
            days.add(List.of(preset("run_easy", 1, 25)));
            days.add(List.of(preset("run_intervals", 1, 20)));
            days.add(List.of(preset("run_easy", 1, 30)));
        } else if (id.equals("weightloss")) {
            days.add(List.of(preset("goblet_squat", 3, 12), preset("pushups", 3, 10), preset("row", 3, 10)));
            days.add(List.of(preset("run_easy", 1, 25)));
            days.add(List.of(preset("lunge", 3, 12), preset("dumbbell_bench", 3, 10), preset("latpull", 3, 10)));
            days.add(List.of(preset("walk_fast", 1, 40)));
        } else if (id.equals("mixed")) {
            days.add(List.of(preset("bench", 3, 10), preset("pullups", 3, 8)));
            days.add(List.of(preset("run_easy", 1, 25)));
            days.add(List.of(preset("squat", 3, 10), preset("leg_press", 3, 12)));
            days.add(List.of(preset("cycling", 1, 30)));
        } else if (id.equals("home")) {
            days.add(List.of(preset("pushups", 3, 10), preset("goblet_squat", 3, 12), preset("crunch", 3, 15)));
            days.add(List.of(preset("lunge", 3, 12), preset("glute_bridge", 3, 15), preset("crunch", 3, 15)));
            days.add(List.of(preset("pushups", 3, 10), preset("step_up", 3, 12), preset("crunch", 3, 15)));
        } else if (id.equals("recovery")) {
            days.add(List.of(preset("walk_fast", 1, 25)));
            days.add(List.of(preset("mobility", 1, 20)));
            days.add(List.of(preset("elliptical", 1, 20), preset("stretching", 1, 15)));
        } else if (id.equals("full")) {
            days.add(List.of(preset("squat", 3, 10), preset("bench", 3, 10), preset("row", 3, 10)));
            days.add(List.of(preset("leg_press", 3, 12), preset("dumbbell_bench", 3, 10), preset("latpull", 3, 10)));
            days.add(List.of(preset("lunge", 3, 12), preset("press", 3, 10), preset("pullups", 3, 8)));
        } else if (id.equals("classic")) {
            days.add(List.of(preset("bench", 4, 8), preset("butterfly", 3, 12)));
            days.add(List.of(preset("pullups", 4, 8), preset("row", 3, 10)));
            days.add(List.of(preset("squat", 4, 8), preset("leg_press", 3, 12)));
            days.add(List.of(preset("dumbbell_press", 3, 10), preset("raises", 3, 12)));
            days.add(List.of(preset("scott_curl", 3, 10), preset("rope_pushdown", 3, 12)));
        } else if (id.equals("ppl")) {
            days.add(List.of(preset("bench", 3, 10), preset("press", 3, 10)));
            days.add(List.of(preset("pullups", 3, 8), preset("row", 3, 10)));
            days.add(List.of(preset("squat", 3, 10), preset("leg_press", 3, 12)));
            days.add(List.of(preset("dumbbell_bench", 3, 10), preset("rope_pushdown", 3, 12)));
            days.add(List.of(preset("latpull", 3, 10), preset("scott_curl", 3, 12)));
            days.add(List.of(preset("romanian_deadlift", 3, 10), preset("lunge", 3, 12)));
        }
        return days;
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
