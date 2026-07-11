import java.util.Objects;

public abstract class WorkoutPlan {

    protected String type;
    protected int duration;
    protected int calories;

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

    public abstract String getDetails();

    @Override
    public String toString() {
        return type + ", " + duration + " min, " + calories + " kcal, " + getDetails();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorkoutPlan)) return false;
        WorkoutPlan other = (WorkoutPlan) obj;
        return duration == other.duration
                && calories == other.calories
                && type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, duration, calories);
    }
}