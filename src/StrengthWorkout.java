public class StrengthWorkout extends WorkoutPlan {

    private int sets;
    private int reps;

    public StrengthWorkout(int duration, int calories, int sets, int reps) {
        super("Strength", duration, calories);
        this.sets = sets;
        this.reps = reps;
    }

    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public void setSets(int sets) { this.sets = sets; }
    public void setReps(int reps) { this.reps = reps; }

    @Override
    public String getDetails() {
        return "sets=" + sets + ", reps=" + reps;
    }

    @Override
    public String getDetailsJson() {
        return "\"sets\":" + sets + ",\"reps\":" + reps;
    }
}