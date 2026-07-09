import java.util.ArrayList;
import java.util.List;

public class User {

    private int age;
    private double weight;
    private double height;
    private String gender;
    private List<WorkoutPlan> workouts;

    public User(int age, double weight, double height, String gender) {
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.gender = gender;
        this.workouts = new ArrayList<>();
    }

    public int getAge() { return age; }
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public String getGender() { return gender; }
    public List<WorkoutPlan> getWorkouts() { return workouts; }

    public void setAge(int age) { this.age = age; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setHeight(double height) { this.height = height; }
    public void setGender(String gender) { this.gender = gender; }

    public void addWorkout(WorkoutPlan plan) {
        workouts.add(plan);
    }

    public int getTotalCalories() {
        int total = 0;
        for (WorkoutPlan plan : workouts) {
            total = total + plan.getCalories();
        }
        return total;
    }

    public String toString() {
        return "User{age=" + age + ", weight=" + weight + ", height=" + height + ", gender=" + gender + "}";
    }
}