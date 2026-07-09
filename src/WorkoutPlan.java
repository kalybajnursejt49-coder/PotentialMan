public class WorkoutPlan {

    private String type;
    private int duration;
    private int calories;

    public WorkoutPlan(String type, int duration, int calories) {
        this.type = type;
        this.duration = duration;
        this.calories = calories;
    }

    public String getType() { return type; }
    public int getDuration() { return duration; }
    public int getCalories() { return calories; }

    public void setType(String type) { this.type = type; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setCalories(int calories) { this.calories = calories; }

    public String toString() {
        return "WorkoutPlan{type=" + type + ", duration=" + duration + " min, calories=" + calories + "}";
    }
}