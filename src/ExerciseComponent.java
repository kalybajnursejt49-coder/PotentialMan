import java.util.LinkedHashMap;
import java.util.Map;

public interface ExerciseComponent {
    Map<String, Double> getLoad();
    int getCalories();
}

class BasicExercise implements ExerciseComponent {
    private final String id;
    private final String primaryMuscle;
    private final String secondaryMuscle;
    private final int sets;
    private final int reps;

    public BasicExercise(String id, String primaryMuscle, String secondaryMuscle, int sets, int reps) {
        this.id = id;
        this.primaryMuscle = primaryMuscle;
        this.secondaryMuscle = secondaryMuscle;
        this.sets = sets;
        this.reps = reps;
    }

    public Map<String, Double> getLoad() {
        double factor = sets * reps / 30.0;
        if (id.startsWith("weighted_")) factor *= 1.25;
        Map<String, Double> result = new LinkedHashMap<>();
        result.put(primaryMuscle, factor * 2.2);
        result.put(secondaryMuscle, factor * 0.8);
        return result;
    }

    public int getCalories() { return Math.round((float) (sets * reps * 1.2)); }
}

abstract class ExerciseDecorator implements ExerciseComponent {
    private final ExerciseComponent exercise;
    private final double multiplier;

    protected ExerciseDecorator(ExerciseComponent exercise, double multiplier) {
        this.exercise = exercise;
        this.multiplier = multiplier;
    }

    public Map<String, Double> getLoad() {
        Map<String, Double> original = exercise.getLoad();
        Map<String, Double> result = new LinkedHashMap<>();
        for (String muscle : original.keySet()) result.put(muscle, original.get(muscle) * multiplier);
        return result;
    }

    public int getCalories() { return Math.round((float) (exercise.getCalories() * multiplier)); }
}

class DropSetDecorator extends ExerciseDecorator {
    public DropSetDecorator(ExerciseComponent exercise) { super(exercise, 1.2); }
}

class SuperSetDecorator extends ExerciseDecorator {
    public SuperSetDecorator(ExerciseComponent exercise) { super(exercise, 1.15); }
}

class HeavyWeightDecorator extends ExerciseDecorator {
    public HeavyWeightDecorator(ExerciseComponent exercise) { super(exercise, 1.25); }
}
