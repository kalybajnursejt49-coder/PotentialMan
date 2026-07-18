import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {

    private int id;
    private int age;
    private double weight;
    private double height;
    private String gender;
    private List<WorkoutPlan> workouts;

    public User(int age, double weight, double height, String gender) {
        this.id = 0;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.gender = gender;
        this.workouts = new ArrayList<>();
    }

    public User(int id, int age, double weight, double height, String gender) {
        this.id = id;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.gender = gender;
        this.workouts = new ArrayList<>();
    }

    public int getId() { return id; }
    public int getAge() { return age; }
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public String getGender() { return gender; }
    public List<WorkoutPlan> getWorkouts() { return workouts; }

    public void setId(int id) { this.id = id; }
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

    @Override
    public String toString() {
        return "User{id=" + id + ", age=" + age + ", weight=" + weight + ", height=" + height + ", gender=" + gender + "}";
    }

    public String toJson() {
        return "{\"id\":" + id + ",\"age\":" + age + ",\"weight\":" + weight + ",\"height\":" + height + ",\"gender\":\"" + gender + "\"}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return age == other.age
                && Double.compare(weight, other.weight) == 0
                && Double.compare(height, other.height) == 0
                && gender.equalsIgnoreCase(other.gender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, weight, height, gender.toLowerCase());
    }
}