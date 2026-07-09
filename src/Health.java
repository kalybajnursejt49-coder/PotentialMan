public class Health {

    private static final double ACTIVITY_COEFFICIENT = 1.2;
    private static final int CALORIE_STEP = 500;

    public double calculateBMR(User user) {
        double bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge();

        if (isMale(user)) {
            return bmr + 5;
        } else {
            return bmr - 161;
        }
    }

    public double calculateDailyNorm(User user, String goal) {
        double dailyCalories = calculateBMR(user) * ACTIVITY_COEFFICIENT;

        if (goal.equalsIgnoreCase("lose")) {
            return dailyCalories - CALORIE_STEP;
        }
        if (goal.equalsIgnoreCase("gain")) {
            return dailyCalories + CALORIE_STEP;
        }
        return dailyCalories;
    }

    public String getBMICategory(User user) {
        double bmi = calculateBMI(user);

        if (bmi < 18.5) return formatBMI(bmi, "Недостаточный вес");
        if (bmi < 25) return formatBMI(bmi, "Норма");
        if (bmi < 30) return formatBMI(bmi, "Избыточный вес");
        return formatBMI(bmi, "Ожирение");
    }

    private boolean isMale(User user) {
        return user.getGender().equalsIgnoreCase("M");
    }

    private double calculateBMI(User user) {
        double h = user.getHeight() / 100.0;
        return user.getWeight() / (h * h);
    }

    private String formatBMI(double bmi, String category) {
        return String.format("%.1f (%s)", bmi, category);
    }
}