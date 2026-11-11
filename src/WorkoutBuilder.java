import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkoutBuilder {
    private final String[] muscles;
    private final List<ExerciseComponent> components = new ArrayList<>();
    private final List<Object> exercises = new ArrayList<>();
    private final List<LoadObserver> observers = new ArrayList<>();
    private final Map<String, Double> load = new LinkedHashMap<>();
    private int calories;

    public WorkoutBuilder(String[] muscles) {
        this.muscles = muscles;
        for (String muscle : muscles) load.put(muscle, 0.0);
    }

    public void addObserver(LoadObserver observer) {
        observers.add(observer);
        observer.update(load);
    }

    public void addExercise(String[] definition, int sets, int reps, boolean dropSet,
                            boolean superSet, boolean heavyWeight) {
        if (sets < 1 || sets > 10 || reps < 1 || reps > 50) {
            throw new IllegalArgumentException("Подходы: 1–10, повторения: 1–50");
        }
        ExerciseComponent exercise = new BasicExercise(definition[0], definition[3], definition[4], sets, reps);
        if (dropSet) exercise = new DropSetDecorator(exercise);
        if (superSet) exercise = new SuperSetDecorator(exercise);
        if (heavyWeight) exercise = new HeavyWeightDecorator(exercise);
        components.add(exercise);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", definition[0]);
        item.put("name", definition[1]);
        item.put("group", definition[2]);
        item.put("sets", sets);
        item.put("reps", reps);
        item.put("dropSet", dropSet);
        item.put("superSet", superSet);
        item.put("heavyWeight", heavyWeight);
        exercises.add(item);
        recalculate();
    }

    public void removeExercise(int index) {
        if (index < 0 || index >= components.size()) throw new IllegalArgumentException("Упражнение не найдено");
        components.remove(index);
        exercises.remove(index);
        recalculate();
    }

    private void recalculate() {
        for (String muscle : muscles) load.put(muscle, 0.0);
        calories = 0;
        for (ExerciseComponent component : components) {
            Map<String, Double> part = component.getLoad();
            for (String muscle : part.keySet()) load.put(muscle, load.get(muscle) + part.get(muscle));
            calories += component.getCalories();
        }
        for (LoadObserver observer : observers) observer.update(load);
    }

    public int getCalories() { return calories; }
    public List<Object> getExercises() { return new ArrayList<>(exercises); }
}
