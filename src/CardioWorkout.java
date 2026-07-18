public class CardioWorkout extends WorkoutPlan {

    private double distance;

    public CardioWorkout(int duration, int calories, double distance) {
        super("Cardio", duration, calories);
        this.distance = distance;
    }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    @Override
    public String getDetails() {
        return "distance=" + distance + " km";
    }

    @Override
    public String getDetailsJson() {
        return "\"distance\":" + distance;
    }
}